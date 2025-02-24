import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;

public class MainMenu {
    private Font customFont;

    public void showMainMenu() {
        JFrame frame = new JFrame("Dungeons Forsaken");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(811, 456);
        frame.setLocationRelativeTo(null);

        // Load custom font
        loadCustomFont();

        // Create title banner with shadow
        ShadowLabel titleLabel = new ShadowLabel("Dungeons Forsaken", JLabel.LEFT);
        if (customFont != null) {
            titleLabel.setFont(customFont.deriveFont(65.0f)); // Increase custom font size
        } else {
            titleLabel.setFont(new Font("Serif", Font.BOLD, 50)); // Increase default font size
        }
        titleLabel.setForeground(new Color(247, 247, 245));
        titleLabel.setShadowColor(Color.GRAY);
        titleLabel.setShadowOffset(2, 4); // X offset 2, Y offset 4

        // Background setup
        ImageIcon backgroundImage = new ImageIcon("AdventureGame/lib/photos/bg.gif");
        BackgroundPanel background = new BackgroundPanel(backgroundImage.getImage());
        background.setLayout(new GridBagLayout());

        // Main panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(-2, 10, -2, 10); // Reduce space between buttons
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Buttons
        JButton startButton = createTextButton("Start a new game");
        JButton continueButton = createTextButton("Continue");
        JButton rulesButton = createTextButton("Rules");
        JButton settingsButton = createTextButton("Settings");
        JButton exitButton = createTextButton("Exit");

        // Button actions
        startButton.addActionListener(e -> {
            frame.dispose();
            new GameWindow().startGame();
        });

        exitButton.addActionListener(e -> System.exit(0));
        rulesButton.addActionListener(e -> showRulesPage());

        // Add buttons to panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(startButton, gbc);
        gbc.gridy++;
        buttonPanel.add(continueButton, gbc);
        gbc.gridy++;
        buttonPanel.add(rulesButton, gbc);
        gbc.gridy++;
        buttonPanel.add(settingsButton, gbc);
        gbc.gridy++;
        buttonPanel.add(exitButton, gbc);

        // Add title and button panel to background
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; // Align text and buttons to the left
        gbc.insets = new Insets(10, 50, 10, 10); // Adjust spacing
        background.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 300); // Fine-tune button positioning under "D"
        background.add(buttonPanel, gbc);

        // Set frame content
        frame.setContentPane(background);
        frame.setVisible(true);
    }

    private JButton createTextButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        
        if (customFont != null) {
            button.setFont(customFont.deriveFont(22.0f));
        } else {
            button.setFont(new Font("Arial", Font.BOLD, 40));
        }

        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.setHorizontalAlignment(SwingConstants.LEFT); // Align text inside button to the left

        return button;
    }

    private void loadCustomFont() {
        try {
            File fontFile = new File("AdventureGame/lib/font/3.otf"); // Replace with your font's path
            customFont = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(fontFile));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont); // Register the font system-wide
        } catch (Exception e) {
            System.out.println("Error loading custom font: " + e.getMessage());
        }
    }

    private void showRulesPage() {
        JFrame rulesFrame = new JFrame("Rules");
        rulesFrame.setSize(400, 300);
        rulesFrame.setLocationRelativeTo(null);
        rulesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel rulesPanel = new JPanel();
        rulesPanel.setBackground(Color.BLACK);
        rulesPanel.setLayout(new BorderLayout());

        JLabel rulesLabel = new JLabel("<html><div style='text-align: center;'>Rules:<br>1. Survive.<br>2. Defeat enemies.<br>3. Find treasures!</div></html>", JLabel.CENTER);
        if (customFont != null) {
            rulesLabel.setFont(customFont.deriveFont(20.0f)); // Use custom font
        } else {
            rulesLabel.setFont(new Font("Arial", Font.PLAIN, 20)); // Default font
        }
        rulesLabel.setForeground(Color.WHITE);

        // OK button to close the rules frame
        JButton okButton = new JButton("OK");
        if (customFont != null) {
            okButton.setFont(customFont.deriveFont(16.0f));
        } else {
            okButton.setFont(new Font("Arial", Font.BOLD, 16));
        }
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.addActionListener(e -> rulesFrame.dispose()); // Close the rules frame

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);

        rulesPanel.add(rulesLabel, BorderLayout.CENTER);
        rulesPanel.add(buttonPanel, BorderLayout.SOUTH);

        rulesFrame.add(rulesPanel);
        rulesFrame.setVisible(true);
    }

    // Custom JPanel to draw the background image
    class BackgroundPanel extends JPanel {
        private Image image;

        public BackgroundPanel(Image image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // Custom JLabel to draw text with shadow
    class ShadowLabel extends JLabel {
        private Color shadowColor = Color.GRAY;
        private int shadowOffsetX = 2;
        private int shadowOffsetY = 5;

        public ShadowLabel(String text, int horizontalAlignment) {
            super(text, horizontalAlignment);
        }

        public void setShadowColor(Color shadowColor) {
            this.shadowColor = shadowColor;
        }

        public void setShadowOffset(int x, int y) {
            this.shadowOffsetX = x;
            this.shadowOffsetY = y;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(shadowColor);
            g2.drawString(getText(), getInsets().left + shadowOffsetX, getInsets().top + g2.getFontMetrics().getAscent() + shadowOffsetY);
            g2.setColor(getForeground());
            g2.drawString(getText(), getInsets().left, getInsets().top + g2.getFontMetrics().getAscent());
            g2.dispose();
        }
    }
}
