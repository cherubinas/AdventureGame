import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class AnimationLoader {
    public static HashMap<String, AnimationData> loadAllAnimations() {
        HashMap<String, AnimationData> animations = new HashMap<>();
        String basePath = "C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\";

        addAnimation(animations, "idle", basePath + "1. Idle 48 x 48.png", 4, 48, 48, 0);
        addAnimation(animations, "run", basePath + "2. Run 48 x 48.png", 6, 48, 48, 0);
        addAnimation(animations, "jump", basePath + "3. Jump 48 x 48.png", 2, 48, 48, 0);
        addAnimation(animations, "fall", basePath + "4. Fall 48 x 48.png", 2, 48, 48, 0);
        addAnimation(animations, "attack", basePath + "5. Attack 131 x 56.png", 4, 131, 56, -1000); // Offset player backward
        addAnimation(animations, "dash", basePath + "6. Dash 112 x 56.png", 4, 112, 56, -10);
        addAnimation(animations, "hit", basePath + "7. Hit 48 x 48.png", 4, 48, 48, 0);
        addAnimation(animations, "death", basePath + "8. Death 76 x 48.png", 10, 76, 48, 0);

        // Enemy animations
        addAnimation(animations, "enemy_idle", basePath + "Skeleton Idle.png", 11, 24, 32, 0);
        addAnimation(animations, "enemy_run", basePath + "Skeleton Walk.png", 13, 22, 33, 0);
        addAnimation(animations, "enemy_attack", basePath + "Skeleton Attack.png", 18, 43, 37, -10);
        addAnimation(animations, "enemy_hurt", basePath + "Skeleton Hit.png", 8, 30, 32, 0);
        addAnimation(animations, "enemy_death", basePath + "Skeleton Dead.png", 15, 33, 32, 0);
        addAnimation(animations, "enemy_react", basePath + "Skeleton React.png", 4, 22, 32, 0);
        // Crow animations
        addAnimation(animations, "npc_idle", basePath + "Mushroom-idle.png", 7, 80, 64, 0);
        // Items (from sprite sheet)
        String itemsPath = basePath + "Pack.png";
        addItem(animations, "item_sword", itemsPath, 1, 16, 16);
        addItem(animations, "item_armor", itemsPath, 9, 16, 16);
        addItem(animations, "item_health_potion", itemsPath, 17, 16, 16);
        addItem(animations, "item_jump_potion", itemsPath, 19, 16, 16);
        addItem(animations, "item_key", itemsPath, 22, 16, 16);
        return animations;
    }

    private static void addAnimation(HashMap<String, AnimationData> animations, String name, String path, int frameCount, int frameWidth, int frameHeight, int xOffset) {
        ArrayList<BufferedImage> frames = loadAnimation(path, frameCount, frameWidth, frameHeight);

        if (frames.isEmpty()) {
            System.out.println("🚨 ERROR: Animation '" + name + "' failed to load! Check the file path: " + path);
        } else {
            animations.put(name, new AnimationData(frames, xOffset));
        }
    }

    private static ArrayList<BufferedImage> loadAnimation(String path, int frameCount, int frameWidth, int frameHeight) {
        ArrayList<BufferedImage> frames = new ArrayList<>();
        int targetWidth = 50;
        int targetHeight = 50;

        try {
            BufferedImage spritesheet = ImageIO.read(new File(path));
            if (spritesheet == null) {
                throw new Exception("Image file not found or corrupted.");
            }

            for (int i = 0; i < frameCount; i++) {
                if ((i + 1) * frameWidth > spritesheet.getWidth()) {
                    System.out.println("⚠️ WARNING: Frame " + i + " out of bounds for image: " + path);
                    break;
                }

                BufferedImage frame = spritesheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);

                double scale = (double) targetHeight / frameHeight;
                int newWidth = (int) (frameWidth * scale);
                int newHeight = targetHeight;

                int xOffset = (targetWidth - newWidth) / 2;

                BufferedImage resizedFrame = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = resizedFrame.createGraphics();
                g2d.drawImage(frame.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), xOffset, 0, null);
                g2d.dispose();

                frames.add(resizedFrame);
            }
        } catch (Exception e) {
            System.out.println("❌ Error loading animation: " + path + " -> " + e.getMessage());
        }

        if (frames.isEmpty()) {
            System.out.println("⚠️ WARNING: Animation at " + path + " loaded 0 frames.");
        }

        return frames;
    }
    private static void addItem(HashMap<String, AnimationData> animations, String name, String path, int index, int cellWidth, int cellHeight) {
        ArrayList<BufferedImage> frames = new ArrayList<>();
        int targetWidth = 50;
        int targetHeight = 50;

        try {
            BufferedImage spriteSheet = ImageIO.read(new File(path));
            int cols = spriteSheet.getWidth() / cellWidth;

            int row = index / cols;
            int col = index % cols;

            BufferedImage item = spriteSheet.getSubimage(col * cellWidth, row * cellHeight, cellWidth, cellHeight);

            // Resize for consistency
            double scale = (double) targetHeight / cellHeight;
            int newWidth = (int) (cellWidth * scale);
            int newHeight = targetHeight;
            int xOffset = (targetWidth - newWidth) / 2;

            BufferedImage resizedItem = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedItem.createGraphics();
            g2d.drawImage(item.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), xOffset, 0, null);
            g2d.dispose();

            frames.add(resizedItem);
        } catch (Exception e) {
            System.out.println("❌ Error loading item '" + name + "': " + e.getMessage());
        }

        if (!frames.isEmpty()) {
            animations.put(name, new AnimationData(frames, 0));
        } else {
            System.out.println("⚠️ WARNING: Item '" + name + "' failed to load.");
        }
    }

    public static class AnimationData {
        public ArrayList<BufferedImage> frames;
        public int xOffset;

        public AnimationData(ArrayList<BufferedImage> frames, int xOffset) {
            this.frames = frames;
            this.xOffset = xOffset;
        }
    }
}
