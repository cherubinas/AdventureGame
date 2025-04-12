import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.SwingUtilities;

public class Player {
    private int x, y, width, height;
    private int dx, dy;
    private boolean onGround;
    private GamePanel gamePanel;
    private int worldWidth = 3000;

    private final int SPEED = 5, JUMP_STRENGTH = -18, GRAVITY = 1;
    private final int DASH_SPEED = 15;
    private final int DASH_DURATION = 10;
    private final int DASH_COOLDOWN = 30;

    private final int ATTACK_FRAMES;
    private final int ATTACK_DURATION = 20;

    private boolean isJumping = false, isFalling = false;
    private boolean isAttacking = false, isDashing = false, isHit = false, isDead = false;
    private boolean facingRight = true;
    private int velocityY = 0;

    private int dashTimer = 0;
    private int dashCooldownTimer = 0;
    private int attackTimer = 0;
    private int attackFrameIndex = 0;
    private int health = 100;

    private boolean mousePressed = false;

    private String currentState = "idle";
    private int currentFrame = 0;
    private HashMap<String, ArrayList<BufferedImage>> animations;

    private int lastXAfterAttack;
    private int lastYAfterAttack;

    public Player(int x, int y, GamePanel gamePanel) {
        this.x = x;
        this.y = y;
        this.width = 50;
        this.height = 50;
        this.dx = 0;
        this.dy = 0;
        this.onGround = false;
        this.gamePanel = gamePanel;
        HashMap<String, AnimationLoader.AnimationData> animationDataMap = AnimationLoader.loadAllAnimations();
        this.animations = new HashMap<>();



        for (String key : animationDataMap.keySet()) {
            this.animations.put(key, animationDataMap.get(key).frames); // Extract frames only
        }
        ATTACK_FRAMES = animations.containsKey("attack") ? animations.get("attack").size() : 1;
    }
    public void setWorldWidth(int worldWidth) {
        this.worldWidth = worldWidth;
    }

    public void render(Graphics g, int cameraX) {
        if (animations.containsKey(currentState)) {
            BufferedImage frame = animations.get(currentState).get(currentFrame);
            int newWidth = (int) (frame.getWidth() * 1.1);
            int newHeight = (int) (frame.getHeight() * 1.1);

            int drawX = x - cameraX;

            if (facingRight) {
                g.drawImage(frame, drawX, y, newWidth, newHeight, null);
            } else {
                g.drawImage(frame, drawX + newWidth, y, -newWidth, newHeight, null);
            }

            // Health bar
            g.setColor(Color.BLACK);
            g.drawRect(drawX, y - 10, width, 5);
            g.setColor(Color.GREEN);
            g.fillRect(drawX, y - 10, (int) ((width * (health / 100.0))), 5);

            // Bounding box (for debugging)
            g.setColor(new Color(255, 0, 0, 250));
            g.drawRect(drawX, y, width, height);
        }
    }

    public void update(List<Platform> platforms, Rectangle floor) {
        if (isAttacking) {
            if (attackTimer > 0) {
                attackTimer--;
                attackFrameIndex = (ATTACK_FRAMES - 1) - (attackTimer * ATTACK_FRAMES / ATTACK_DURATION);
                currentFrame = Math.min(attackFrameIndex, ATTACK_FRAMES - 1);
            } else {
                isAttacking = false;
                currentState = "idle";
            }
        }
        if (velocityY < 0) {
            currentState = "jump";  // Going up = Jump animation
        } else if (velocityY > 0) {
            currentState = "fall";  // Going down = Fall animation
        }
        if (isHit) {
            x += dx; // Apply knockback movement
            dx *= 0.8; // Gradually slow down knockback effect
            if (Math.abs(dx) < 1) { // If knockback speed is low, reset hit state
                dx = 0;
                isHit = false;
            }
        }
        if (hitTimer > 0) {
            hitTimer--;
            if (hitTimer == 0) {
                isHit = false;
                currentState = "idle"; // Reset state when animation finishes
            }
        }
        if (isDead) {
            if (currentFrame < animations.get("death").size() - 1) {
                currentFrame++; // Progress death animation
            }
            return; // Stop further updates
        }

        if (isDashing) {
            if (dashTimer > 0) {
                x += facingRight ? DASH_SPEED : -DASH_SPEED;
                dashTimer--;
            } else {
                isDashing = false;
                dashCooldownTimer = DASH_COOLDOWN;
                currentState = "idle";
            }
        }

        if (!isDashing && !isAttacking) {
            velocityY += GRAVITY;
            y += velocityY;
            onGround = false;

            // Platform collision handling
            for (Platform platform : platforms) {
                Rectangle platformBounds = platform.getBounds();
                Rectangle futureBounds = new Rectangle(x + dx, y + velocityY, width, height);

                if (futureBounds.intersects(platformBounds)) {
                    if (velocityY > 0) { // Falling
                        y = platformBounds.y - height;
                        velocityY = 0;
                        isJumping = false;
                        isFalling = false;
                        onGround = true;
                    } else if (velocityY < 0) { // Jumping upwards into a platform
                        y = platformBounds.y + platformBounds.height;
                        velocityY = 0;
                    }
                }
            }

            // Floor collision
            if (y + height >= floor.y) {
                y = floor.y - height;
                velocityY = 0;
                isJumping = false;
                isFalling = false;
                onGround = true;
            }


            // Prevent horizontal movement while colliding with platforms
            x += dx;
            for (Platform platform : platforms) {
                if (getBounds().intersects(platform.getBounds())) {
                    x -= dx; // Revert movement if a collision happens
                    break;
                }
            }

            // Boundary checks to prevent going out of bounds
            if (x < 0) x = 0;
            if (x + width > worldWidth) x = worldWidth - width;
        }

        if (dashCooldownTimer > 0) {
            dashCooldownTimer--;
        }

        if (!isAttacking && !isDashing && animations.containsKey(currentState)) {
            currentFrame = (currentFrame + 1) % animations.get(currentState).size();
        }
    }

