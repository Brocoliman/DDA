package game;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.stream.IntStream;


import static game.Config.*;

public class GamePanel extends JPanel implements ActionListener {
    boolean running;
    Timer timer;

    // --------------------------
    // Mouse events
    // --------------------------
    boolean firstMouseRead;
    long lastFrameNanos;
    Robot robot;

    // --------------------------
    // Info panel
    // --------------------------
    // For recording Fps
    long lastFpsNanos;
    double currentFps;
    double frameTimeMs;

    // --------------------------
    // Raycast cache
    // --------------------------

    // Reduce allocations
    private static final ThreadLocal<Ray> tlRay = ThreadLocal.withInitial(Ray::new);
    private static final ThreadLocal<double[]> tlT = ThreadLocal.withInitial(() -> new double[3]);
    private static final ThreadLocal<double[]> tlDt = ThreadLocal.withInitial(() -> new double[3]);
    private static final ThreadLocal<double[]> tlM = ThreadLocal.withInitial(() -> new double[3]);
    private static final ThreadLocal<double[]> tlDm = ThreadLocal.withInitial(() -> new double[3]);

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
    Ray target_ray;

    // --------------------------
    // World variables
    // --------------------------

    World world = new World(WORLD_SEED, WORLD_VOXELS_X, WORLD_VOXELS_Y, WORLD_VOXELS_Z);

    // --------------------------
    // Initialization
    // --------------------------

