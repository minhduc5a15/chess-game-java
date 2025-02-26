package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.model.Move;
import com.minhduc5a12.chess.utils.ImageLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public abstract class ChessPiece {
    private final PieceColor color;
    private final String imagePath;
    private final Image image; // Lưu ảnh đã resize
    private int pieceValue = 0;
    private boolean hasMoved = false;

    public ChessPiece(PieceColor color, String imagePath) {
        this.color = color;
        this.imagePath = imagePath;
        this.image = ImageLoader.getImage(imagePath); // Tải và resize ngay khi khởi tạo
    }

    public Image getImage() {
        return image;
    }

    // Các getter/setter và method khác giữ nguyên
    public PieceColor getColor() {
        return color;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public void setPieceValue(int value) {
        this.pieceValue = value;
    }

    public int getPieceValue() {
        return pieceValue;
    }

    public abstract java.util.List<Move> generateValidMoves(int startX, int startY, ChessTile[][] board);

    public boolean isValidMove(int startX, int startY, int endX, int endY, ChessTile[][] board) {
        java.util.List<Move> validMoves = generateValidMoves(startX, startY, board);
        Move currentMove = new Move(startX, startY, endX, endY);
        return validMoves.contains(currentMove);
    }
}