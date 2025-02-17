package com.minhduc5a12.chess.pieces;

import com.minhduc5a12.chess.ChessPiece;
import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.utils.BoardUtils;

public class Bishop extends ChessPiece {
    public Bishop(PieceColor color, String imagePath) {
        super(color, imagePath);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, ChessTile[][] board) {
        // Kiểm tra xem quân Tượng có di chuyển theo đường chéo không
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);
        if (dx != dy) {
            return false; // Quân Tượng chỉ di chuyển theo đường chéo
        }

        // Kiểm tra xem có quân cờ nào chặn đường không
        int directionX = (endX > startX) ? 1 : -1; // Hướng di chuyển theo cột
        int directionY = (endY > startY) ? 1 : -1; // Hướng di chuyển theo hàng

        int x = startX + directionX;
        int y = startY + directionY;
        while (x != endX && y != endY) {
            if (!BoardUtils.isWithinBoard(x, y) || board[y][x].getPiece() != null) {
                return false; // Có quân cờ chặn đường hoặc vượt ra khỏi bàn cờ
            }
            x += directionX;
            y += directionY;
        }

        // Kiểm tra xem ô đích có quân cờ cùng màu không
        ChessPiece targetPiece = board[endY][endX].getPiece();
        return targetPiece == null || targetPiece.getColor() != getColor(); // Không thể bắt quân cùng màu
    }
}