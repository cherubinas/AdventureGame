import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;

public class PauseMenu extends JPanel {
    private JButton saveButton, exitButton, resumeButton;
    private JToggleButton muteMusicButton, muteSfxButton;
    private JSlider volumeSlider;
    private Font customFont;
    private Runnable saveAction, exitAction, resumeAction;

    private MusicPlayer musicPlayer;

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

        muteMusicButton = new JToggleButton("Music ON");
        muteSfxButton = new JToggleButton("SFX ON");
        muteSfxButton.setSelected(MusicPlayer.areSoundEffectsMuted());
        muteSfxButton.setText(MusicPlayer.areSoundEffectsMuted() ? "SFX OFF" : "SFX ON");
        volumeSlider = new JSlider(0, 100, (int) (MusicPlayer.getGlobalVolume() * 100));

        styleButton(saveButton);
        styleButton(exitButton);
        styleButton(resumeButton);
        styleToggleButton(muteMusicButton);
        styleToggleButton(muteSfxButton);
        styleSlider(volumeSlider);

        saveButton.addActionListener(e -> saveAction.run());
        exitButton.addActionListener(e -> exitAction.run());
        resumeButton.addActionListener(e -> resumeAction.run());

        muteMusicButton.addActionListener(e -> {
            boolean muted = muteMusicButton.isSelected();
            muteMusicButton.setText(muted ? "Music OFF" : "Music ON");
            if (musicPlayer != null) {
                musicPlayer.setMuted(muted);
                if (muted) {
                    musicPlayer.pause();
                } else {
                    musicPlayer.resume();
                }
            }
        });

        muteSfxButton.addActionListener(e -> {
            boolean muted = muteSfxButton.isSelected();
            muteSfxButton.setText(muted ? "SFX OFF" : "SFX ON");
            MusicPlayer.setSoundEffectsMuted(muted);
        });

        volumeSlider.addChangeListener(e -> {
            double volume = volumeSlider.getValue() / 100.0;
            MusicPlayer.setGlobalVolume(volume);
            if (musicPlayer != null) {
                musicPlayer.setVolume(volume);
            }
        });

        add(saveButton);
        add(exitButton);
        add(resumeButton);
        add(muteMusicButton);
        add(muteSfxButton);
        add(volumeSlider);

        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                updateLayout(frame);
            }
        });

        updateLayout(frame);
    }

    public void setMusicPlayer(MusicPlayer player) {
        this.musicPlayer = player;
        if (musicPlayer != null) {
            boolean musicMuted = MusicPlayer.getGlobalVolume() == 0 || muteMusicButton.isSelected(); // fallback
            muteMusicButton.setSelected(musicMuted);
            muteMusicButton.setText(musicMuted ? "Music OFF" : "Music ON");
        }
    }

    private void styleButton(JButton button) {
        button.setOpaque(true);
        button.setBackground(Color.BLACK);
        button.setForeground(Color.RED);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        if (customFont != null) {
            button.setFont(customFont.deriveFont(30f));
        }
        addHoverEffect(button);
    }

    private void styleToggleButton(JToggleButton button) {
        button.setOpaque(true);
        button.setBackground(Color.BLACK);
        button.setForeground(Color.RED);
        button.setFocusPainted(false);
        button.setIcon(null);
        button.setSelectedIcon(null);
        button.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        if (customFont != null) {
            button.setFont(customFont.deriveFont(24f));
        }
        addHoverEffect(button);
    }

    private void styleSlider(JSlider slider) {
        slider.setOpaque(false);
        slider.setForeground(Color.RED);
        slider.setBackground(Color.BLACK);
        slider.setFocusable(false);
        slider.setUI(new javax.swing.plaf.metal.MetalSliderUI() {
            @Override
            public void paintThumb(Graphics g) {
                g.setColor(Color.RED);
                g.fillRect(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
            }

            @Override
            public void paintTrack(Graphics g) {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(trackRect.x, trackRect.y + trackRect.height / 2 - 2,
                        trackRect.width, 4);
            }
        });
    }

    private void addHoverEffect(AbstractButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.DARK_GRAY);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.BLACK);
            }
        });
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

    private void updateLayout(JFrame frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();

        int buttonWidth = 250;
        int buttonHeight = 50;
        int centerX = (width - buttonWidth) / 2;
        int centerY = height / 2;

        resumeButton.setBounds(centerX, centerY - 140, buttonWidth, buttonHeight);
        saveButton.setBounds(centerX, centerY - 70, buttonWidth, buttonHeight);
        exitButton.setBounds(centerX, centerY, buttonWidth, buttonHeight);
        muteMusicButton.setBounds(centerX, centerY + 70, buttonWidth, buttonHeight);
        muteSfxButton.setBounds(centerX, centerY + 130, buttonWidth, buttonHeight);
        volumeSlider.setBounds(centerX, centerY + 190, buttonWidth, 40);

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.RED);
        g.setFont(customFont != null ? customFont.deriveFont(60f) : new Font("Arial", Font.BOLD, 60));
        String pausedText = "GAME PAUSED";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(pausedText);
        int textX = (getWidth() - textWidth) / 2;
        int textY = getHeight() / 5;
        g.drawString(pausedText, textX, textY);
    }
}
