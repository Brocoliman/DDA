package game;

public record Ray (double[] dir, boolean hit, double t_hit, int axis, int[] hit_block) {};
