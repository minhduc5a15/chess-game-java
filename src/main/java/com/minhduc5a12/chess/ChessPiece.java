package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;

public abstract class ChessPiece {
    private final PieceColor color;
    private final String imagePath;

    public ChessPiece(PieceColor color, String imagePath) {
        this.color = color;
        this.imagePath = imagePath;
    }

    public PieceColor getColor() {
        return color;
    }

    public String getImagePath() {
        return imagePath;
    }

    public abstract boolean isValidMove(int startX, int startY, int endX, int endY, ChessTile[][] board);
}