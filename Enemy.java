import java.awt.*;

public class Enemy {
    private int x, y, width, height;
    private int speed = 2;
    private int health = 50;
    private boolean chasingPlayer = false;
    private boolean isDead = false;
    private boolean isAttacking = false;
    private int patrolDirection = 1;
    private int patrolDistance = 100;
    private int patrolStartX;
    private final int ATTACK_RANGE = 40;
    private final int CHASE_RANGE = 200;
    private final int DAMAGE = 10;
    private GamePanel gamePanel;

    public Enemy(int x, int y, GamePanel gamePanel) {
        this.x = x;
        this.y = y;
        this.width = 50;
        this.height = 50;
        this.patrolStartX = x;
        this.gamePanel = gamePanel;
    }

    public void update(Player player) {
        if (isDead) return; // Stop updating if dead

        int playerX = player.getX();
        int distanceToPlayer = Math.abs(playerX - x);

        if (getBounds().intersects(player.getBounds())) {
            attack(player);
        } else if (distanceToPlayer < CHASE_RANGE) {
            chasingPlayer = true;
        } else {
            chasingPlayer = false;
        }

        if (chasingPlayer) {
            x += (playerX > x) ? speed : -speed;
        } else {
            patrol();
        }
    }

    private void patrol() {
        x += speed * patrolDirection;
        if (Math.abs(x - patrolStartX) > patrolDistance) {
            patrolDirection *= -1; // Change direction
        }
    }

    private void attack(Player player) {
        if (!isAttacking) {
            isAttacking = true;
            player.takeHit(DAMAGE);
            isAttacking = false; // Reset attacking flag after attack
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            die();
        }
    }

    private void die() {
        isDead = true;
        gamePanel.removeEnemy(this);
    }

    public void render(Graphics g) {
        if (isDead) return; // Don't render if dead

        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);

        // Health bar
        g.setColor(Color.BLACK);
        g.drawRect(x, y - 10, width, 5);
        g.setColor(Color.GREEN);
        g.fillRect(x, y - 10, (int) ((width * (health / 50.0))), 5);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() {
        return x;
    }
}