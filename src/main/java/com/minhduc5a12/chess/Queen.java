package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.utils.BoardUtils;

public class Queen extends ChessPiece {
    public Queen(PieceColor color, String imagePath) {
        super(color, imagePath);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, ChessTile[][] board) {
        // Kiểm tra xem quân Hậu có di chuyển theo hàng ngang, dọc hoặc chéo không
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);

        if (dx != 0 && dy != 0 && dx != dy) {
            return false; // Quân Hậu chỉ di chuyển theo hàng ngang, dọc hoặc chéo
        }

        // Kiểm tra xem có quân cờ nào chặn đường không
        int directionX = Integer.compare(endX, startX); // Hướng di chuyển theo cột
        int directionY = Integer.compare(endY, startY); // Hướng di chuyển theo hàng

        int x = startX + directionX;
        int y = startY + directionY;
        while (x != endX || y != endY) {
            // Kiểm tra xem chỉ số hàng và cột có hợp lệ không
            if (BoardUtils.isWithinBoard(x, y)) return false;

            if (board[y][x].getPiece() != null) {
                return false; // Có quân cờ chặn đường
            }
            x += directionX;
            y += directionY;
        }

        // Kiểm tra xem ô đích có quân cờ cùng màu không
        ChessPiece targetPiece = board[endY][endX].getPiece();
        return targetPiece == null || targetPiece.getColor() != getColor(); // Không thể bắt quân cùng màu
    }
}