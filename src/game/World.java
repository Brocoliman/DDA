package game;

import java.util.Random;

import static game.Config.*;
import static game.OpenSimplex2.noise2;

public class World {
    final int[][][] map;
    final int voxels_x;
    final int voxels_y;
    final int voxels_z;

    final Random random;
    final int seed;
    public double[] playerStartPos;

    public World(int seed, int voxels_x, int voxels_y, int voxels_z) {
        this.seed = seed;
        this.voxels_x = voxels_x;
        this.voxels_y = voxels_y;
        this.voxels_z = voxels_z;
        this.map = new int[voxels_z][voxels_y][voxels_x];

        this.random = new Random();
    }

    public void initialize() {
        playerStartPos = new double[3];
        playerStartPos[0] = (int)((Math.clamp(random.nextGaussian(), -1, 1)+1)/2*voxels_x);
        playerStartPos[1] = (int)((Math.clamp(random.nextGaussian(), -1, 1)+1)/2*voxels_y);
        for (int y_unit = 0; y_unit < voxels_y; y_unit++) {
            for (int x_unit = 0; x_unit < voxels_x; x_unit++) {
                double n = noise2(seed, x_unit*0.02, y_unit*0.02) * 0.55
                        + noise2(seed, x_unit*0.08, y_unit*0.08) * 0.4
                        + noise2(seed, x_unit*0.32, y_unit*0.32) * 0.05;
                int height = (int) ((n+1)*0.5*voxels_z);
                if (x_unit==playerStartPos[0] && y_unit==playerStartPos[1]) playerStartPos[2] = height+1+BB_VERTICAL_DOWN;
                for (int z_unit = 0; z_unit < height; z_unit++) {
                    map[z_unit][y_unit][x_unit] = 1;
                }
            }
        }
    }
    public boolean isSolid(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 ||
                x >= voxels_x || y >= voxels_y || z >= voxels_z) {
            return false;  // outside world = not solid (player can fly out)
        }
        return map[z][y][x] != 0;
    }
    public void placeBlock(Ray ray, int block_type) {
        int[] voxel = ray.hit_block;
        int axis = ray.axis;
        double[] dir = ray.dir;
        voxel[axis] -= (dir[axis] > 0) ? 1:-1;
        map[voxel[2]][voxel[1]][voxel[0]] = block_type;
    }
    public void destroyBlock(Ray ray) {
        int[] voxel = ray.hit_block;
        map[voxel[2]][voxel[1]][voxel[0]] = 0;
    }
}