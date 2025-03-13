import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

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

    private HashMap<String, ArrayList<BufferedImage>> animations;
    private String currentAnimation = "enemy_idle";
    private int frameIndex = 0;
    private int animationSpeed = 2;
    private int animationCounter = 0;
    private boolean facingRight = true; // Track enemy facing direction

    public Enemy(int x, int y, GamePanel gamePanel) {
        this.x = x;
        this.y = y;
        this.width = 50;
        this.height = 50;
        this.patrolStartX = x;
        this.gamePanel = gamePanel;
        this.animations = AnimationLoader.loadAllAnimations();
    }

    public void update(Player player) {
        if (isDead) return;

        int playerX = player.getX();
        int playerY = player.getY();
        int distanceToPlayer = Math.abs(playerX - x);
        int verticalDistanceToPlayer = Math.abs(playerY - y);

        // NEW: If the player is directly above or below, set idle animation
        if (distanceToPlayer < 10 && verticalDistanceToPlayer > 30) {
            currentAnimation = "enemy_idle";
            return; // Stop further updates to avoid twitching
        }

        if (distanceToPlayer < ATTACK_RANGE && verticalDistanceToPlayer < 30) {
            attack(player);
        } else if (distanceToPlayer < CHASE_RANGE) {
            chasingPlayer = true;
        } else {
            chasingPlayer = false;
        }

        if (!isAttacking) {
            if (chasingPlayer) {
                if (playerX > x) {
                    x += speed;
                    facingRight = true;
                } else {
                    x -= speed;
                    facingRight = false;
                }
                currentAnimation = "enemy_run";
            } else {
                patrol();
            }
        }

        updateAnimation();
    }

    private void patrol() {
        x += speed * patrolDirection;
        if (Math.abs(x - patrolStartX) > patrolDistance) {
            patrolDirection *= -1;
            facingRight = (patrolDirection > 0); // Flip direction
        }
        currentAnimation = "enemy_run"; // Use walk animation instead of idle
    }

    private void attack(Player player) {
        if (!isAttacking) {
            isAttacking = true;
            currentAnimation = "enemy_attack";
            facingRight = player.getX() > x;

            // Calculate how long the animation should take based on frame count
            int attackAnimationTime = animations.get("enemy_attack").size() * animationSpeed * 16; // 16ms per frame

            new Thread(() -> {
                try {
                    Thread.sleep(attackAnimationTime / 2); // Apply damage halfway through the animation
                    if (Math.abs(player.getY() - y) <= 30 && Math.abs(player.getX() - x) <= ATTACK_RANGE) {
                        player.takeHit(DAMAGE);
                    }

                    Thread.sleep(attackAnimationTime / 2); // Wait for the rest of the animation
                } catch (InterruptedException ignored) {}

                isAttacking = false; // Only reset after full animation plays
            }).start();
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
        currentAnimation = "enemy_hurt";
        if (health <= 0) {
            die();
        }
    }

    private void die() {
        isDead = true;
        currentAnimation = "enemy_death";
        gamePanel.removeEnemy(this);
    }

    private void updateAnimation() {
        animationCounter++;
        if (animationCounter >= animationSpeed) {
            animationCounter = 0;
            frameIndex++;

            // Reset frameIndex after animation finishes
            if (frameIndex >= animations.get(currentAnimation).size()) {
                frameIndex = 0;

                // After death animation finishes, remove enemy
                if (currentAnimation.equals("enemy_death")) {
                    gamePanel.removeEnemy(this);
                }

                // After hurt animation, go back to idle or run
                if (currentAnimation.equals("enemy_hurt")) {
                    currentAnimation = chasingPlayer ? "enemy_run" : "enemy_idle";
                }
            }
        }
    }

    public void render(Graphics g) {
        if (isDead) return;

        BufferedImage frame = animations.get(currentAnimation).get(frameIndex);

        // Flip image if facing left
        if (!facingRight) {
            g.drawImage(frame, x + width, y, -width, height, null);
        } else {
            g.drawImage(frame, x, y, width, height, null);
        }

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
