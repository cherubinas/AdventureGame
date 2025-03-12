import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.SwingUtilities;

public class Player {
    private int x, y, width, height;
    private int dx, dy;
    private boolean onGround;
    private GamePanel gamePanel;

    private final int SPEED = 5, JUMP_STRENGTH = -15, GRAVITY = 1;
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
        animations = AnimationLoader.loadAllAnimations();
        ATTACK_FRAMES = animations.containsKey("attack") ? animations.get("attack").size() : 1;
    }

    public void render(Graphics g) {
        if (animations.containsKey(currentState)) {
            BufferedImage frame = animations.get(currentState).get(currentFrame);
            int newWidth = (int) (frame.getWidth() * 1.1);
            int newHeight = (int) (frame.getHeight() * 1.1);

            if (facingRight) {
                g.drawImage(frame, x, y, newWidth, newHeight, null);
            } else {
                g.drawImage(frame, x + newWidth, y, -newWidth, newHeight, null);
            }
            g.setColor(Color.BLACK);
            g.drawRect(x, y - 10, width, 5);
            g.setColor(Color.GREEN);
            g.fillRect(x, y - 10, (int) ((width * (health / 100.0))), 5);
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
                Rectangle futureBounds = new Rectangle(x, y + velocityY, width, height);
    
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
            if (x + width > floor.width) x = floor.width - width;
        }
    
        if (dashCooldownTimer > 0) {
            dashCooldownTimer--;
        }
    
        if (!isAttacking && !isDashing && animations.containsKey(currentState)) {
            currentFrame = (currentFrame + 1) % animations.get(currentState).size();
        }
    }

    public void moveLeft() {
        if (!isDashing && !isAttacking) {
            dx = -SPEED;
            facingRight = false;
            if (!isJumping && !isFalling) currentState = "run";
        }
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

    public void attack() {
        if (!isAttacking) {
            isAttacking = true;
            attackTimer = ATTACK_DURATION;
            currentState = "attack";
            currentFrame = 0;
            attackFrameIndex = 0;
            lastXAfterAttack = x;
            lastYAfterAttack = y;
        }
    }

    public void takeHit(int damage) {
        if (isDead) return; // Prevent further hits after death
    
        health -= damage;
        if (health <= 0) {
            health = 0;
            die();
        } else {
            // Apply knockback
            dx = facingRight ? -10 : 10;
            isHit = true;
        }
    }
    

    public void die() {
        if (isDead) return; // Prevent redundant death call
    
        isDead = true;
        currentState = "death";
    
        SwingUtilities.invokeLater(() -> {
            GamePanel ancestorGamePanel = (GamePanel) SwingUtilities.getAncestorOfClass(GamePanel.class, gamePanel);
            if (gamePanel != null) {
                gamePanel.setGameOver(true);
            }
        });
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

    public void handleMouseInput(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                if (!mousePressed) {
                    attack();
                    mousePressed = true;
                }
            } else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                mousePressed = false;
            }
        }
    }
}
