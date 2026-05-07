package game;

import game.math.MovementBasis;
import game.math.Ray;
import game.math.Vec3;

import static game.Config.*;
import static game.Config.DELAY;
import static game.Config.TERM_ZVEL;
import static game.Config.WALK_SPEED;

public class Player {
    public double theta;                        // azimuth, radians
    public double eps;                      // elevation, radians, in (-π/2, +π/2)
    public double px, py, pz;        // position in world units
    public double pdforward, pdright;
    public double zvel;
    public boolean grounded;
    public Ray targetRay;

    Player(World world) {
        this.theta = START_THETA;
        this.eps = START_EPSILON;
        this.px = world.startPos[0];
        this.py = world.startPos[1];
        this.pz = world.startPos[2];
        this.zvel = 0;
        this.grounded = false;
    }

    public void update(double dt, World world) {
        // Gravity
        zvel -= GRAVITY * DELAY/1000;
        zvel = Math.max(TERM_ZVEL, zvel);

        // Movement
        MovementBasis movementBasis = new MovementBasis(theta);
        Vec3 player_dxyz = movementBasis.forward.scale(pdforward)
                .add(movementBasis.right.scale(pdright));
        double new_x = px + player_dxyz.x * dt * WALK_SPEED;
        double new_y = py + player_dxyz.y * dt * WALK_SPEED;
        double new_z = pz + zvel * dt;

        // Collision
        if (!wouldCollide(world, new_x, py, pz)) px = new_x;
        if (!wouldCollide(world, px, new_y, pz)) py = new_y;
        if (!wouldCollide(world, px, py, new_z)) {
            pz = new_z;
        } else {
            if (zvel < 0) grounded = true;  // landed on something
            zvel = 0;
        }
    }

    public boolean wouldCollide(World world, double x, double y, double z) {
        int min_voxel_x = (int)Math.floor(x-BB_HORIZONTAL_HALF);
        int max_voxel_x = (int)Math.floor(x+BB_HORIZONTAL_HALF);
        int min_voxel_y = (int)Math.floor(y-BB_HORIZONTAL_HALF);
        int max_voxel_y = (int)Math.floor(y+BB_HORIZONTAL_HALF);
        int min_voxel_z = (int)Math.floor(z-BB_VERTICAL_DOWN);
        int max_voxel_z = (int)Math.floor(z+BB_VERTICAL_UP);
        boolean collide = false;
        for (int voxel_x = min_voxel_x; voxel_x <= max_voxel_x; voxel_x++) {
            for (int voxel_y = min_voxel_y; voxel_y <= max_voxel_y; voxel_y++) {
                for (int voxel_z = min_voxel_z; voxel_z <= max_voxel_z; voxel_z++) {
                    if (world.isSolid(voxel_x, voxel_y, voxel_z)) {
                        collide = true;
                        break;
                    }
                }
            }
        }
        return collide;
    }
}
