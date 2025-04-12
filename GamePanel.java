import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.Random;

public class GamePanel extends JPanel implements KeyListener, ActionListener, MouseListener {
    private final Player player;
    private Timer gameLoop;
    private List<Platform> platforms;
    private Rectangle floor;
    public List<Enemy> enemies;
    private boolean gameOver = false;
    private ImageIcon backgroundGif; // Background image
    private GameOverScreen gameOverScreen; // Separate Game Over screen
    private boolean isPaused = false;
    private PauseMenu pauseMenu;
    private JButton resumeButton;
    private Runnable resumeAction;
    private MusicPlayer backgroundMusicPlayer;
    private int worldWidth = 9000; // Width of the entire game world
    private int worldHeight = 600;
    private int cameraX = 0; // How much the view is offset horizontally

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        backgroundMusicPlayer = new MusicPlayer("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\music\\kim-lightyear-leave-the-world-tonight-chiptune-edit-loop-132102.mp3"); // Adjust path as needed
        backgroundMusicPlayer.play(); // Start background music


        setLayout(null); // Allows manual positioning of components
        // Load background image
        setDoubleBuffered(true);
        backgroundGif = new ImageIcon("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\j1IX2Y.png");
        // Initialize player
        player = new Player(100, 300, this);
        player.setWorldWidth(worldWidth);


        // Initialize platforms
        platforms = new ArrayList<>();
        platforms.add(new Platform(100, 400, 200, 20));
        platforms.add(new Platform(350, 300, 200, 20));
        platforms.add(new Platform(600, 200, 200, 20));

        // Initialize floor
        floor = new Rectangle(0, 500, worldWidth, 50);


        // Initialize enemies
        enemies = new ArrayList<>(); // 👈 initialize FIRST

        resizePlatforms(worldWidth, worldHeight);

        // Initialize game loop
        gameLoop = new Timer(16, this); // Approximately 60 FPS
        gameLoop.start();

