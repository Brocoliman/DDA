package game;
import game.input.GameKeyAdapter;
import game.input.GameMouseAdapter;
import game.math.CameraBasis;
import game.math.Ray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import static game.Config.*;

public class GamePanel extends JPanel implements ActionListener {
    // Dependencies
    private final World world;
    private final Renderer renderer;
    private final Player player;

    // Timing
    Timer timer;
    long lastFrameNanos;    // for mouse
    long lastFpsNanos;      // for record fps
    double currentFps;      // for record fps
    double frameTimeMs;     // for record fps

    // Input
    boolean firstMouseRead;
    Robot robot;

    // --------------------------
    // Initialization
    // --------------------------

    GamePanel() {

        this.world = new World(WORLD_SEED, WORLD_VOXELS_X, WORLD_VOXELS_Y, WORLD_VOXELS_Z);
        world.initialize();
        this.renderer = new Renderer(world);
        this.player = new Player(world);

        this.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT)); // minimum size for the component to display correctly
        this.setFocusable(true);
        this.addKeyListener(new GameKeyAdapter(player));
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
        lastFrameNanos = System.nanoTime();
        lastFpsNanos = System.nanoTime();
        currentFps = 0;
        firstMouseRead = true;

        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        update(g);
    }

    public void update(Graphics g) {
        updateMovement();
        updateTarget();
        renderer.render(g, player);
        renderer.drawDebugInfo(g, player, this);

        long now = System.nanoTime();
        frameTimeMs = (now - lastFpsNanos) / 1e6;
        double instantFps = 1000.0 / frameTimeMs;
        currentFps = currentFps * 0.9 + instantFps * 0.1;
        lastFpsNanos = now;
    }

    // --------------------------
    // Event cycles
    // --------------------------

    public void updateMovement() {
        long now = System.nanoTime();
        double dt = (now - lastFrameNanos) / 1e9;  // seconds
        lastFrameNanos = now;

        // Player movement
        player.update(dt, world);

        // Apply mouse movements update
        Point window_topleft = this.getLocationOnScreen();
        int window_centerx = window_topleft.x + WINDOW_WIDTH/2;
        int window_centery = window_topleft.y + WINDOW_HEIGHT/2;
        if (!firstMouseRead) {
            Point mouse_pxy = MouseInfo.getPointerInfo().getLocation();
            int delta_x = mouse_pxy.x - window_centerx;
            int delta_y = mouse_pxy.y - window_centery;
            player.theta -= delta_x * MOUSE_SENSITIVITY_X;
            player.eps = Math.clamp(player.eps-delta_y * MOUSE_SENSITIVITY_Y, -Math.PI/2, Math.PI/2);
            robot.mouseMove(window_centerx,window_centery);
        }
        firstMouseRead = false;
    }

    public void updateTarget() {
        double[] playerPos = new double[] {player.px, player.py, player.pz};
        double[] mapPos = new double[] {Math.floor(player.px), Math.floor(player.py), Math.floor(player.pz)};
        CameraBasis cameraBasis = new CameraBasis(player.theta, player.eps);
        double[] dir = new double[3];
        dir[0] = cameraBasis.forward.x;
        dir[1] = cameraBasis.forward.y;
        dir[2] = cameraBasis.forward.z;
        Ray ray = new Ray();
        player.targetRay = renderer.traceRay(dir, playerPos, mapPos, MAX_DDA_TIME_TARGET, ray);
    }

    // --------------------------
    // Event instances
    // --------------------------

    public void placeBlock() {
        if(player.targetRay.hit) world.placeBlock(player.targetRay, 1);
    }

    public void destroyBlock() {
        if(player.targetRay.hit) world.destroyBlock(player.targetRay);
    }

    // --------------------------
    // Key Adapter
    // --------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

}
