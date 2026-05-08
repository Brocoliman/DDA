package game;

public final class Config {
    private Config() {}
    // --------------------------
    // Window settings
    // --------------------------
    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;

    // --------------------------
    // RENDER display
    // --------------------------
    public static final int RENDER_WIDTH = 256 * 2;
    public static final int RENDER_HEIGHT = 192 * 2;
    public static final int DELAY = 8;                              // 1000/FPS
    public static final double EDGE_WIDTH = 0.02;

    // --------------------------
    // ALL WORLD settings
    // --------------------------
    public static final int WORLD_VOXELS_X = 128;
    public static final int WORLD_VOXELS_Y = 128;
    public static final int WORLD_VOXELS_Z = 64;
    public static final int WORLD_SEED = (int)(Math.random() * Integer.MAX_VALUE);;
    public static final int MIN_TERRAIN_HEIGHT = WORLD_VOXELS_Z/2;

    // --------------------------
    // ALL RAYCAST settings
    // --------------------------
    public static final int MAX_DDA_STEPS = WORLD_VOXELS_X + WORLD_VOXELS_Y + WORLD_VOXELS_Z + 2;
    public static final double MAX_DDA_TIME_RENDER = 50.0;
    public static final double MAX_DDA_TIME_TARGET = 10.0;
    public static final double FOV_HORIZONTAL = Math.toRadians(70);
    public static final int POV_DIST = -0;                          // distance behind camera to start light beams

    // --------------------------
    // RENDERING tuning
    // --------------------------
    public static final double FOG_DISTANCE = 50.0;                 // world units at which fog reaches max
    public static final int SKY_COLOR_R = 135;
    public static final int SKY_COLOR_G = 206;
    public static final int SKY_COLOR_B = 235;

    // --------------------------
    // INPUT settings
    // --------------------------
    public static final double WALK_SPEED = 3.0;                    // world units per second
    public static final double JUMP_STRENGTH = 9.8;                 // world units per second
    public static final double GRAVITY = 5*9.8;                     // world units per second^2
    public static final double TERM_ZVEL = -12;                     // world units per second
    public static final double BB_HORIZONTAL_HALF = 0.3;            // bounding box half width&length
    public static final double BB_VERTICAL_DOWN = 1.5;              // bounding box extend downwards
    public static final double BB_VERTICAL_UP = 0.3;                // bounding box extend upwards
    public static final double MOUSE_SENSITIVITY_X = 0.003;         // radians per pixel of mouse motion
    public static final double MOUSE_SENSITIVITY_Y = 0.003;
}