    public Rectangle getAttackHitbox() {
        int attackWidth = 50; // Adjust this based on weapon range
        int attackHeight = height; // Same as player's height
        int attackX = facingRight ? x + width : x - attackWidth; // Position it in front
        int attackY = y;

        return new Rectangle(attackX, attackY, attackWidth, attackHeight);
    }

    public void moveLeft() {
        if (!isDashing && !isAttacking) {
            dx = -SPEED;
            facingRight = false;
            if (!isJumping && !isFalling) currentState = "run";
        }
    }
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void moveRight() {
        if (!isDashing && !isAttacking) {
            dx = SPEED;
            facingRight = true;
            if (!isJumping && !isFalling) currentState = "run";
        }
    }

    public void stopMoving() {
        if (!isJumping && !isFalling && !isDashing && !isAttacking) currentState = "idle";
        dx = 0;
    }

    public void jump() {
        if (!isJumping && !isFalling && !isDashing && !isAttacking) {
            isJumping = true;
            velocityY = JUMP_STRENGTH;
            currentState = "jump"; // Ensure animation starts properly
        }
    }

    public void dash() {
        if (!isDashing && dashCooldownTimer == 0 && !isAttacking) {
            isDashing = true;
            dashTimer = DASH_DURATION;
            currentState = "dash";
            currentFrame = 0;
        }
    }

    public void attack(List<Enemy> enemies) {
        if (!isAttacking) {
            isAttacking = true;
            attackTimer = ATTACK_DURATION;
            currentState = "attack";
            currentFrame = 0;

            Rectangle attackHitbox = getAttackHitbox();
            for (Enemy enemy : enemies) {
                if (attackHitbox.intersects(enemy.getBounds())) {
                    MusicPlayer.playSound("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\music\\attack.mp3");
                    enemy.takeDamage(10);
                }
            }
        }
    }

    private int hitTimer = 0; // Timer for hit animation duration

    public void takeHit(int damage) {
        if (isDead || hitTimer > 0) return; // Prevent multiple triggers

        health -= damage;
        if (health <= 0) {
            health = 0;
            die();
        } else {
            dx = facingRight ? -10 : 10; // Apply knockback
            isHit = true;
            hitTimer = 20; // Set a timer so animation plays only once
            currentState = "hit";
            currentFrame = 0;
        }
    }

    public void die() {

        if (isDead) return;

        isDead = true;
        currentState = "death";
        currentFrame = 0;
        MusicPlayer.playSound("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\music\\gameover.mp3");

        new Thread(() -> {
            try {
                Thread.sleep(1500); // Wait for the animation to complete
            } catch (InterruptedException ignored) {
            }

            SwingUtilities.invokeLater(() -> {
                if (gamePanel != null) {
                    gamePanel.setGameOver(true);
                }
            });
        }).start();
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

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void handleMouseInput(MouseEvent e, List<Enemy> enemies) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                if (!mousePressed) {
                    attack(enemies); // Pass enemies list
                    mousePressed = true;
                }
            } else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                mousePressed = false;
            }
        }
    }

    // --- SAVE & LOAD FUNCTIONS ---

    public void savePlayerState(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(x + "," + y + "," + health);
        } catch (IOException e) {
            System.err.println("Error saving player state: " + e.getMessage());
        }
    }

    public void loadPlayerState(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split(",");
                x = Integer.parseInt(parts[0]);
                y = Integer.parseInt(parts[1]);
                health = Integer.parseInt(parts[2]);
            }
        } catch (IOException e) {
            System.err.println("Error loading player state: " + e.getMessage());
        }
    }
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // Load normal fields

        // Restore animations after loading
        HashMap<String, AnimationLoader.AnimationData> animationDataMap = AnimationLoader.loadAllAnimations();
        this.animations = new HashMap<>();
        for (String key : animationDataMap.keySet()) {
            this.animations.put(key, animationDataMap.get(key).frames); // Extract frames only
        }
    }
    public void reset() {
        x = 250;
        y = 300;
        dx = 0;
        dy = 0;
        velocityY = 0;
        health = 100;
        currentFrame = 0;
        currentState = "idle";
        isJumping = false;
        isFalling = false;
        isAttacking = false;
        isDashing = false;
        isHit = false;
        isDead = false;
        dashTimer = 0;
        dashCooldownTimer = 0;
        attackTimer = 0;
        attackFrameIndex = 0;
        hitTimer = 0;
        mousePressed = false;
        facingRight = true;
    }
}