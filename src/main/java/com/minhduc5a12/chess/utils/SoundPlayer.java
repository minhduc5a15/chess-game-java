package com.minhduc5a12.chess.utils;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.InputStream;

public class SoundPlayer {

    /**
     * Phát một file âm thanh MP3 từ đường dẫn trong thư mục resources.
     *
     * @param soundFilePath Đường dẫn đến file âm thanh MP3 trong thư mục resources.
     */
    public static void playSound(String soundFilePath) {
        try {
            ClassLoader classLoader = SoundPlayer.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(soundFilePath);

            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + soundFilePath);
            }

            Player player = new Player(inputStream);

            new Thread(() -> {
                try {
                    player.play();
                    player.close();
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (JavaLayerException e) {
            e.printStackTrace();
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
}