import javax.swing.*;

public class GameWindow {
    public void startGame() {
        JFrame gameFrame = new JFrame("Game Screen");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(600, 400);
        gameFrame.setLocationRelativeTo(null);

        GamePanel gamePanel = new GamePanel();
        gameFrame.add(gamePanel);
        gameFrame.setVisible(true);
    }
}
