import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Enemy implements Serializable {
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
    private transient GamePanel gamePanel;

    private transient HashMap<String, ArrayList<BufferedImage>> animations;
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
        HashMap<String, AnimationLoader.AnimationData> animationDataMap = AnimationLoader.loadAllAnimations();
        this.animations = new HashMap<>();

        for (String key : animationDataMap.keySet()) {
            this.animations.put(key, animationDataMap.get(key).frames); // Extract frames only
        }
    }

    private boolean isCollidingWithPlatform(int newX, int newY) {
        for (Platform platform : gamePanel.getPlatforms()) {
            Rectangle platformBounds = platform.getBounds();
            Rectangle enemyBounds = new Rectangle(newX, newY, width, height);

            // Check if enemy is inside platform bounds
            if (enemyBounds.intersects(platformBounds)) {
                // If enemy is below the platform, prevent getting stuck
                if (newY + height - 5 < platform.getY()) {
                    y = platform.getY() - height; // Place enemy on top of the platform
                    return true;
                }

                // If enemy is hitting the bottom of the platform, push it down
                if (newY < platform.getY() + platform.getHeight()) {
                    y += 5;
                    return true;
                }

                return true;
            }
        }
        return false;
    }

    public void update(Player player) {
        if (isDead) return;

        int playerX = player.getX();
        int playerY = player.getY();
        int distanceToPlayer = Math.abs(playerX - x);
        int verticalDistanceToPlayer = Math.abs(playerY - y);

        if (distanceToPlayer < 10 && verticalDistanceToPlayer > 30) {
            currentAnimation = "enemy_idle";
            return;
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
                int direction = (playerX > x) ? 1 : -1;
                int newX = x + direction * speed;

                if (!isCollidingWithPlatform(newX, y) && hasGroundBelow(newX)) {
                    x = newX;
                    facingRight = direction > 0;
                    currentAnimation = "enemy_run";
                }
            } else {
                patrol();
            }
        }
        boolean standingOnPlatform = false;

// Check if there's a platform just below
        for (Platform platform : gamePanel.getPlatforms()) {
            Rectangle enemyFeet = new Rectangle(x, y + height, width, 5); // 5px "feet"
            if (enemyFeet.intersects(platform.getBounds())) {
                // Snap to the top of the platform
                y = platform.getY() - height;
                standingOnPlatform = true;
                break;
            }
        }

// If not on a platform, apply gravity
        if (!standingOnPlatform) {
            y += 5; // gravity pull
        }
        updateAnimation();
    }
    private boolean isOnPlatform(int x, int y) {
        Rectangle enemyBounds = new Rectangle(x, y + height, width, 1); // check feet
        for (Platform platform : gamePanel.getPlatforms()) {
            if (enemyBounds.intersects(platform.getBounds())) {
                return true;
            }
        }
        return false;
    }

    private void patrol() {
        int newX = x + speed * patrolDirection;

        if (!hasGroundBelow(newX)) {
            patrolDirection *= -1;
            facingRight = (patrolDirection > 0);
            return;
        }

        if (!isCollidingWithPlatform(newX, y)) {
            x = newX;
        }

        currentAnimation = "enemy_run";
    }
    private boolean hasGroundBelow(int newX) {
        int checkX = newX + width / 2;
        int checkY = y + height + 1;

        for (Platform platform : gamePanel.getPlatforms()) {
            if (platform.getBounds().contains(checkX, checkY)) {
                return true;
            }
        }
        return false;
    }
    private void attack(Player player) {
        if (isAttacking) return;

        isAttacking = true;
        currentAnimation = "enemy_attack";
        facingRight = player.getX() > x;

        int attackFrames = animations.get("enemy_attack").size();
        int attackDuration = attackFrames * animationSpeed * 16;

        new Thread(() -> {
            try {
                Thread.sleep(attackDuration / 2); // Midway through animation

                // Check hit again here to ensure player is still in range
                Rectangle attackRange = new Rectangle(x - ATTACK_RANGE, y, width + 2 * ATTACK_RANGE, height);
                Rectangle playerBounds = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());

                if (!isDead && attackRange.intersects(playerBounds)) {
                    player.takeHit(DAMAGE);
                }

                Thread.sleep(attackDuration / 2);
            } catch (InterruptedException ignored) {}

            isAttacking = false;
        }).start();
    }

    public void takeDamage(int damage) {
        health -= damage;
        currentAnimation = "enemy_hurt";
        if (health <= 0) {
            die();
        }
    }

    void die() {
        isDead = true;

        MusicPlayer.playSound("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\music\\kill.wav");
        currentAnimation = "enemy_death"; // Set death animation
        frameIndex = 0; // Start from the first frame


        // Calculate death animation time (based on frame count)
        int deathAnimationTime = animations.get("enemy_death").size() * animationSpeed * 16; // 16ms per frame

        new Thread(() -> {
            try {
                Thread.sleep(deathAnimationTime); // Wait for the animation to complete
            } catch (InterruptedException ignored) {}

            gamePanel.removeEnemy(this); // Remove enemy after animation
        }).start();
    }

    private void updateAnimation() {
        animationCounter++;
        if (animationCounter >= animationSpeed) {
            animationCounter = 0;
            frameIndex++;

            // If animation reaches the last frame of enemy_death, then remove enemy
            if (currentAnimation.equals("enemy_death") && frameIndex >= animations.get(currentAnimation).size()) {
                gamePanel.removeEnemy(this);
                return; // Stop further updates
            }

            // Loop other animations if needed
            if (frameIndex >= animations.get(currentAnimation).size()) {
                frameIndex = 0;

                // After hurt animation, return to idle or run
                if (currentAnimation.equals("enemy_hurt")) {
                    currentAnimation = chasingPlayer ? "enemy_run" : "enemy_idle";
                }
            }
        }
    }

    public void render(Graphics g, int cameraX) {
        if (isDead) return;

        if (!animations.containsKey(currentAnimation)) return;

        List<BufferedImage> frames = animations.get(currentAnimation);
        if (frameIndex < 0 || frameIndex >= frames.size()) return;

        BufferedImage frame = frames.get(frameIndex);

        int renderX = x - cameraX; // <- Apply camera offset here

        // Flip image if facing left
        if (!facingRight) {
            g.drawImage(frame, renderX + width, y, -width, height, null);
        } else {
            g.drawImage(frame, renderX, y, width, height, null);
        }

        // Health bar with camera offset
        g.setColor(Color.BLACK);
        g.drawRect(renderX, y - 10, width, 5);
        g.setColor(Color.GREEN);
        g.fillRect(renderX, y - 10, (int) ((width * (health / 50.0))), 5);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setState(int x, int y, int health) {
        this.x = x;
        this.y = y;
        this.health = health;
    }

    public int getHealth() {
        return health;
    }
    public class EnemyState implements Serializable {
        public int x, y;
        public int health;
        public boolean isDead;

        public EnemyState(int x, int y, int health, boolean isDead) {
            this.x = x;
            this.y = y;
            this.health = health;
            this.isDead = isDead;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getHealth() { return health; }
        public boolean isDead() { return isDead; }
    }
    public EnemyState getState() {
        return new EnemyState(x, y, health, isDead);
    }

}
