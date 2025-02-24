import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class Player {
    private int x, y;
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

    private boolean mousePressed = false;

    private String currentState = "idle";
    private int currentFrame = 0;
    private HashMap<String, ArrayList<BufferedImage>> animations;

    private int lastXAfterAttack;
    private int lastYAfterAttack;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
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
        }
    }

    public void update() {
        if (isAttacking) {
            if (attackTimer > 0) {
                attackTimer--;
                attackFrameIndex = (ATTACK_FRAMES - 1) - (attackTimer * ATTACK_FRAMES / ATTACK_DURATION);
                currentFrame = Math.min(attackFrameIndex, ATTACK_FRAMES - 1);
            } else {
                isAttacking = false;
                x = lastXAfterAttack;
                y = lastYAfterAttack;
                currentState = "idle";
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
            if (isJumping || isFalling) {
                velocityY += GRAVITY;
                y += velocityY;
                if (y >= 300) {
                    y = 300;
                    isJumping = false;
                    isFalling = false;
                    currentState = "idle";
                } else if (velocityY > 0) {
                    isJumping = false;
                    isFalling = true;
                    currentState = "fall";
                }
            }
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
            x = Math.max(x - SPEED, 0);
            facingRight = false;
            if (!isJumping && !isFalling) currentState = "run";
        }
    }

    public void moveRight() {
        if (!isDashing && !isAttacking) {
            x = Math.min(x + SPEED, 600 - 50);
            facingRight = true;
            if (!isJumping && !isFalling) currentState = "run";
        }
    }

    public void stopMoving() {
        if (!isJumping && !isFalling && !isDashing && !isAttacking) currentState = "idle";
    }

    public void jump() {
        if (!isJumping && !isFalling && !isDashing && !isAttacking) {
            isJumping = true;
            velocityY = JUMP_STRENGTH;
            currentState = "jump";
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

    public void takeHit() {
        isHit = true;
        currentState = "hit";
    }

    public void die() {
        isDead = true;
        currentState = "death";
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
