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
    // ALL RENDER settings
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
    public static final int WORLD_VOXELS_X = 8;
    public static final int WORLD_VOXELS_Y = 8;
    public static final int WORLD_VOXELS_Z = 8;
    public static final double VOXEL_SIZE = 1.0;  // world units per voxel edge

    // --------------------------
    // RENDERING tuning
    // --------------------------

    public static final int MAX_DDA_STEPS = WORLD_VOXELS_X + WORLD_VOXELS_Y + WORLD_VOXELS_Z + 2;
    public static final double FOG_DISTANCE = 20.0;     // world units at which fog reaches max
    public static final Color SKY_COLOR = new Color(135, 206, 235);

    // --------------------------
    // INPUT settings
    // --------------------------
    public static final double MOVE_SPEED = 3.0;        // world units per second
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
