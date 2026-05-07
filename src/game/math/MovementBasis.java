package game.math;

/*
CameraBasis is a camera SNAPSHOT
 */

public class MovementBasis {
    public final Vec3 forward;
    public final Vec3 right;
    public final Vec3 up;

    public MovementBasis(double theta) {
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
