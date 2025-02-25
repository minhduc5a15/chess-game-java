package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.model.Move;

import java.util.List;

public abstract class ChessPiece {
    private final PieceColor color;
    private final String imagePath;
    private int pieceValue = 0;
    private boolean hasMoved = false;

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

    /**
     * Phương thức trừu tượng để sinh ra tất cả các nước đi hợp lệ của quân cờ.
     *
     * @param startX Tọa độ cột ban đầu.
     * @param startY Tọa độ hàng ban đầu.
     * @param board  Bàn cờ hiện tại.
     * @return Danh sách các nước đi hợp lệ.
     */
    public abstract List<Move> generateValidMoves(int startX, int startY, ChessTile[][] board);

    /**
     * Kiểm tra xem nước đi có hợp lệ không bằng cách kiểm tra xem nó có nằm trong danh sách các nước đi hợp lệ không.
     *
     * @param startX Tọa độ cột ban đầu.
     * @param startY Tọa độ hàng ban đầu.
     * @param endX   Tọa độ cột đích.
     * @param endY   Tọa độ hàng đích.
     * @param board  Bàn cờ hiện tại.
     * @return true nếu nước đi hợp lệ, false nếu không.
     */
    public boolean isValidMove(int startX, int startY, int endX, int endY, ChessTile[][] board) {
        List<Move> validMoves = generateValidMoves(startX, startY, board);
        Move currentMove = new Move(startX, startY, endX, endY);
        return validMoves.contains(currentMove);
    }
}