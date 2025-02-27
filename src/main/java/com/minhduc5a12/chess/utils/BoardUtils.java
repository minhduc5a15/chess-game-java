package com.minhduc5a12.chess.utils;

import com.minhduc5a12.chess.ChessPiece;
import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.model.Move;
import com.minhduc5a12.chess.pieces.King;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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
                            LoggerFactory.getLogger(BoardUtils.class).info("King of {} in check by {} at ({},{})", color, enemyPiece, enemyX, enemyY);
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
        Logger logger = LoggerFactory.getLogger(BoardUtils.class);
        logger.info("Checking if {} is in checkmate", color);
        if (!isKingInCheck(board, color, piecePositions)) {
            logger.info("King of {} not in check", color);
            return false;
        }

        // Lấy danh sách quân cờ của bên bị chiếu trước để tránh lặp trực tiếp trên Map
        List<Map.Entry<ChessPiece, int[]>> pieces = new ArrayList<>(piecePositions.entrySet());

        for (Map.Entry<ChessPiece, int[]> entry : pieces) {
            ChessPiece piece = entry.getKey();
            if (piece.getColor() == color) {
                int[] position = entry.getValue();
                int startX = position[0];
                int startY = position[1];
                List<Move> validMoves = piece.generateValidMoves(startX, startY, board);

                for (Move move : validMoves) {
                    int endX = move.endX();
                    int endY = move.endY();

                    // Thử di chuyển tạm thời trên bàn cờ
                    ChessPiece targetPiece = board[endY][endX].getPiece();
                    board[endY][endX].setPiece(piece);
                    board[startY][startX].setPiece(null);

                    // Tạm lưu trạng thái để không sửa Map gốc
                    Map<ChessPiece, int[]> tempPositions = new java.util.HashMap<>(piecePositions);
                    tempPositions.put(piece, new int[]{endX, endY});
                    if (targetPiece != null) tempPositions.remove(targetPiece);

                    boolean stillInCheck = isKingInCheck(board, color, tempPositions);

                    // Hoàn tác
                    board[startY][startX].setPiece(piece);
                    board[endY][endX].setPiece(targetPiece);

                    if (!stillInCheck) {
                        logger.info("Escape found for {} with move ({},{}) to ({},{})", color, startX, startY, endX, endY);
                        return false;
                    }
                }
            }
        }
        logger.info("No escape for {}, checkmate confirmed", color);
        return true;
    }

    public static boolean isMoveValidUnderCheck(int startX, int startY, int endX, int endY, ChessTile[][] board, PieceColor currentColor, Map<ChessPiece, int[]> piecePositions) {
        ChessPiece movingPiece = board[startY][startX].getPiece();
        ChessPiece targetPiece = board[endY][endX].getPiece();

        board[endY][endX].setPiece(movingPiece);
        board[startY][startX].setPiece(null);

        int[] oldPosition = piecePositions.get(movingPiece);
        piecePositions.put(movingPiece, new int[]{endX, endY});
        if (targetPiece != null) {
            piecePositions.remove(targetPiece);
        }

        boolean isKingInCheck = isKingInCheck(board, currentColor, piecePositions);

        board[startY][startX].setPiece(movingPiece);
        board[endY][endX].setPiece(targetPiece);
        piecePositions.put(movingPiece, oldPosition);
        if (targetPiece != null) {
            piecePositions.put(targetPiece, new int[]{endX, endY});
        }

        return !isKingInCheck;
    }
}