    GamePanel() { // initialize things at the instance of an 'app'
        this.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT)); // minimum size for the component to display correctly
        this.setFocusable(true);
        this.addKeyListener(new GameKeyAdapter(this));
        this.addMouseListener(new GameMouseAdapter(this));

        startGame();

        // Set crosshair cursor
        this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        // Relative motion robot
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            robot = null; // fallback if Robot isn't available
        }
    }

    public void startGame() {
        world.initialize();

        // initialize at the instance of a 'game'
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
            update(g);
        }
    }

    public void update(Graphics g) {
        updateMovement();
        updateTarget();
        drawRaysDDA(g);
        drawDebugInfo(g);

        long now = System.nanoTime();
        frameTimeMs = (now - lastFpsNanos) / 1e6;
        currentFps = 1000.0 / frameTimeMs;
        currentFps = currentFps * 0.9 + (1000.0/frameTimeMs) * 0.1;
        lastFpsNanos = now;
    }

    // --------------------------
    // Raycast functions
    // --------------------------

    public Ray traceRay(double[] dir, double[] startPos, double[] startVoxelPos, Ray out) {
        double[] t = tlT.get();
        double[] dt = tlDt.get();
        double[] m = tlM.get();
        double[] dm = tlDm.get();

        t[0]=t[1]=t[2]=0;
        dt[0]=dt[1]=dt[2]=0;
        dm[0]=dm[1]=dm[2]=0;
        m[0]=startVoxelPos[0]; m[1]=startVoxelPos[1]; m[2]=startVoxelPos[2];

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
        for (int axis = 0; axis < 3; axis++) {
            // Eq. 1 & 3b
            if (dir[axis] > 0) {
                t[axis] = (startVoxelPos[axis] + 1 - startPos[axis]) / dir[axis];
                dm[axis] = 1.0;
            } else if (dir[axis] < 0) {
                t[axis] = (startVoxelPos[axis] - startPos[axis]) / dir[axis];
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
        int axis = -1;
        while (steps < MAX_DDA_STEPS) {
            axis = 0;
            for (int i = 1; i < t.length; i++) {
                if (t[i] < t[axis]) {
                    axis = i;
                }
            }
            t_hit = t[axis];
            m[axis] += dm[axis];
            t[axis] += dt[axis];

            // Check if voxel is outside render distance
            // if outside, then stop trajectory
            if (t_hit > MAX_DDA_TIME) break;

            // Check if voxel is in the map
            // if outside, don't check in the map
            if (!((-1 < m[0]) && (m[0] < WORLD_VOXELS_X) &&
                    (-1 < m[1]) && (m[1] < WORLD_VOXELS_Y) &&
                    (-1 < m[2]) && (m[2] < WORLD_VOXELS_Z))) continue;

            // Check if voxel is solid
            if (world.map[(int)m[2]][(int)m[1]][(int)m[0]] != 0) {
                hit = true;
                break;
            }
            steps++;
        }

        // Find block that is hit; if not hit, it won't be accessed anyway
        int[] hit_block = {(int)m[0], (int)m[1], (int)m[2]};  // [x, y, z]

        out.dir = dir;
        out.hit = hit;
        out.t_hit = t_hit;
        out.axis = axis;
        out.hit_block[0] = (int)m[0];
        out.hit_block[1] = (int)m[1];
        out.hit_block[2] = (int)m[2];
        return out;

    }

    // --------------------------
    // Render functions
    // --------------------------

    public void drawPixel(Ray ray, int row, int col) {
        int rgb;
        if (ray.hit) {
            int shade;
            if (ray.axis == 0) {
                shade = 170;
            } else if (ray.axis == 1) {
                shade = 213;
            } else {
                shade = 255;
            }
            double fogFactor = Math.clamp(ray.t_hit / FOG_DISTANCE, 0.0, 1.0);

            int r, g, b;
            if (Arrays.equals(ray.hit_block,target_ray.hit_block)) {
                r = shade; g = 0; b = 0;
            } else {
                r = g = b = shade;
            }

            r = (int)(r * (1 - fogFactor) + 135 * fogFactor);
            g = (int)(g * (1 - fogFactor) + 206 * fogFactor);
            b = (int)(b * (1 - fogFactor) + 235 * fogFactor);

            rgb = (r << 16) | (g << 8) | b;
        } else {
            rgb = (SKY_COLOR_R << 16) | (SKY_COLOR_G << 8) | SKY_COLOR_B;
        }
        pixels[row * RENDER_WIDTH + col] = rgb;
    };

    public void drawRaysDDA(Graphics g) {
        // Find camera basis
        double angle_per_pixel = FOV_HORIZONTAL / RENDER_WIDTH;
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

        // Find ray start position
        double[] startPos = new double[] {player_x + POV_DIST * cb_fx, player_y + POV_DIST * cb_fy, player_z + POV_DIST * cb_fz};
        double[] startVoxelPos = new double[] {Math.floor(startPos[0]), Math.floor(startPos[1]), Math.floor(startPos[2])};

        IntStream.range(0, RENDER_HEIGHT).parallel().forEach(row -> {
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

                // Find ray hit and draw
                Ray ray = tlRay.get();
                traceRay(dir, startPos, startVoxelPos, ray);
                drawPixel(ray, row, col);
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
                String.format("FPS: %.1f  Frame: %.2fms", currentFps, frameTimeMs),
                String.format("Target: %d %d %d", target_ray.hit_block[0], target_ray.hit_block[1], target_ray.hit_block[2])
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

    public void updateTarget() {
        double[] playerPos = new double[] {player_x, player_y, player_z};
        double[] mapPos = new double[] {Math.floor(player_x), Math.floor(player_y), Math.floor(player_z)};
        CameraBasis cameraBasis = new CameraBasis(player_theta, player_epsilon);
        double[] dir = new double[3];
        dir[0] = cameraBasis.forward.x;
        dir[1] = cameraBasis.forward.y;
        dir[2] = cameraBasis.forward.z;
        Ray ray = new Ray();
        target_ray = traceRay(dir, playerPos, mapPos, ray);
    }

    // --------------------------
    // Event instances
    // --------------------------

    public void placeBlock() {
        if(target_ray.hit) world.placeBlock(target_ray, 1);
    }

    public void destroyBlock() {
        if(target_ray.hit) world.destroyBlock(target_ray);
    }

    // --------------------------
    // Key Adapter
    // --------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

}
