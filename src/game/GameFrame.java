package game;
import javax.swing.*;

public class GameFrame extends JFrame {
    GameFrame(){
        this.add(new GamePanel());

        this.setTitle("Project 1000C");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack(); // fit JFrame around every component
        this.setVisible(true);
        this.setLocationRelativeTo(null); // appear middle of computer
    }
}