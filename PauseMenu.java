import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;

public class PauseMenu extends JPanel {
    private JButton saveButton;
    private JButton exitButton;
    private JButton resumeButton;
    private Font customFont;
    private Runnable saveAction, exitAction, resumeAction;

    public PauseMenu(JFrame frame, Runnable saveAction, Runnable exitAction, Runnable resumeAction) {
        this.saveAction = saveAction;
        this.exitAction = exitAction;
        this.resumeAction = resumeAction;

        setLayout(null);
        setBackground(Color.BLACK);
        loadCustomFont();

        saveButton = new JButton("Save Game");
        exitButton = new JButton("Exit");
        resumeButton = new JButton("Resume");

        makeButtonTransparent(saveButton);
        makeButtonTransparent(exitButton);
        makeButtonTransparent(resumeButton);

        saveButton.addActionListener(e -> {
            saveAction.run();
            frame.requestFocus();
            frame.repaint();
        });

        exitButton.addActionListener(e -> {
            exitAction.run();
        });

        resumeButton.addActionListener(e -> {
            resumeAction.run(); // Switch back to the game
        });

        add(saveButton);
        add(exitButton);
        add(resumeButton);

        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                updateLayout(frame);
            }
        });

        updateLayout(frame);
    }

    private void loadCustomFont() {
        try {
            File fontFile = new File("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\font\\3.otf");
            customFont = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(fontFile)).deriveFont(60f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (Exception e) {
            System.out.println("Error loading font: " + e.getMessage());
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

    private void updateLayout(JFrame frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();

        int buttonWidth = 250;
        int buttonHeight = 50;
        int centerX = (width - buttonWidth) / 2;
        int centerY = height / 2;

        resumeButton.setBounds(centerX, centerY - 70, buttonWidth, buttonHeight);
        saveButton.setBounds(centerX, centerY, buttonWidth, buttonHeight);
        exitButton.setBounds(centerX, centerY + 70, buttonWidth, buttonHeight);

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.RED);
        if (customFont != null) {
            g.setFont(customFont.deriveFont(60f));
        } else {
            g.setFont(new Font("Arial", Font.BOLD, 60));
        }

        String pausedText = "GAME PAUSED";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(pausedText);
        int textX = (getWidth() - textWidth) / 2;
        int textY = getHeight() / 3;
        g.drawString(pausedText, textX, textY);
    }
}
