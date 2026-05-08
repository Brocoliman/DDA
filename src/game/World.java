package game;


import game.math.Ray;
import game.worldgen.WorldGenerator;

public class World {
    public Player player;
    public final int[][][] map;
    public final int voxels_x;
    public final int voxels_y;
    public final int voxels_z;

    final int seed;
    public double[] startPos;

    public World(int seed, int voxels_x, int voxels_y, int voxels_z) {
        this.seed = seed;
        this.voxels_x = voxels_x;
        this.voxels_y = voxels_y;
        this.voxels_z = voxels_z;
        this.map = new int[voxels_z][voxels_y][voxels_x];
    }

    public void bindPlayer(Player p) {
        player = p;
    }

    public void initialize() {
        WorldGenerator generator = new WorldGenerator(this, seed);
        generator.generate();
        startPos = generator.startPos;
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
        setBlock(voxel, block_type);
        if(player.wouldCollide(this, player.px, player.py, player.pz)){setBlock(voxel, 0);}
    }
    public void destroyBlock(Ray ray) {
        int[] voxel = ray.hit_block;
        setBlock(voxel, 0);
    }
    public void setBlock(int[] voxel, int block_type) {
        map[voxel[2]][voxel[1]][voxel[0]] = block_type;
    }
}