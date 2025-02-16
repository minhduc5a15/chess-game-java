package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;

public class Pawn extends ChessPiece {
    public Pawn(PieceColor color, String imagePath) {
        super(color, imagePath);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, ChessTile[][] board) {
        int direction = (getColor() == PieceColor.WHITE) ? -1 : 1; // Hướng di chuyển

        // Di chuyển tiến lên 1 ô
        if (endX == startX && endY == startY + direction && board[endY][endX].getPiece() == null) {
            return true;
        }

        // Di chuyển tiến lên 2 ô (chỉ áp dụng cho lần đầu tiên)
        if (endX == startX && endY == startY + 2 * direction && board[endY][endX].getPiece() == null) {
            if ((getColor() == PieceColor.WHITE && startY == 6) || (getColor() == PieceColor.BLACK && startY == 1)) {
                // Kiểm tra xem ô giữa có trống không
                if (board[startY + direction][startX].getPiece() == null) {
                    return true;
                }
            }
        }

        // Bắt quân chéo
        if (Math.abs(endX - startX) == 1 && endY == startY + direction && board[endY][endX].getPiece() != null) {
            ChessPiece targetPiece = board[endY][endX].getPiece();
            return targetPiece.getColor() != getColor();
        }

        return false;
    }
}