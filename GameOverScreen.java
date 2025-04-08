import java.awt.*;
import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;

public class GameOverScreen extends JPanel {
    private JButton exitButton;
    private JButton newGameButton;
    private GamePanel gamePanel; // Reference to the main game panel
    private Font customFont;

    public GameOverScreen(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        setLayout(null);
        setBackground(Color.BLACK);

        // Load custom font
        loadCustomFont();

        newGameButton = new JButton("New Game");
        exitButton = new JButton("Exit");

        // Make buttons see-through with red text and custom font
        makeButtonTransparent(newGameButton);
        makeButtonTransparent(exitButton);

        newGameButton.addActionListener(e -> gamePanel.startNewGame());
        exitButton.addActionListener(e -> System.exit(0));

        add(newGameButton);
        add(exitButton);
    }

    private void loadCustomFont() {
        try {
            File fontFile = new File("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\font\\3.otf");
            customFont = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(fontFile)).deriveFont(60f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (Exception e) {
            System.out.println("Error loading custom font: " + e.getMessage());
        }
    }

    private void makeButtonTransparent(JButton button) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setForeground(Color.RED);
        if (customFont != null) {
            button.setFont(customFont.deriveFont(30f));
        }
    }

    public void showGameOverScreen() {
        setVisible(true);
        repaint();
        System.out.println("🎮 Showing game over screen...");
        MusicPlayer.playSound("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\music\\gameover.wav");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        g.setColor(Color.RED);
        if (customFont != null) {
            g.setFont(customFont);
        } else {
            g.setFont(new Font("Arial", Font.BOLD, 60));
        }

        String gameOverText = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(gameOverText);
        int textHeight = fm.getAscent();

        int textX = (panelWidth - textWidth) / 2;
        int textY = (panelHeight / 3);  // Slightly above center

        g.drawString(gameOverText, textX, textY);

        // Buttons below text
        int buttonWidth = 200;
        int buttonHeight = 50;
        int buttonX = (panelWidth - buttonWidth) / 2;

        newGameButton.setBounds(buttonX, textY + 40, buttonWidth, buttonHeight);
        exitButton.setBounds(buttonX, textY + 100, buttonWidth, buttonHeight);
    }
}