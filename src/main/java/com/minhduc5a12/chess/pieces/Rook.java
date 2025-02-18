package com.minhduc5a12.chess.pieces;

import com.minhduc5a12.chess.ChessPiece;
import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.utils.BoardUtils;

public class Rook extends ChessPiece {
    public Rook(PieceColor color, String imagePath) {
        super(color, imagePath);
        this.setPieceValue(5);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, ChessTile[][] board) {
        // Kiểm tra xem tọa độ đích có nằm trong bàn cờ không
        if (!BoardUtils.isWithinBoard(endX, endY)) {
            return false;
        }

        // Kiểm tra xem quân Xe có di chuyển theo hàng ngang hoặc dọc không
        if (startX != endX && startY != endY) {
            return false; // Quân Xe chỉ di chuyển theo hàng ngang hoặc dọc
        }

        // Kiểm tra xem có quân cờ nào chặn đường không
        if (startX == endX) {
            // Di chuyển theo cột (dọc)
            int direction = (endY > startY) ? 1 : -1;
            for (int y = startY + direction; y != endY; y += direction) {
                if (!BoardUtils.isWithinBoard(startX, y)) {
                    return false; // Vượt ra khỏi bàn cờ
                }
                if (board[y][startX].getPiece() != null) {
                    return false; // Có quân cờ chặn đường
                }
            }
        } else {
            // Di chuyển theo hàng (ngang)
            int direction = (endX > startX) ? 1 : -1;
            for (int x = startX + direction; x != endX; x += direction) {
                if (!BoardUtils.isWithinBoard(x, startY)) {
                    return false; // Vượt ra khỏi bàn cờ
                }
                if (board[startY][x].getPiece() != null) {
                    return false; // Có quân cờ chặn đường
                }
            }
        }

        // Kiểm tra xem ô đích có quân cờ cùng màu không
        ChessPiece targetPiece = board[endY][endX].getPiece();
        return targetPiece == null || targetPiece.getColor() != getColor(); // Không thể bắt quân cùng màu
    }
}