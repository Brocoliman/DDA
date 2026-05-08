package game;
import java.awt.Color;

public enum Block {
    AIR(0, "Air", null, false),
    STONE(1, "Stone", new Color(120, 120, 120), true),
    DIRT(2, "Dirt", new Color(120, 80, 40), true),
    GRASS(3, "Grass", new Color(80, 160, 60), true),
    WATER(4, "Water", new Color(60, 100, 200), false);

    public final int id;
    public final String name;
    public final Color color;
    public final boolean solid;

    Block(int id, String name, Color color, boolean solid) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.solid = solid;
    }

    private static final Block[] BY_ID = new Block[256];
    static {
        for (Block b : values()) BY_ID[b.id] = b;
    }

    public static Block byId(int id) { return BY_ID[id]; }
}