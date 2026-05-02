package game;

public class Vec3 {
    public final double x, y, z;

    public Vec3(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    public Vec3 add(Vec3 other) { return new Vec3(x + other.x, y + other.y, z + other.z); }
    public Vec3 scale(double s)  { return new Vec3(x * s, y * s, z * s); }
    public double dot(Vec3 other) { return x*other.x + y*other.y + z*other.z; }
    public Vec3 cross(Vec3 other) {
        return new Vec3(y*other.z - z*other.y,
                z*other.x - x*other.z,
                x*other.y - y*other.x);
    }
    public double length() { return Math.sqrt(x*x + y*y + z*z); }
    public Vec3 normalize() { double L = length(); return new Vec3(x/L, y/L, z/L); }
    public double get(int index) {return index == 0 ? x : (index == 1 ? y : z); }

    @Override
    public String toString() { return String.format("(%.3f, %.3f, %.3f)", x, y, z); }
}