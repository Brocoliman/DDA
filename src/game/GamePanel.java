package game;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.stream.IntStream;


import static game.Config.*;

public class GamePanel extends JPanel implements ActionListener {
    boolean running;
    boolean firstMouseRead;
    long lastFrameNanos;
    long lastFpsNanos;
    double currentFps;
    double frameTimeMs;
    Timer timer;
    Robot robot;

    // --------------------------
    // Render cache
    // --------------------------
    BufferedImage frame;
    int[] pixels;

    // --------------------------
    // States
    // --------------------------
    double player_theta;                        // azimuth, radians
    double player_epsilon;                      // elevation, radians, in (-π/2, +π/2)
    double player_x, player_y, player_z;        // position in world units
    double player_dforward, player_dright, player_dup;

    // --------------------------
    // Map
    // --------------------------

    int [][][] map = new TestMap().mountainmap;

    // --------------------------
    // Initialization
    // --------------------------

    GamePanel() { // initialize things at the instance of an 'app'
        this.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT)); // minimum size for the component to display correctly
        this.setBackground(SKY_COLOR);
        this.setFocusable(true);
        this.addKeyListener(new ProjectionKeyAdapter());

        startGame();

        // Hide cursor (if in window)
        Cursor blank = Toolkit.getDefaultToolkit().createCustomCursor(
                new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB),
                new Point(0, 0), "blank"
        );
        this.setCursor(blank);

        // Relative motion robot
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            robot = null; // fallback if Robot isn't available
        }


    }

    public void startGame() { // initialize at the instance of a 'game'
        // Initialize player states
        player_theta = START_THETA;
        player_epsilon = START_EPSILON;
        player_x = START_X;
        player_y = START_Y;
        player_z = START_Z;

        running = true;
        lastFrameNanos = System.nanoTime();
        lastFpsNanos = System.nanoTime();
        currentFps = 0;
        firstMouseRead = true;
        timer = new Timer(DELAY, this);
        timer.start();

        frame = new BufferedImage(RENDER_WIDTH, RENDER_HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (running) {
            updateMovement();
            drawRaysDDA(g);
            drawDebugInfo(g);

            long now = System.nanoTime();
            frameTimeMs = (now - lastFpsNanos) / 1e6;
            currentFps = 1000.0 / frameTimeMs;
            currentFps = currentFps * 0.9 + (1000.0/frameTimeMs) * 0.1;
            lastFpsNanos = now;
        }
    }

    // --------------------------
    // Helper functions
    // --------------------------

    public static int argmin(double[] arr) {
        int minIndex = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[minIndex]) {
                minIndex = i;
            }
        }
        return minIndex;
    }

    public static boolean isInMap(double[] arr) {
        return (-1 < arr[0]) && (arr[0] < WORLD_VOXELS_X) &&
                (-1 < arr[1]) && (arr[1] < WORLD_VOXELS_Y) &&
                (-1 < arr[2]) && (arr[2] < WORLD_VOXELS_Z);
    }

    public static boolean inRenderDistance(double[] arr) {
        int extra_world_bounds = 10;
        return (-extra_world_bounds < arr[0]) && (arr[0] < WORLD_VOXELS_X+extra_world_bounds) &&
                (-extra_world_bounds < arr[1]) && (arr[1] < WORLD_VOXELS_Y+extra_world_bounds) &&
                (-extra_world_bounds < arr[2]) && (arr[2] < WORLD_VOXELS_Z+extra_world_bounds);
    }

    // --------------------------
    // Raycast functions
    // --------------------------

    // --------------------------
    // Render functions
    // --------------------------

    public void drawRaysDDA(Graphics g) {

        // First pass for DDA
        double angle_per_pixel = FOV_HORIZONTAL / RENDER_WIDTH;
        double[] playerPos = new double[] {player_x, player_y, player_z};
        double[] mapPos = new double[] {Math.floor(player_x), Math.floor(player_y), Math.floor(player_z)};
        CameraBasis cameraBasis = new CameraBasis(player_theta, player_epsilon);
        double cb_fx = cameraBasis.forward.x;
        double cb_fy = cameraBasis.forward.y;
        double cb_fz = cameraBasis.forward.z;
        double cb_ux = cameraBasis.up.x;
        double cb_uy = cameraBasis.up.y;
        double cb_uz = cameraBasis.up.z;
        double cb_rx = cameraBasis.right.x;
        double cb_ry = cameraBasis.right.y;
        double cb_rz = cameraBasis.right.z;

        IntStream.range(0, RENDER_HEIGHT).parallel().forEach(row -> {
            double[] t = new double[3];
            double[] dt = new double[3];
            double[] m = new double[3];
            double[] dm = new double[3];
            double[] dir = new double[3];

            for (int col = 0; col < RENDER_WIDTH; col++){
                // Find ray unit vector (dx, dy, dz)
                double u = (col - (double) RENDER_WIDTH /2 + 0.5)*angle_per_pixel;
                double v = ((double) RENDER_HEIGHT /2 - row - 0.5)*angle_per_pixel;
                dir[0]=cb_fx + u*cb_rx + v*cb_ux;
                dir[1]=cb_fy + u*cb_ry + v*cb_uy;
                dir[2]=cb_fz + u*cb_rz + v*cb_uz;
                double len = Math.sqrt(dir[0]*dir[0]+dir[1]*dir[1]+dir[2]*dir[2]);
                dir[0] /= len;
                dir[1] /= len;
                dir[2] /= len;


                /*
                DDA Goal:
                Traverse the line of voxels that the ray passes
                Then check for any solid blocks
                 */

                /*
                Find t of first crossing of x-plane
                Eq. 1
                if dx > 0: px + t * dx = floor(px) + 1
                if dx < 0: px + t * dx = floor(px)
                if dx = 0: t = +INF

                Eq. 2: time updates
                t[i+1] = t[i] + dt
                where dt = 1 / |dx|

                Eq. 3: voxel lattice pos updates
                mx[i+1] = mx[i] + dmx
                where dm = sign of dx
                 */

                // Initialize event steps
                t[0]=t[1]=t[2]=0;
                dt[0]=dt[1]=dt[2]=0;
                dm[0]=dm[1]=dm[2]=0;
                m[0]=mapPos[0]; m[1]=mapPos[1]; m[2]=mapPos[2];
                for (int axis = 0; axis < 3; axis++) {
                    // Eq. 1 & 3b
                    if (dir[axis] > 0) {
                        t[axis] = (mapPos[axis] + 1 - playerPos[axis]) / dir[axis];
                        dm[axis] = 1.0;
                    } else if (dir[axis] < 0) {
                        t[axis] = (mapPos[axis] - playerPos[axis]) / dir[axis];
                        dm[axis] = -1.0;
                    } else {
                        t[axis] = Double.POSITIVE_INFINITY;
                        continue; // don't need dt or dm, will not be used
                    }
                    // Eq. 2b
                    dt[axis] = 1.0 / Math.abs(dir[axis]);
                }

                // Traverse trajectory
                boolean hit = false;
                double t_hit = 0;
                int steps = 0;
                int dim = -1;
                while (steps < MAX_DDA_STEPS) {
                    dim = argmin(t);
                    t_hit = t[dim];
                    m[dim] += dm[dim];
                    t[dim] += dt[dim];

                    // Check if voxel is outside render distance
                    // if outside, then stop trajectory
                    if (!inRenderDistance(m)) {
                        hit = false;
                        break;
                    }

                    // Check if voxel is in the map
                    // if outside, don't check in the map
                    if (!isInMap(m)) continue;

                    // Check if voxel is solid
                    if (map[(int)m[2]][(int)m[1]][(int)m[0]] != 0) {
                        hit = true;
                        break;
                    }

                    steps++;
                }

                // Now render the pixel
                int rgb;
                if (hit) {
                    int shade;
                    if (dim == 0) {
                        shade = 170;
                    } else if (dim == 1) {
                        shade = 213;
                    } else {
                        shade = 255;
                    }
                    shade /= Math.max(t_hit, 1.0);
                    shade = Math.clamp(shade, 0, 255);
                    rgb = (shade << 16) | (shade << 8) | shade;
                } else {
                    rgb = (135 << 16) | (206 << 8) | 235;
                }
                pixels[row * RENDER_WIDTH + col] = rgb;
            }
        });

        // Blit
        g.drawImage(frame, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);
    }

    private void drawDebugInfo(Graphics g) {
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        String[] lines = {
                String.format("X: %.2f  Y: %.2f  Z: %.2f", player_x, player_y, player_z),
                String.format("Theta: %.2f  Epsilon: %.2f", Math.toDegrees(player_theta), Math.toDegrees(player_epsilon)),
                String.format("FPS: %.1f  Frame: %.2fms", currentFps, frameTimeMs)
        };

        for (int i = 0; i < lines.length; i++) {
            int y = 20 + i * 18;
            // Shadow/outline
            g.setColor(Color.BLACK);
            g.drawString(lines[i], 11, y + 1);
            // Main text
            g.setColor(Color.WHITE);
            g.drawString(lines[i], 10, y);
        }
    }

    // --------------------------
    // Event cycles
    // --------------------------

    public void updateMovement() {
        // Apply player movement update
        long now = System.nanoTime();
        double dt = (now - lastFrameNanos) / 1e9;  // seconds
        lastFrameNanos = now;

        MovementBasis movementBasis = new MovementBasis(player_theta);
        Vec3 player_dxyz = movementBasis.forward.scale(player_dforward)
                .add(movementBasis.right.scale(player_dright))
                .add(movementBasis.up.scale(player_dup));

        player_x += player_dxyz.x * dt * MOVE_SPEED;
        player_y += player_dxyz.y * dt * MOVE_SPEED;
        player_z += player_dxyz.z * dt * MOVE_SPEED;

        // Apply mouse movements update
        Point window_topleft = this.getLocationOnScreen();
        int window_centerx = window_topleft.x + WINDOW_WIDTH/2;
        int window_centery = window_topleft.y + WINDOW_HEIGHT/2;
        if (!firstMouseRead) {
            Point mouse_pxy = MouseInfo.getPointerInfo().getLocation();
            int delta_x = mouse_pxy.x - window_centerx;
            int delta_y = mouse_pxy.y - window_centery;
            player_theta -= delta_x * MOUSE_SENSITIVITY_X;
            player_epsilon -= delta_y * MOUSE_SENSITIVITY_Y;
            robot.mouseMove(window_centerx,window_centery);
        }
        firstMouseRead = false;
    }

    // --------------------------
    // Key Adapter
    // --------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public class ProjectionKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A:
                    player_dright = Math.clamp(player_dright-1, -1, 0);
                    break;
                case KeyEvent.VK_D:
                    player_dright = Math.clamp(player_dright+1, 0, 1);
                    break;
                case KeyEvent.VK_W:
                    player_dforward = Math.clamp(player_dforward+1, 0, 1);
                    break;
                case KeyEvent.VK_S:
                    player_dforward = Math.clamp(player_dforward-1, -1, 0);
                    break;
                case KeyEvent.VK_SPACE:
                    player_dup = Math.clamp(player_dup+1, 0, 1);
                    break;
                case KeyEvent.VK_SHIFT:
                    player_dup = Math.clamp(player_dup-1, -1, 0);
                    break;
            }

        }

        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_D:
                    player_dright = Math.clamp(player_dright - 1, -1, 0);
                    break;
                case KeyEvent.VK_A:
                    player_dright = Math.clamp(player_dright + 1, 0, 1);
                    break;
                case KeyEvent.VK_S:
                    player_dforward = Math.clamp(player_dforward + 1, 0, 1);
                    break;
                case KeyEvent.VK_W:
                    player_dforward = Math.clamp(player_dforward - 1, -1, 0);
                    break;
                case KeyEvent.VK_SHIFT:
                    player_dup = Math.clamp(player_dup + 1, 0, 1);
                    break;
                case KeyEvent.VK_SPACE:
                    player_dup = Math.clamp(player_dup - 1, -1, 0);
                    break;
            }
        }
    }
}
