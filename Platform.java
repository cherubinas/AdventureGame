import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import javax.imageio.ImageIO;

public class Platform implements Serializable {
    private int x, y, width, height;
    private boolean isMoving;
    private int direction = 1;
    private int speed = 2;
    private int originalX;
    private int range = 100;


    private static BufferedImage texture;

    static {
        try {
            texture = ImageIO.read(new File("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\resources\\map\\afb6872a0fbefb737c7636270f3b626b.jpg"));
        } catch (IOException e) {
            System.err.println("Failed to load platform texture!");
            e.printStackTrace();
        }
    }
    public static class PlatformState implements Serializable {
        private final int x, y, width, height;
        private final boolean isMoving;

        public PlatformState(int x, int y, int width, int height, boolean isMoving) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.isMoving = isMoving;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public boolean isMoving() { return isMoving; }
    }

    public Platform(int x, int y, int width, int height) {
        this(x, y, width, height, false);
    }
    public PlatformState getState() {
        return new PlatformState(x, y, width, height, isMoving);
    }
    public Platform(PlatformState state) {
        this(state.getX(), state.getY(), state.getWidth(), state.getHeight(), state.isMoving());
    }


    public Platform(int x, int y, int width, int height, boolean isMoving) {
        this.x = x;
        this.originalX = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isMoving = isMoving;
    }

    public void update() {
        if (isMoving) {
            x += speed * direction;
            if (x > originalX + range || x < originalX - range) {
                direction *= -1;
            }
        }
    }

    public void render(Graphics g, int cameraX) {
        int drawX = x - cameraX;

        if (texture != null) {
            int tileWidth = texture.getWidth();
            int tileHeight = height = 21;

            for (int i = 0; i < width; i += tileWidth) {
                int drawWidth = Math.min(tileWidth, width - i);
                // Draw the tile without squishing vertically
                g.drawImage(texture,
                        drawX + i, y, drawX + i + drawWidth, y + tileHeight,
                        0, 0, drawWidth, tileHeight,
                        null);
            }
        } else {
            g.setColor(isMoving ? Color.CYAN : Color.GRAY);
            g.fillRect(drawX, y, width, height);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
