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

    public World(int seed, int voxels_x, int voxels_y, int voxels_z) {
        this.seed = seed;
        this.voxels_x = voxels_x;
        this.voxels_y = voxels_y;
        this.voxels_z = voxels_z;
        this.map = new int[voxels_z][voxels_y][voxels_x];

        this.random = new Random();
    }

    public void initialize() {
        for (int y_unit = 0; y_unit < voxels_y; y_unit++) {
            for (int x_unit = 0; x_unit < voxels_x; x_unit++) {
                double n = noise2(seed, x_unit*0.02, y_unit*0.02) * 1.0          // big mountains
                        + noise2(seed, x_unit*0.08, y_unit*0.08) * 0.4          // medium hills
                        + noise2(seed, x_unit*0.32, y_unit*0.32) * 0.05;        // small bumps
                int height = (int) ((n+1)*0.5*voxels_z);
                for (int z_unit = 0; z_unit < height; z_unit++) {
                    map[z_unit][y_unit][x_unit] = 1;
                }
            }
        }
    }
    public boolean isSolid(double x, double y, double z) {
        int ix = (int)Math.floor(x);
        int iy = (int)Math.floor(y);
        int iz = (int)Math.floor(z);
        if (ix < 0 || iy < 0 || iz < 0 ||
                ix >= voxels_x || iy >= voxels_y || iz >= voxels_z) {
            return false;  // outside world = not solid (player can fly out)
        }
        return map[iz][iy][ix] != 0;
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