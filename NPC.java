import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;

public class NPC {
    private int x, y;
    private BufferedImage[] idleFrames;
    private int frameIndex = 0;
    private int frameDelay = 10;
    private int frameTimer = 0;
    private boolean showText = false;
    private int currentLineIndex = 0;
    private ArrayList<String> dialogueLines;
    private Rectangle bounds;
    private BufferedImage spriteSheet;
    private Font pixelFont;
    private boolean showPrompt = false;

    public NPC(int x, int y, String spritePath) {
        this.x = x;
        this.y = y;
        loadIdleAnimation(spritePath);
        loadDialogue();
        loadPixelFont();
        bounds = new Rectangle(x, y, 64, 64);
    }

    private void loadIdleAnimation(String spritePath) {
        try {
            spriteSheet = ImageIO.read(new File(spritePath));
            idleFrames = new BufferedImage[7];
            for (int i = 0; i < 7; i++) {
                idleFrames[i] = spriteSheet.getSubimage(i * 80, 0, 80, 64);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDialogue() {
        dialogueLines = new ArrayList<>();
        dialogueLines.add("Cave Mushroom: Hello, traveler!");
        dialogueLines.add("Cave Mushroom: I've been waiting for someone like you.");
        dialogueLines.add("Cave Mushroom: The cave ahead is dangerous.");
        dialogueLines.add("Cave Mushroom: Take this advice - beat a lot of enemies.");
        dialogueLines.add("Cave Mushroom: That will show you the way out.");
        dialogueLines.add("Cave Mushroom: Good luck on your journey!");
        dialogueLines.add("Cave Mushroom: I hope you will not die!");
    }

    private void loadPixelFont() {
        try {
            // Replace this path with a real pixel font if available
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/fonts/pixel.ttf")).deriveFont(20f);
        } catch (Exception e) {
            pixelFont = new Font("Monospaced", Font.BOLD, 20); // fallback font
        }
    }

    public void update() {
        frameTimer++;
        if (frameTimer >= frameDelay) {
            frameTimer = 0;
            frameIndex = (frameIndex + 1) % idleFrames.length;
        }
    }
    private void drawPromptBox(Graphics g) {
        int boxX = x - 40;
        int boxY = y - 50;
        int boxWidth = 320;
        int boxHeight = 50;

        // White border
        g.setColor(Color.WHITE);
        g.fillRect(boxX - 2, boxY - 2, boxWidth + 4, boxHeight + 4);

        // Black inner box
        g.setColor(Color.BLACK);
        g.fillRect(boxX, boxY, boxWidth, boxHeight);

        // Prompt text (smaller font and two lines)
        g.setColor(Color.WHITE);
        g.setFont(pixelFont.deriveFont(12f)); // Temporarily smaller font

        g.drawString("Press E to talk", boxX + 10, boxY + 20);
        g.drawString("Press SPACE or click to continue", boxX + 10, boxY + 35);
    }


    public void render(Graphics g, int cameraX) {
        g.drawImage(idleFrames[frameIndex], x - cameraX, y, null);
        if (showPrompt) {
            drawPromptBox(g);
        }
        if (showText) {
            drawTextBox(g);
        }
    }


    private void drawTextBox(Graphics g) {
        int boxX = 250;
        int boxY = 200;
        int boxWidth = 700;
        int boxHeight = 120;

        // Draw white border
        g.setColor(Color.WHITE);
        g.fillRect(boxX - 2, boxY - 2, boxWidth + 4, boxHeight + 4);

        // Draw black box inside
        g.setColor(Color.BLACK);
        g.fillRect(boxX, boxY, boxWidth, boxHeight);

        // Draw text
        g.setColor(Color.WHITE);
        g.setFont(pixelFont);
        g.drawString(dialogueLines.get(currentLineIndex), boxX + 20, boxY + 60);
    }

    public void checkInteraction(Rectangle playerBounds, boolean isEPressed) {
        if (playerBounds.intersects(bounds)) {
            showPrompt = true;
            if (isEPressed) {
                showText = true;
                showPrompt = false; // Hide prompt when dialogue starts
            }
        } else {
            showPrompt = false;
            showText = false;
        }
    }

    public void handleMouseClick() {
        if (showText && currentLineIndex < dialogueLines.size() - 1) {
            currentLineIndex++;
        } else {
            showText = false;
        }
        showPrompt = false;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }
}
