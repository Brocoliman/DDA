package game;

/*
CameraBasis is a camera SNAPSHOT
 */

public class MovementBasis {
    final Vec3 forward;
    final Vec3 right;
    final Vec3 up;

    MovementBasis(double theta) {
        this.forward = new Vec3(
                Math.cos(theta),
                Math.sin(theta),
                0);
        this.right = new Vec3(
                Math.sin(theta),
                -Math.cos(theta),
                0
        );
        this.up = new Vec3(0,0,1);
    }
}
