package com.minhduc5a12.chess.utils;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class SoundPlayer {
    private static final Logger logger = LoggerFactory.getLogger(SoundPlayer.class);

    public static void playSound(String soundFilePath) {
        try {
            ClassLoader classLoader = SoundPlayer.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(soundFilePath);
            if (inputStream == null) throw new IllegalArgumentException("File not found: " + soundFilePath);
            Player player = new Player(inputStream);
            new Thread(() -> {
                try {
                    player.play();
                    player.close();
                } catch (JavaLayerException e) {
                    logger.error("Error playing sound", e);
                }
            }).start();
        } catch (JavaLayerException e) {
            logger.error("Error playing sound", e);
        }
    }

    public static void preloadSounds() {
        String[] sounds = {
            "sounds/move-self.mp3", "sounds/capture.mp3",
            "sounds/castle.mp3", "sounds/move-check.mp3",
            "sounds/illegal.mp3"
        };
        for (String sound : sounds) {
            try {
                ClassLoader classLoader = SoundPlayer.class.getClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream(sound);
                if (inputStream == null) throw new IllegalArgumentException("File not found: " + sound);
                Player player = new Player(inputStream); // Khởi tạo Player
                player.close(); // Đóng ngay, không phát
            } catch (Exception e) {
                logger.error("Error preloading sound: {}", sound, e);
            }
        }
    }

    public static void playMoveSound() { playSound("sounds/move-self.mp3"); }
    public static void playCaptureSound() { playSound("sounds/capture.mp3"); }
    public static void playCastleSound() { playSound("sounds/castle.mp3"); }
    public static void playMoveCheckSound() { playSound("sounds/move-check.mp3"); }
    public static void playMoveIllegal() { playSound("sounds/illegal.mp3"); }
}