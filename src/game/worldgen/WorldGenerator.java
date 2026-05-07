package game.worldgen;

import game.World;
import game.math.OpenSimplex2;

import java.util.Random;

import static game.Config.BB_VERTICAL_DOWN;
import static game.Config.MIN_TERRAIN_HEIGHT;
import static game.math.OpenSimplex2.noise2;

public class WorldGenerator {
    private final World world;
    private final int seed;
    private final Random random;

    final int voxels_x;
    final int voxels_y;
    final int voxels_z;
    final int[][][] map;

    public double[] startPos;

    public WorldGenerator(World world, int seed) {
        this.world = world;
        this.seed = seed;
        this.random = new Random(seed);
        this.voxels_x = world.voxels_x;
        this.voxels_y = world.voxels_y;
        this.voxels_z = world.voxels_z;
        this.map = world.map;
    }

    public void generate() {
        generateTerrain();
        carveCaves();
        placePlayerSpawn();
    }

    private void generateTerrain() {
        startPos = new double[3];
        startPos[0] = (int)((Math.clamp(random.nextGaussian(), -1, 1)+1)/2*voxels_x);
        startPos[1] = (int)((Math.clamp(random.nextGaussian(), -1, 1)+1)/2*voxels_y);

        // Heightmap loop
        for (int y_unit = 0; y_unit < voxels_y; y_unit++) {
            for (int x_unit = 0; x_unit < voxels_x; x_unit++) {
                double warpStrength = 3;  // how far to perturb (in world units)
                double warpScale = 0.05;     // frequency of the warp itself
                double warpX = noise2(seed + 1, x_unit * warpScale, y_unit * warpScale) * warpStrength;
                double warpY = noise2(seed + 2, x_unit * warpScale, y_unit * warpScale) * warpStrength;

                double wx = x_unit + warpX;
                double wy = y_unit + warpY;

                double n = noise2(seed, wx*0.02, wy*0.02) * 0.55    // large rolling hills
                        + noise2(seed, wx*0.08, wy*0.08) * 0.4      // medium detail
                        + noise2(seed, wx*0.32, wy*0.32) * 0.05;    // small bumps

                int height = MIN_TERRAIN_HEIGHT + (int)((n+1)*0.5*(voxels_z-MIN_TERRAIN_HEIGHT));
                if (x_unit== startPos[0] && y_unit== startPos[1]) startPos[2] = height+1+BB_VERTICAL_DOWN;
                for (int z_unit = 0; z_unit < height; z_unit++) {
                    this.map[z_unit][y_unit][x_unit] = 1;
                }
            }
        }
    }

    private void carveCaves() {
        // 3D noise carving
        // Cave generation
        double caveScale = 0.1;
        double caveThreshold = 0.55;

        for (int z = 0; z < voxels_z; z++) {
            for (int y = 0; y < voxels_y; y++) {
                for (int x = 0; x < voxels_x; x++) {
                    if (map[z][y][x] == 0) continue;  // skip air
                    double caveNoise = OpenSimplex2.noise3_ImproveXY(seed + 3, x * caveScale, y * caveScale, z * caveScale);
                    if (caveNoise > caveThreshold) {
                        map[z][y][x] = 0;  // carve
                    }
                }
            }
        }
    }


    private void placePlayerSpawn() {
        // random start position
    }
}