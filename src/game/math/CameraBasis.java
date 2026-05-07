package game.math;
import java.lang.Math;

/*
CameraBasis is a camera SNAPSHOT
 */

public class CameraBasis {
    public final Vec3 forward;
    public final Vec3 right;
    public final Vec3 up;

    public CameraBasis(double theta, double epsilon) {
        this.forward = new Vec3(
                Math.cos(theta)*Math.cos(epsilon),
                Math.sin(theta)*Math.cos(epsilon),
                Math.sin(epsilon));
        this.right = new Vec3(
                Math.sin(theta),
                -Math.cos(theta),
                0
        );
        this.up = this.right.cross(this.forward);
    }
}
