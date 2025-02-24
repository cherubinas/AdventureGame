import javax.swing.*;

public class GameMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu().showMainMenu());
    }
}
