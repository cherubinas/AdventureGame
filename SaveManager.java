import java.io.*;

public class SaveManager {
    private static final String SAVE_FILE = "game_save.dat";

    // Save game state
    public static void saveGame(GameState gameState) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(gameState);
            System.out.println("Game saved successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load game state
    public static GameState loadGame() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            return (GameState) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No saved game found!");
            return null;
        }
    }
}
