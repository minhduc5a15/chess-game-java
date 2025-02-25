package com.minhduc5a12.chess.pieces;

import com.minhduc5a12.chess.ChessPiece;
import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.model.Move;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.utils.BoardUtils;

import java.util.ArrayList;
import java.util.List;

public class King extends ChessPiece {
    public King(PieceColor color, String imagePath) {
        super(color, imagePath);
    }

    @Override
    public List<Move> generateValidMoves(int startX, int startY, ChessTile[][] board) {
        List<Move> validMoves = new ArrayList<>();

        // Các hướng di chuyển (1 ô theo mọi hướng)
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, // Ngang và dọc
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1} // Chéo
        };

        for (int[] dir : directions) {
            int newX = startX + dir[0];
            int newY = startY + dir[1];

            if (BoardUtils.isWithinBoard(newX, newY)) {
                ChessPiece targetPiece = board[newY][newX].getPiece();
                if (targetPiece == null || targetPiece.getColor() != getColor()) {
                    validMoves.add(new Move(startX, startY, newX, newY));
                }
            }
        }

        // Kiểm tra nhập thành
        if (!hasMoved()) {
            // Nhập thành cánh Vua (King-side castling)
            if (canCastle(startX, startY, 7, board)) {
                validMoves.add(new Move(startX, startY, startX + 2, startY));
            }
            // Nhập thành cánh Hậu (Queen-side castling)
            if (canCastle(startX, startY, 0, board)) {
                validMoves.add(new Move(startX, startY, startX - 2, startY));
            }
        }

        return validMoves;
    }

    private boolean canCastle(int startX, int startY, int rookX, ChessTile[][] board) {
        int direction = (rookX > startX) ? 1 : -1;
        int x = startX + direction;

        // Kiểm tra không có quân cờ nào giữa Vua và Xe
        while (x != rookX) {
            if (board[startY][x].getPiece() != null) {
                return false;
            }
            x += direction;
        }

        // Kiểm tra Xe chưa di chuyển
        ChessPiece rook = board[startY][rookX].getPiece();
        return rook instanceof Rook && !rook.hasMoved();
    }
}