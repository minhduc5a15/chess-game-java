package com.minhduc5a12.chess;

import com.minhduc5a12.chess.utils.ImageLoader;
import com.minhduc5a12.chess.utils.SoundPlayer;

public class ChessGame {
    public static void main(String[] args) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            System.setProperty("sun.java2d.uiScale", "1.0");
        }

        ImageLoader.preloadImages();    
        SoundPlayer.preloadSounds();
        new ChessPanel();
    }
}
