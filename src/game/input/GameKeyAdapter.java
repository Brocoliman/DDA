package game.input;

import game.Player;

import java.awt.event.KeyEvent;

import static game.Config.JUMP_STRENGTH;

public class GameKeyAdapter extends java.awt.event.KeyAdapter {
    private final Player player;

    public GameKeyAdapter(Player player) {
        this.player = player;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
                player.pdright = Math.clamp(player.pdright -1, -1, 0);
                break;
            case KeyEvent.VK_D:
                player.pdright = Math.clamp(player.pdright +1, 0, 1);
                break;
            case KeyEvent.VK_W:
                player.pdforward = Math.clamp(player.pdforward +1, 0, 1);
                break;
            case KeyEvent.VK_S:
                player.pdforward = Math.clamp(player.pdforward -1, -1, 0);
                break;
            case KeyEvent.VK_SPACE:
                if (player.grounded) {
                    player.zvel = JUMP_STRENGTH;
                    player.grounded = false;
                }
                break;
            case KeyEvent.VK_SHIFT:
                break;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_D:
                player.pdright = Math.clamp(player.pdright - 1, -1, 0);
                break;
            case KeyEvent.VK_A:
                player.pdright = Math.clamp(player.pdright + 1, 0, 1);
                break;
            case KeyEvent.VK_S:
                player.pdforward = Math.clamp(player.pdforward + 1, 0, 1);
                break;
            case KeyEvent.VK_W:
                player.pdforward = Math.clamp(player.pdforward - 1, -1, 0);
                break;
        }
    }
}