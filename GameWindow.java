import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import javafx.embed.swing.JFXPanel; // Make sure to import this!

public class GameWindow {
    private GamePanel gamePanel;
    private JFrame gameFrame;
    private  static  MusicPlayer musicPlayer;

    // Start a new game from scratch
    public void startGame() {
        // ✅ Initialize JavaFX (only once)
        new JFXPanel();

        // ✅ Start background music
       // musicPlayer = new MusicPlayer("C:/Users/eveli/Desktop/New folder (3)/AdventureGame/lib/music/kim-lightyear-leave-the-world-tonight-chiptune-edit-loop-132102.mp3");
        //musicPlayer.play();

        gameFrame = new JFrame("Game Screen");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(1200, 600);
        gameFrame.setLocationRelativeTo(null);

        gamePanel = new GamePanel();
        gameFrame.add(gamePanel);

        gameFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveGameProgress();
            }
        });

        gameFrame.setVisible(true);
    }

    // Load a saved game
    public void loadGame(GameState savedState) {

        gameFrame = new JFrame("Game Screen");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(800, 600);
        gameFrame.setLocationRelativeTo(null);

        gamePanel = new GamePanel();

        if (savedState != null) {
            gamePanel.getPlayer().setHealth(savedState.getPlayerHealth());
            gamePanel.getPlayer().setPosition(savedState.getPlayerX(), savedState.getPlayerY());
            gamePanel.loadEnemies(savedState.getEnemyPositions());
        }

        gameFrame.add(gamePanel);

        // 🆕 Start music just like in startGame
        new JFXPanel();
        musicPlayer = new MusicPlayer("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\music\\kim-lightyear-leave-the-world-tonight-chiptune-edit-loop-132102.mp3");
        musicPlayer.play();

        gameFrame.setVisible(true);

    }
    // Save the game progress
    public void saveGameProgress() {
        if (gamePanel != null) {
            GameState gameState = new GameState(
                    gamePanel.getPlayer().getHealth(),
                    gamePanel.getPlayer().getX(),
                    gamePanel.getPlayer().getY(),
                    extractEnemyPositions(gamePanel.getEnemies())
            );
            SaveManager.saveGame(gameState);
        }
    }

    // Helper method to extract enemy positions
    public List<Enemy.EnemyState> extractEnemyPositions(List<Enemy> enemies) {
        List<Enemy.EnemyState> enemyStates = new ArrayList<>();
        for (Enemy enemy : enemies) {
            enemyStates.add(enemy.getState());
        }
        return enemyStates;
    }
}
