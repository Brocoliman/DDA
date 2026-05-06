package game;

public class Ray {
    public double[] dir;
    public boolean hit;
    public double t_hit;
    public int axis;
    public int[] hit_block = new int[3];

    public Ray() {}
}