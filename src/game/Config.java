package game;
import java.awt.*;

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
    public static final int RENDER_WIDTH = 256 * 4;
    public static final int RENDER_HEIGHT = 192 * 4;
    public static final int DELAY = 8; // FPS

    // --------------------------
    // ALL RAYCAST settings
    // --------------------------
    public static final double FOV_HORIZONTAL = Math.toRadians(70);
    public static final int POV_DIST = -0; // distance behind camera to start light beams

    // --------------------------
    // ALL WORLD settings
    // --------------------------
    public static final int WORLD_VOXELS_X = 32;
    public static final int WORLD_VOXELS_Y = 32;
    public static final int WORLD_VOXELS_Z = 16;
    public static final int WORLD_SEED = 53;

    // --------------------------
    // RENDERING tuning
    // --------------------------
    public static final int MAX_DDA_STEPS = WORLD_VOXELS_X + WORLD_VOXELS_Y + WORLD_VOXELS_Z + 2;
    public static final double MAX_DDA_TIME = 50.0;
    public static final double FOG_DISTANCE = 50.0;     // world units at which fog reaches max
    public static final int SKY_COLOR_R = 135;
    public static final int SKY_COLOR_G = 206;
    public static final int SKY_COLOR_B = 235;

    // --------------------------
    // INPUT settings
    // --------------------------
    public static final double MOVE_SPEED = 7.0;        // world units per second
    public static final double MOUSE_SENSITIVITY_X = 0.003;  // radians per pixel of mouse motion
    public static final double MOUSE_SENSITIVITY_Y = 0.003;

    // --------------------------
    // INITIAL states
    // --------------------------

    public static final double START_X = 4.0;
    public static final double START_Y = 4.0;
    public static final double START_Z = 4.0;
    public static final double START_THETA = 0.0;
    public static final double START_EPSILON = 0.0;
}
