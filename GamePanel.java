import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class GamePanel extends JPanel implements KeyListener, ActionListener, MouseListener {
    private final Player player;
    private Timer gameLoop;
    private List<Platform> platforms;
    private Rectangle floor;
    private List<Enemy> enemies;
    private boolean gameOver = false;

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

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
        enemies.add(new Enemy(500, 400, this));

        // Game loop for movement and physics
        gameLoop = new Timer(16, this); // Approximately 60 FPS
        gameLoop.start();
    }

    public void removeEnemy(Enemy enemy) {
        enemies.remove(enemy);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", getWidth() / 2 - 100, getHeight() / 2);
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

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        switch (key) {
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> player.moveLeft();
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> player.moveRight();
            case KeyEvent.VK_SPACE -> player.jump();
            case KeyEvent.VK_SHIFT -> player.dash();
            case KeyEvent.VK_F -> player.attack();
            case KeyEvent.VK_H -> player.takeHit(1); // Pass an integer argument
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
        player.update(platforms, floor);
        repaint();
        for (Enemy enemy : enemies) {
            enemy.update(player);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        player.handleMouseInput(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        player.handleMouseInput(e);
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
    }
}