        // Initialize game over screen
        gameOverScreen = new GameOverScreen(this);
        gameOverScreen.setBounds(0, 0, getWidth(), getHeight());
        gameOverScreen.setPreferredSize(new Dimension(getWidth(), getHeight()));
        add(gameOverScreen);
        gameOverScreen.setVisible(false); // Hide it initially
    }
    private static BufferedImage platformTexture;

    static {
        try {
            platformTexture = ImageIO.read(new File("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\resources\\map\\afb6872a0fbefb737c7636270f3b626b.jpg"));
        } catch (IOException e) {
            System.err.println("Failed to load floor/wall texture!");
            e.printStackTrace();
        }
    }

    public void startNewGame() {
        if (gameLoop != null && gameLoop.isRunning()) {
            gameLoop.stop();
        }

        gameOver = false;

        // 🧼 Clear and reset the player
        player.reset();

        // 🧼 Clear all platforms and enemies
        platforms.clear();
        enemies.clear();

        // ✅ Reinitialize all platforms (and their enemies)
        resizePlatforms(worldWidth, worldHeight);

        // 👇 Add at least one fallback enemy in case platforms fail
        if (enemies.isEmpty()) {
            enemies.add(new Enemy(300, 450, this));
        }

        gameOverScreen.setBounds(0, 0, getWidth(), getHeight());
        gameOverScreen.setPreferredSize(new Dimension(getWidth(), getHeight()));
        gameOverScreen.setVisible(false);

        gameLoop = new Timer(16, this);
        gameLoop.start();

        this.setFocusable(true);
        this.requestFocusInWindow();
        repaint();
    }
    public void removeEnemy(Enemy enemy) {
        enemies.remove(enemy);
    }

    public GameState createGameState() {
        List<Enemy.EnemyState> enemyStates = new ArrayList<>();
        for (Enemy enemy : enemies) {
            enemyStates.add(enemy.getState());
        }

        List<Platform.PlatformState> platformStates = new ArrayList<>();
        for (Platform platform : platforms) {
            platformStates.add(platform.getState());
        }

        return new GameState(player.getHealth(), player.getX(), player.getY(), enemyStates, platformStates);
    }
    public void loadPlatforms(List<Platform.PlatformState> platformStates) {
        platforms.clear();
        enemies.clear(); // Optional: clear enemies too before placing new ones

        for (Platform.PlatformState state : platformStates) {
            Platform platform = new Platform(state.getX(), state.getY(), state.getWidth(), state.getHeight(), state.isMoving());
            platforms.add(platform);
        }
    }

    public void loadEnemies(List<Enemy.EnemyState> enemyStates) {
        enemies.clear();
        for (Enemy.EnemyState enemyState : enemyStates) {
            Enemy enemy = new Enemy(enemyState.getX(), enemyState.getY(), this);
            enemy.setState(enemyState.getX(), enemyState.getY(), enemyState.getHealth());
            if (enemyState.isDead()) {

                enemy.die();
            }
            enemies.add(enemy);
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background
        if (backgroundGif != null) {
            g.drawImage(backgroundGif.getImage(), 0, 0, getWidth(), getHeight(), this);
        }

        if (gameOver) {
            gameOverScreen.setVisible(true);
            return;
        }
        // --- Draw left wall with scaled width ---
        // --- Draw left wall using a vertical slice of the texture ---
        if (platformTexture != null) {
            int sliceWidth = 20; // a narrow piece of the texture
            int tileH = platformTexture.getHeight();
            BufferedImage wallSlice = platformTexture.getSubimage(0, 0, sliceWidth, tileH);

            for (int y = 0; y < getHeight(); y += tileH) {
                g.drawImage(wallSlice, 0 - cameraX, y, null);
            }
        } else {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0 - cameraX, 0, 20, getHeight());
        }

// --- Draw floor ---
        if (platformTexture != null) {
            int tileW = platformTexture.getWidth();
            int tileH = 30;
            for (int x = floor.x; x < floor.x + floor.width; x += tileW) {
                g.drawImage(platformTexture, x - cameraX, floor.y, null);
            }
        } else {
            g.setColor(Color.GRAY);
            g.fillRect(floor.x - cameraX, floor.y, floor.width, floor.height);
        }

        // Player & world offset
        player.render(g, cameraX);

        for (Platform platform : platforms) {
            platform.render(g, cameraX);
        }

        for (Enemy enemy : enemies) {
            enemy.render(g, cameraX);
        }

        // Floor

    }
    public void openPauseMenu() {
        if (isPaused) return;

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        pauseMenu = new PauseMenu(

                frame,
                () -> SaveManager.saveGame(createGameState()),  // Save callback
                () -> System.exit(0),                          // Quit callback
                () -> closePauseMenu(frame)                         // Resume callback
        );
        pauseMenu.setMusicPlayer(backgroundMusicPlayer); // Connects MusicPlayer instance

        frame.setContentPane(pauseMenu);
        frame.revalidate();
        frame.repaint();
        isPaused = true;
    }

    public void closePauseMenu(JFrame frame) {
        frame.setContentPane(this); // restores GamePanel as the main content pane
        frame.revalidate();
        frame.repaint();

        this.setFocusable(true);
        this.requestFocusInWindow(); // <-- This is the magic line
        isPaused = false;
    }

    public void checkPlayerAttack() {
        for (Enemy enemy : enemies) {
            if (player.getBounds().intersects(enemy.getBounds())) {
                player.attack(enemies);
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        switch (key) {

            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> player.moveLeft();
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> player.moveRight();
            case KeyEvent.VK_SPACE -> player.jump();
            case KeyEvent.VK_SHIFT -> player.dash();
            case KeyEvent.VK_F -> player.attack(enemies);
            case KeyEvent.VK_H -> player.takeHit(10); // Press H to test player taking damage
            case KeyEvent.VK_ESCAPE -> {
                if (!isPaused) {
                    openPauseMenu(); // Only pauses the game
                }
                // No else! Resume only happens via button
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_D) player.stopMoving();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            player.update(platforms, floor);
            for (Enemy enemy : enemies) {
                enemy.update(player);
            }
            for (Platform platform : platforms) {
                platform.update(); // only moves if it's a moving one
            }

            // 👇 CAMERA follows player
            cameraX = player.getX() - getWidth() / 2;
            cameraX = Math.max(0, Math.min(cameraX, worldWidth - getWidth()));

            repaint();
        }
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        player.handleMouseInput(e, enemies);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        player.handleMouseInput(e, enemies);
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        floor = new Rectangle(0, height - 50, width, 50); // Resize floor
        resizePlatforms(width, height); // Recalculate platform positions
    }

    private void resizePlatforms(int width, int height) {
        platforms.clear();
        enemies.clear();

        Random rand = new Random();
        int currentX = 100;
        int minY = 240;
        int maxY = 440;
        int minGapX = 140;
        int maxGapX = 220;

        while (currentX < worldWidth - 200) {
            int baseY = rand.nextInt(maxY - minY) + minY;
            int baseWidth = rand.nextInt(60) + 140;
            boolean baseMoving = rand.nextDouble() < 0.15;

            Platform basePlatform = new Platform(currentX, baseY, baseWidth, 20, baseMoving);
            platforms.add(basePlatform);

            // Enemy spawn chance scales with progress
            float progress = (float) currentX / worldWidth;
            float enemySpawnChance = 0.3f + progress * 0.5f; // 30% to 80%

            if (!baseMoving && rand.nextFloat() < enemySpawnChance) {
                int enemyX = currentX + baseWidth / 2 - 10;
                int enemyY = baseY - 40;
                enemies.add(new Enemy(enemyX, enemyY, this));
            }

            // Stack vertically
            int stackCount = rand.nextInt(4) + 1;

            for (int i = 1; i < stackCount; i++) {
                int stackY = baseY - i * 80;
                if (stackY < 180) break;

                int xOffset = rand.nextInt(100) - 50;
                int stackWidth = Math.max(80, baseWidth - (i * 20));

                Platform stackedPlatform = new Platform(currentX + xOffset, stackY, stackWidth, 20, false);
                platforms.add(stackedPlatform);

                if (progress > 0.4 && rand.nextFloat() < (0.1f + progress * 0.2f)) {
                    int enemyX = currentX + xOffset + stackWidth / 2 - 10;
                    int enemyY = stackY - 40;
                    enemies.add(new Enemy(enemyX, enemyY, this));
                }
            }

            int gap = rand.nextInt(maxGapX - minGapX) + minGapX;
            currentX += baseWidth + gap;
        }
    }


    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
        if (gameOver) {

            gameOverScreen.setBounds(0, 0, getWidth(), getHeight()); // Make sure it resizes
            // 👈 handles the sound + repaint
            gameOverScreen.setVisible(true);
            gameOverScreen.showGameOverScreen();


        }
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (gameOverScreen != null) {
                    gameOverScreen.setBounds(0, 0, getWidth(), getHeight());
                    gameOverScreen.revalidate();
                    gameOverScreen.repaint();
                }
            }
        });
    }


}
