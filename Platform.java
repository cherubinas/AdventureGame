import java.awt.*;

public class Platform {
    private int x, y, width, height;

    public Platform(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(x, y, width, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // Add these getter methods 👇
    public int getX() {
        return x;
    }

    public int getY() {  // Add getY() so the enemy knows the platform's height
        return y;
    }

    public int getWidth() {
        return width;
    }
}
