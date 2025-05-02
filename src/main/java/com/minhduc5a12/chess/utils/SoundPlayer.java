package com.minhduc5a12.chess.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class SoundPlayer {

    private static final Logger logger = LoggerFactory.getLogger(SoundPlayer.class);
    private static final ExecutorService soundExecutor = Executors.newFixedThreadPool(2);
    private static final Map<String, Boolean> soundCache = new HashMap<>();

    static {
        warmUpThreadPool();
        preloadSounds();
    }

    public static void playSound(String soundFilePath) {
        if (soundFilePath == null || soundFilePath.isEmpty()) {
            logger.warn("Invalid sound file path");
            return;
        }

        soundExecutor.submit(() -> {
            InputStream inputStream = null;
            try {
                ClassLoader classLoader = SoundPlayer.class.getClassLoader();
                inputStream = classLoader.getResourceAsStream(soundFilePath);
                if (inputStream == null) {
                    logger.error("Sound file not found: {}", soundFilePath);
                    return;
                }
                Player player = new Player(new BufferedInputStream(inputStream));
                player.play();
                player.close();
            } catch (JavaLayerException e) {
                logger.error("Error playing sound: {}", soundFilePath, e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (java.io.IOException | SecurityException e) {
                        logger.error("Error closing input stream for: {}", soundFilePath, e);
                    }
                }
            }
        });
    }

    private static void warmUpThreadPool() {
        soundExecutor.submit(() -> {
            logger.debug("Warming up sound thread pool");
        });
    }

    public static void preloadSounds() {
        String[] sounds = {"sounds/move-self.mp3", "sounds/capture.mp3", "sounds/castle.mp3", "sounds/move-check.mp3", "sounds/illegal.mp3"};
        for (String sound : sounds) {
            if (!soundCache.containsKey(sound)) {
                InputStream inputStream = null;
                try {
                    ClassLoader classLoader = SoundPlayer.class.getClassLoader();
                    inputStream = classLoader.getResourceAsStream(sound);
                    if (inputStream == null) {
                        logger.error("Sound file not found during preload: {}", sound);
                        continue;
                    }
                    BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
                    Player player = new Player(bufferedStream);
                    player.close();
                    soundCache.put(sound, true);
                    logger.debug("Preloaded sound: {}", sound);
                } catch (JavaLayerException e) {
                    logger.error("Error preloading sound: {}", sound, e);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (java.io.IOException | SecurityException e) {
                            logger.error("Error closing preload stream: {}", sound, e);
                        }
                    }
                }
            }
        }
    }

    public static void playMoveSound() {
        playSound("sounds/move-self.mp3");
    }

    public static void playCaptureSound() {
        playSound("sounds/capture.mp3");
    }

    public static void playCastleSound() {
        playSound("sounds/castle.mp3");
    }

    public static void playMoveCheckSound() {
        playSound("sounds/move-check.mp3");
    }

    public static void playMoveIllegal() {
        playSound("sounds/illegal.mp3");
    }

    public static void shutdown() {
        soundExecutor.shutdown();
    }
}
