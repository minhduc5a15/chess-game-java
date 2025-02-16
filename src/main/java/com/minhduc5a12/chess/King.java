package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;

public class King extends ChessPiece {
    public King(PieceColor color, String imagePath) {
        super(color, imagePath);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, ChessTile[][] board) {
        // Tính toán sự chênh lệch giữa vị trí đầu và cuối
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);

        // Quân Vua chỉ di chuyển 1 ô theo bất kỳ hướng nào
        if (dx > 1 || dy > 1) {
            return false;
        }

        // Kiểm tra xem ô đích có quân cờ cùng màu không
        ChessPiece targetPiece = board[endY][endX].getPiece();
        return targetPiece == null || targetPiece.getColor() != getColor(); // Không thể bắt quân cùng màu
    }
}