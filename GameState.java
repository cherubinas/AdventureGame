import java.io.Serializable;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private int playerHealth;
    private int playerX, playerY;
    private List<Enemy.EnemyState> enemyStates;
    private List<Platform.PlatformState> platformStates; // ✅ New
    private boolean swordEquipped; // ✅ Add sword equipped state
    private boolean armorEquipped; // ✅ Add armor equipped state

    public GameState(int playerHealth, int playerX, int playerY,
                     List<Enemy.EnemyState> enemyStates,
                     List<Platform.PlatformState> platformStates, boolean swordEquipped, boolean armorEquipped) { // ✅ New
        this.playerHealth = playerHealth;
        this.playerX = playerX;
        this.playerY = playerY;
        this.enemyStates = enemyStates;
        this.platformStates = platformStates;

    }

    public int getPlayerHealth() { return playerHealth; }
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public List<Enemy.EnemyState> getEnemyPositions() { return enemyStates; }
    public List<Platform.PlatformState> getPlatformStates() { return platformStates; }
    public boolean isSwordEquipped() { return swordEquipped; } // ✅ Getter for sword state
    public boolean isArmorEquipped() { return armorEquipped; } // ✅
}
