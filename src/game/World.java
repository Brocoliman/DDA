package game;

import static game.Config.*;

public class World {
    final int[][][] map;

    public World(int[][][] map) { this.map = map; }

    public static boolean isInMap(double[] arr) {
        return (-1 < arr[0]) && (arr[0] < WORLD_VOXELS_X) &&
                (-1 < arr[1]) && (arr[1] < WORLD_VOXELS_Y) &&
                (-1 < arr[2]) && (arr[2] < WORLD_VOXELS_Z);
    }
    public void placeBlock(Ray ray, int block_type) {
        int[] voxel = ray.hit_block();
        int axis = ray.axis();
        double[] dir = ray.dir();
        voxel[axis] -= (dir[axis] > 0) ? 1:-1;
        map[voxel[2]][voxel[1]][voxel[0]] = block_type;
    }
    public void destroyBlock(Ray ray) {
        int[] voxel = ray.hit_block();
        map[voxel[2]][voxel[1]][voxel[0]] = 0;
    }
}