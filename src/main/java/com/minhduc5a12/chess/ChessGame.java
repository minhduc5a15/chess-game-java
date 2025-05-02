package com.minhduc5a12.chess;

import com.minhduc5a12.chess.game.ChessLauncher;
import com.minhduc5a12.chess.utils.ImageLoader;
import com.minhduc5a12.chess.utils.SoundPlayer;

public class ChessGame {
    public static void main(String[] args) {
        // Set up JVM options for high DPI displays
        // This is a workaround for high DPI scaling issues on Windows
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            System.setProperty("sun.java2d.uiScale", "1.0");
            System.setProperty("sun.java2d.dpiaware", "true");
        }

        // Load resources
        ImageLoader.preloadImages();
        SoundPlayer.preloadSounds();

        // Launch the chess game
        ChessLauncher.launch();
    }
}