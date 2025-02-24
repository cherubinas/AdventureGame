import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class AnimationLoader {
    public static HashMap<String, ArrayList<BufferedImage>> loadAllAnimations() {
        HashMap<String, ArrayList<BufferedImage>> animations = new HashMap<>();
        animations.put("idle", loadAnimation("AdventureGame/lib/photos/1. Idle 48 x 48.png", 4, 48, 48));
        animations.put("run", loadAnimation("AdventureGame/lib/photos/2. Run 48 x 48.png", 6, 48, 48));
        animations.put("jump", loadAnimation("AdventureGame/lib/photos/3. Jump 48 x 48.png", 2, 48, 48));
        animations.put("fall", loadAnimation("AdventureGame/lib/photos/4. Fall 48 x 48.png", 2, 48, 48));
        animations.put("attack", loadAnimation("AdventureGame/lib/photos/5. Attack 131 x 56.png", 4, 131, 56));
        animations.put("dash", loadAnimation("AdventureGame/lib/photos/6. Dash 112 x 56.png", 4, 112, 56));
        animations.put("hit", loadAnimation("AdventureGame/lib/photos/7. Hit 48 x 48.png", 2, 48, 48));
        animations.put("death", loadAnimation("AdventureGame/lib/photos/8. Death 76 x 48.png", 6, 76, 48));
        return animations;
    }

    private static ArrayList<BufferedImage> loadAnimation(String path, int frameCount, int width, int height) {
        ArrayList<BufferedImage> frames = new ArrayList<>();
        try {
            BufferedImage spritesheet = ImageIO.read(new File(path));
            for (int i = 0; i < frameCount; i++) {
                frames.add(spritesheet.getSubimage(i * width, 0, width, height));
            }
        } catch (Exception e) {
            System.out.println("Error loading animation: " + e.getMessage());
        }
        return frames;
    }
}
