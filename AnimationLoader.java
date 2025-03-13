import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class AnimationLoader {
    public static HashMap<String, ArrayList<BufferedImage>> loadAllAnimations() {
        HashMap<String, ArrayList<BufferedImage>> animations = new HashMap<>();
        animations.put("idle", loadAnimation("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\1. Idle 48 x 48.png", 4, 48, 48));
        animations.put("run", loadAnimation("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\2. Run 48 x 48.png", 6, 48, 48));
        animations.put("jump", loadAnimation("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\3. Jump 48 x 48.png", 2, 48, 48));
        animations.put("fall", loadAnimation("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\4. Fall 48 x 48.png", 2, 48, 48));
        animations.put("attack", loadAnimation("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\5. Attack 131 x 56.png", 4, 131, 56));
        animations.put("dash", loadAnimation("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\6. Dash 112 x 56.png", 4, 112, 56));
        animations.put("hit", loadAnimation("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\7. Hit 48 x 48.png", 4, 48, 48));
        animations.put("death", loadAnimation("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\8. Death 76 x 48.png", 10, 76, 48));

        // Enemy animations (new additions)
        animations.put("enemy_idle", loadAnimation("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\Skeleton Idle.png", 11, 24, 32)); // Idle - 11 frames, 24x32 each
        animations.put("enemy_run", loadAnimation("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\Skeleton Walk.png", 13, 22, 33)); // Walk - 13 frames, 22x33 each
        animations.put("enemy_attack", loadAnimation("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\Skeleton Attack.png", 18, 43, 37)); // Attack - 18 frames, 43x37 each
        animations.put("enemy_hurt", loadAnimation("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\Skeleton Hit.png", 8, 30, 32)); // Hit - 8 frames, 30x32 each
        animations.put("enemy_death", loadAnimation("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\Skeleton Dead.png", 15, 33, 32)); // Death - 15 frames, 33x32 each
        animations.put("enemy_react", loadAnimation("C:\\Users\\eveli\\Desktop\\New folder (3)\\AdventureGame\\lib\\photos\\Skeleton React.png", 4, 22, 32));
        return animations;
    }

    private static ArrayList<BufferedImage> loadAnimation(String path, int frameCount, int frameWidth, int frameHeight) {
        ArrayList<BufferedImage> frames = new ArrayList<>();
        int targetWidth = 50;  // Standard enemy width
        int targetHeight = 50; // Standard enemy height

        try {
            BufferedImage spritesheet = ImageIO.read(new File(path));
            for (int i = 0; i < frameCount; i++) {
                BufferedImage frame = spritesheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);

                // **Ensure height matches targetHeight fully**
                double scale = (double) targetHeight / frameHeight; // Always scale to match height
                int newWidth = (int) (frameWidth * scale);
                int newHeight = targetHeight; // Match height exactly

                // Center horizontally inside 50x50
                int xOffset = (targetWidth - newWidth) / 2;

                BufferedImage resizedFrame = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = resizedFrame.createGraphics();
                g2d.drawImage(frame.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), xOffset, 0, null);
                g2d.dispose();

                frames.add(resizedFrame);
            }
        } catch (Exception e) {
            System.out.println("Error loading animation: " + e.getMessage());
        }
        return frames;
    }
}
