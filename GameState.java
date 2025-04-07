import java.io.Serializable;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    private int playerHealth;
    private int playerX, playerY;
    private List<Enemy.EnemyState> enemyStates; // Store full enemy states now

    public GameState(int playerHealth, int playerX, int playerY, List<Enemy.EnemyState> enemyStates) {
        this.playerHealth = playerHealth;
        this.playerX = playerX;
        this.playerY = playerY;
        this.enemyStates = enemyStates;
    }

    public int getPlayerHealth() { return playerHealth; }
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public List<Enemy.EnemyState> getEnemyPositions() { return enemyStates; } // Rename for clarity
}