import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements KeyListener, ActionListener, MouseListener {
    private final Player player;
    private Timer gameLoop;

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        // Initialize player
        player = new Player(250, 300);

        // Game loop for movement and physics
        gameLoop = new Timer(16, this); // Approximately 60 FPS
        gameLoop.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        player.render(g);
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
            case KeyEvent.VK_H -> player.takeHit();
            case KeyEvent.VK_K -> player.die();
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
        player.update();
        repaint();
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
}
