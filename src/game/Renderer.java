package game;

import game.math.CameraBasis;
import game.math.Ray;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.stream.IntStream;

import static game.Config.*;

public class Renderer {

    private final World world;

    // Frame buffer
    private final BufferedImage frame;
    private final int[] pixels;

    // Thread caches
    private static final ThreadLocal<Ray> tlRay = ThreadLocal.withInitial(Ray::new);
    private static final ThreadLocal<double[]> tlT = ThreadLocal.withInitial(() -> new double[3]);
    private static final ThreadLocal<double[]> tlDt = ThreadLocal.withInitial(() -> new double[3]);
    private static final ThreadLocal<double[]> tlM = ThreadLocal.withInitial(() -> new double[3]);
    private static final ThreadLocal<double[]> tlDm = ThreadLocal.withInitial(() -> new double[3]);

    public Renderer(World world) {
        this.world = world;
        this.frame = new BufferedImage(RENDER_WIDTH, RENDER_HEIGHT, BufferedImage.TYPE_INT_RGB);
        this.pixels = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
    }

    // Raycasting
    public Ray traceRay(double[] dir, double[] startPos, double[] startVoxelPos, double maxTime, Ray out) {
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
            if (t_hit > maxTime) break;

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

        out.dir = dir;
        out.hit = hit;
        out.t_hit = t_hit;
        out.axis = axis;
        out.hit_block[0] = (int)m[0];
        out.hit_block[1] = (int)m[1];
        out.hit_block[2] = (int)m[2];
        out.hit_point[0] = startPos[0] + dir[0] * t_hit;
        out.hit_point[1] = startPos[1] + dir[1] * t_hit;
        out.hit_point[2] = startPos[2] + dir[2] * t_hit;
        return out;

    }

    // Rendering
    public void drawPixel(Ray ray, int row, int col, Ray targetRay) {
        int rgb;
        if (ray.hit) {
            // Determine lightness
            double lightness;
            if (ray.axis == 0) {
                lightness = 170.0/255;
            } else if (ray.axis == 1) {
                lightness = 213.0/255;
            } else {
                lightness = 255.0/255;
            }

            // Determine fog factor
            double fogFactor = Math.clamp(ray.t_hit / FOG_DISTANCE, 0.0, 1.0);

            // Determine outline
            boolean isOutline = false;
            if (Arrays.equals(ray.hit_block, targetRay.hit_block)) {
                for (int axis = 0; axis < 3; axis++) {
                    if (axis == ray.axis) continue;
                    double local = ray.hit_point[axis] - ray.hit_block[axis];
                    if (local < EDGE_WIDTH || local > 1.0 - EDGE_WIDTH) {
                        isOutline = true;
                        break;
                    }
                }
            }

            // Determine block type
            Block block = Block.byId(world.map[ray.hit_block[2]][ray.hit_block[1]][ray.hit_block[0]]);
            int r, g, b;

            if (isOutline) {
                r = 0; g = 0; b = 0;
            } else {
                r = (int)(lightness * block.color.getRed());
                g = (int)(lightness * block.color.getGreen());
                b = (int)(lightness * block.color.getBlue());
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

    public void render(Graphics g, Player p) {
        // Extract variables
        double px = p.px;
        double py = p.py;
        double pz = p.pz;
        double theta = p.theta;
        double epsilon = p.eps;
        Ray targetRay = p.targetRay;

        // Find camera basis
        double angle_per_pixel = FOV_HORIZONTAL / RENDER_WIDTH;
        CameraBasis cameraBasis = new CameraBasis(theta, epsilon);
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
        double[] startPos = new double[] {px + POV_DIST * cb_fx, py + POV_DIST * cb_fy, pz + POV_DIST * cb_fz};
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
                traceRay(dir, startPos, startVoxelPos, MAX_DDA_TIME_RENDER, ray);
                drawPixel(ray, row, col, targetRay);
            }
        });

        // Blit
        g.drawImage(frame, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);
    }

    public void drawDebugInfo(Graphics g, Player p, GamePanel panel) {
        // Extract variables
        double px = p.px;
        double py = p.py;
        double pz = p.pz;
        double pzv = p.zvel;
        double theta = p.theta;
        double epsilon = p.eps;
        Ray targetRay = p.targetRay;

        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        String[] lines = {
                String.format("X: %.2f  Y: %.2f  Z: %.2f ZVel: %.2f Seed: %d", px, py, pz, pzv, WORLD_SEED),
                String.format("Theta: %.2f  Epsilon: %.2f", Math.toDegrees(theta), Math.toDegrees(epsilon)),
                String.format("FPS: %.1f", panel.currentFps),
                String.format("Target: %d %d %d Type: %s", targetRay.hit_block[0], targetRay.hit_block[1], targetRay.hit_block[2],
                        targetRay.hit?(Block.byId(world.map[targetRay.hit_block[2]][targetRay.hit_block[1]][targetRay.hit_block[0]]).name):"Air")
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
}
