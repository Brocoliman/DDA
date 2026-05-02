package game;

import java.awt.event.MouseEvent;

public class GameMouseAdapter extends java.awt.event.MouseAdapter {
    private final GamePanel panel;

    public GameMouseAdapter(GamePanel panel) {
        this.panel = panel;
    }

    @Override
    public void mousePressed (MouseEvent e) {

        if (e.getButton() == MouseEvent.BUTTON1) { // left button = break
            panel.destroyBlock();
        }
        if (e.getButton() == MouseEvent.BUTTON3) { // right button = place
            panel.placeBlock();
        }
    }
}