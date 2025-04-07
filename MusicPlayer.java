import javafx.embed.swing.JFXPanel; // Needed to init JavaFX
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class MusicPlayer {
    private MediaPlayer mediaPlayer;

    public MusicPlayer(String filepath) {
        new JFXPanel(); // Initializes JavaFX

        try {
            Media media = new Media(new File(filepath).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop music
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}
