import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.swing.*;
import java.io.File;

public class MusicPlayer {
    private static boolean javafxInitialized = false;
    private static JFXPanel fxPanel; // Keeps JavaFX initialized

    private MediaPlayer mediaPlayer;
    private static double globalVolume = 1.0; // 0.0 (silent) to 1.0 (full)
    private static boolean musicMuted = false;
    private static boolean soundEffectsMuted = false;

    private static void initFX() {
        if (!javafxInitialized) {
            javafxInitialized = true;
            SwingUtilities.invokeLater(() -> {
                fxPanel = new JFXPanel(); // Initializes JavaFX
                System.out.println("JavaFX initialized (via invokeLater).");
            });
        }
    }

    // Constructor for background music
    public MusicPlayer(String filepath) {
        initFX();

        try {
            Media media = new Media(new File(filepath).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // loop
            mediaPlayer.setVolume(musicMuted ? 0 : globalVolume);

            mediaPlayer.setOnError(() ->
                    System.out.println("MediaPlayer error: " + mediaPlayer.getError()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(musicMuted ? 0 : globalVolume);
            mediaPlayer.play();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    public void setVolume(double volume) {
        globalVolume = volume;
        if (mediaPlayer != null && !musicMuted) {
            mediaPlayer.setVolume(globalVolume);
        }
    }

    public static void setGlobalVolume(double volume) {
        globalVolume = volume;
    }

    public static double getGlobalVolume() {
        return globalVolume;
    }

    public void setMuted(boolean muted) {
        musicMuted = muted;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(muted ? 0 : globalVolume);
        }
    }

    public static void setSoundEffectsMuted(boolean muted) {
        soundEffectsMuted = muted;
    }

    public static boolean areSoundEffectsMuted() {
        return soundEffectsMuted;
    }

    // Static method to play short sound effects
    public static void playSound(String filepath) {
        initFX();

        if (soundEffectsMuted) {
            System.out.println("Sound effects are muted.");
            return;
        }

        Platform.runLater(() -> {
            try {
                Media media = new Media(new File(filepath).toURI().toString());
                MediaPlayer soundEffectPlayer = new MediaPlayer(media);
                soundEffectPlayer.setVolume(globalVolume);

                soundEffectPlayer.setOnEndOfMedia(() -> soundEffectPlayer.dispose());
                soundEffectPlayer.setOnError(() ->
                        System.out.println("SFX MediaPlayer error: " + soundEffectPlayer.getError()));

                soundEffectPlayer.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
