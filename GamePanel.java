import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.swing.*;

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

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);


        setLayout(null); // Allows manual positioning of components
        // Load background image
        setDoubleBuffered(true);
        backgroundGif = new ImageIcon("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\6d13119ec35d038c0649f6f4d5f2b9ad.gif");
        // Initialize player
        player = new Player(250, 300, this);

        // Initialize platforms
        platforms = new ArrayList<>();
        platforms.add(new Platform(100, 400, 200, 20));
        platforms.add(new Platform(350, 300, 200, 20));
        platforms.add(new Platform(600, 200, 200, 20));

        // Initialize floor
        floor = new Rectangle(0, 500, 800, 50);

        // Initialize enemies
        enemies = new ArrayList<>();
        enemies.add(new Enemy(500, 450, this));

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

    public void startNewGame() {
        if (gameLoop != null && gameLoop.isRunning()) {
            gameLoop.stop();
        }

        gameOver = false;

        player.reset(); // <-- Make sure we reset *everything*

        enemies.clear();
        enemies.add(new Enemy(500, 450, this));
        gameOverScreen.setBounds(0, 0, getWidth(), getHeight());
        gameOverScreen.setPreferredSize(new Dimension(getWidth(), getHeight()));

        add(gameOverScreen);
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
        return new GameState(player.getHealth(), player.getX(), player.getY(), enemyStates);
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

        // Draw background image
        if (backgroundGif != null) {
            g.drawImage(backgroundGif.getImage(), 0, 0, getWidth(), getHeight(), this);
        }

        if (gameOver) {
            gameOverScreen.setVisible(true);
            return;
        }

        player.render(g);

        // Render platforms
        for (Platform platform : platforms) {
            platform.render(g);
        }

        // Render enemies
        for (Enemy enemy : enemies) {
            enemy.render(g);
        }

        // Render floor
        g.setColor(Color.GRAY);
        g.fillRect(floor.x, floor.y, floor.width, floor.height);
    }
    public void openPauseMenu() {
        if (isPaused) return;

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        pauseMenu = new PauseMenu(
                frame,
                () -> player.savePlayerState("savefile.txt"),  // Save callback
                () -> System.exit(0),                          // Quit callback
                () -> closePauseMenu(frame)                         // Resume callback
        );

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
        platforms.add(new Platform(width / 4, height - 200, width / 6, 20));
        platforms.add(new Platform(width / 2, height - 300, width / 6, 20));
        platforms.add(new Platform((3 * width) / 4, height - 400, width / 6, 20));
    }
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
        if (gameOver) {
            gameOverScreen.setBounds(0, 0, getWidth(), getHeight()); // Make sure it resizes
            gameOverScreen.setVisible(true);
            this.repaint();
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
