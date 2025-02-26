package com.minhduc5a12.chess;

import com.minhduc5a12.chess.utils.ImageLoader;
import com.minhduc5a12.chess.utils.SoundPlayer;

public class ChessGame {
    public static void main(String[] args) {
        ImageLoader.preloadImages();
        SoundPlayer.preloadSounds();
        new ChessPanel();
    }
}
