package com.minhduc5a12.chess.utils;

import com.minhduc5a12.chess.ChessPiece;
import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.pieces.King;

import java.util.Map;

public class BoardUtils {
    public static final int BOARD_SIZE = 8;

    public static boolean isWithinBoard(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    public static boolean isKingInCheck(ChessTile[][] board, PieceColor color, Map<ChessPiece, int[]> piecePositions) {
        for (Map.Entry<ChessPiece, int[]> entry : piecePositions.entrySet()) {
            ChessPiece piece = entry.getKey();
            if (piece instanceof King && piece.getColor() == color) {
                int[] kingPosition = entry.getValue();
                int kingX = kingPosition[0];
                int kingY = kingPosition[1];

                for (Map.Entry<ChessPiece, int[]> enemyEntry : piecePositions.entrySet()) {
                    ChessPiece enemyPiece = enemyEntry.getKey();
                    if (enemyPiece.getColor() != color) {
                        int[] enemyPosition = enemyEntry.getValue();
                        int enemyX = enemyPosition[0];
                        int enemyY = enemyPosition[1];

                        if (enemyPiece.isValidMove(enemyX, enemyY, kingX, kingY, board)) {
                            return true;
                        }
                    }
                }
                break;
            }
        }
        return false;
    }

    public static boolean isCheckmate(ChessTile[][] board, PieceColor color, Map<ChessPiece, int[]> piecePositions) {
        if (!isKingInCheck(board, color, piecePositions)) {
            return false;
        }

        int[] kingPosition = null;
        for (Map.Entry<ChessPiece, int[]> entry : piecePositions.entrySet()) {
            ChessPiece piece = entry.getKey();
            if (piece instanceof King && piece.getColor() == color) {
                kingPosition = entry.getValue();
                break;
            }
        }
        if (kingPosition == null) {
            return false;
        }

        int kingX = kingPosition[0];
        int kingY = kingPosition[1];

        int[][] directions = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] dir : directions) {
            int newX = kingX + dir[0];
            int newY = kingY + dir[1];

            if (isWithinBoard(newX, newY)) {
                ChessPiece targetPiece = board[newY][newX].getPiece();
                if (targetPiece == null || targetPiece.getColor() != color) {
                    ChessPiece originalPiece = board[newY][newX].getPiece();
                    board[newY][newX].setPiece(board[kingY][kingX].getPiece());
                    board[kingY][kingX].setPiece(null);

                    boolean stillInCheck = isKingInCheck(board, color, piecePositions);

                    board[kingY][kingX].setPiece(board[newY][newX].getPiece());
                    board[newY][newX].setPiece(originalPiece);

                    if (!stillInCheck) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static boolean isMoveValidUnderCheck(int startX, int startY, int endX, int endY, ChessTile[][] board, PieceColor currentColor, Map<ChessPiece, int[]> piecePositions) {
        ChessPiece movingPiece = board[startY][startX].getPiece();
        ChessPiece targetPiece = board[endY][endX].getPiece();

        // Thử di chuyển tạm thời
        board[endY][endX].setPiece(movingPiece);
        board[startY][startX].setPiece(null);

        // Cập nhật piecePositions tạm thời
        int[] oldPosition = piecePositions.get(movingPiece);
        piecePositions.put(movingPiece, new int[]{endX, endY});
        if (targetPiece != null) {
            piecePositions.remove(targetPiece);
        }

        // Kiểm tra chiếu
        boolean isKingInCheck = isKingInCheck(board, currentColor, piecePositions);

        // Hoàn tác
        board[startY][startX].setPiece(movingPiece);
        board[endY][endX].setPiece(targetPiece);
        piecePositions.put(movingPiece, oldPosition);
        if (targetPiece != null) {
            piecePositions.put(targetPiece, new int[]{endX, endY});
        }

        return !isKingInCheck;
    }
}