package game;

import java.awt.event.KeyEvent;

public class GameKeyAdapter extends java.awt.event.KeyAdapter {
    private final GamePanel panel;

    public GameKeyAdapter(GamePanel panel) {
        this.panel = panel;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
                panel.player_dright = Math.clamp(panel.player_dright-1, -1, 0);
                break;
            case KeyEvent.VK_D:
                panel.player_dright = Math.clamp(panel.player_dright+1, 0, 1);
                break;
            case KeyEvent.VK_W:
                panel.player_dforward = Math.clamp(panel.player_dforward+1, 0, 1);
                break;
            case KeyEvent.VK_S:
                panel.player_dforward = Math.clamp(panel.player_dforward-1, -1, 0);
                break;
            case KeyEvent.VK_SPACE:
                panel.player_dup = Math.clamp(panel.player_dup+1, 0, 1);
                break;
            case KeyEvent.VK_SHIFT:
                panel.player_dup = Math.clamp(panel.player_dup-1, -1, 0);
                break;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_D:
                panel.player_dright = Math.clamp(panel.player_dright - 1, -1, 0);
                break;
            case KeyEvent.VK_A:
                panel.player_dright = Math.clamp(panel.player_dright + 1, 0, 1);
                break;
            case KeyEvent.VK_S:
                panel.player_dforward = Math.clamp(panel.player_dforward + 1, 0, 1);
                break;
            case KeyEvent.VK_W:
                panel.player_dforward = Math.clamp(panel.player_dforward - 1, -1, 0);
                break;
            case KeyEvent.VK_SHIFT:
                panel.player_dup = Math.clamp(panel.player_dup + 1, 0, 1);
                break;
            case KeyEvent.VK_SPACE:
                panel.player_dup = Math.clamp(panel.player_dup - 1, -1, 0);
                break;
        }
    }
}