package com.minhduc5a12.chess.utils;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.model.BoardState;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;
import com.minhduc5a12.chess.core.pieces.*;
import com.minhduc5a12.chess.game.BoardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.minhduc5a12.chess.constants.GameConstants.Board.BOARD_SIZE;

public class BoardUtils {

    private static final Logger logger = LoggerFactory.getLogger(BoardUtils.class);

    public static boolean isWithinBoard(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    public static boolean isKingInCheck(PieceColor color, ChessPieceMap pieceMap) {
        ChessPosition kingPosition = pieceMap.getKingPosition(color);
        if (kingPosition == null) {
            return false;
        }

        PieceColor opponentColor = (color == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        ChessPosition opponentKingPos = pieceMap.getKingPosition(opponentColor);

        if (opponentKingPos != null && Math.abs(kingPosition.col() - opponentKingPos.col()) <= 1 && Math.abs(kingPosition.row() - opponentKingPos.row()) <= 1) {
            return true;
        }

        for (Map.Entry<ChessPosition, ChessPiece> enemyEntry : pieceMap.getPieceMap().entrySet()) {
            ChessPosition enemyPosition = enemyEntry.getKey();
            ChessPiece enemyPiece = enemyEntry.getValue();

            if (enemyPiece.getColor() != opponentColor || enemyPiece instanceof King) {
                continue;
            }

            List<ChessMove> possibleAttacks = enemyPiece.generateValidMoves(enemyPosition, pieceMap);
            for (ChessMove move : possibleAttacks) {
                if (move.end().equals(kingPosition)) {
                    logger.info("King of {} in check by {} at {}", color, enemyPiece.getClass().getSimpleName(), enemyPosition.toChessNotation());
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isCheckmate(PieceColor color, ChessPieceMap pieceMap) {
        if (!isKingInCheck(color, pieceMap)) {
            return false;
        }

        for (ChessPosition pos : pieceMap.getPieceMap().keySet()) {
            ChessPiece piece = pieceMap.getPiece(pos);
            if (piece.getColor() == color) {
                List<ChessMove> moves = piece.generateValidMoves(pos, pieceMap);
                for (ChessMove move : moves) {
                    ChessPieceMap tempMap = simulateMove(move, pieceMap);
                    if (!isKingInCheck(color, tempMap)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean isMoveValidUnderCheck(ChessMove move, ChessPieceMap pieceMap) {
        ChessPiece piece = pieceMap.getPiece(move.start());
        if (piece == null) {
            return false;
        }

        PieceColor color = piece.getColor();

        if (!isKingInCheck(color, pieceMap)) {
            return piece.isValidMove(move, pieceMap);
        }

        ChessPieceMap tempMap = simulateMove(move, pieceMap);
        return !isKingInCheck(color, tempMap);
    }

    public static ChessPieceMap simulateMove(ChessMove move, ChessPieceMap pieceMap) {
        ChessPieceMap tempMap = new ChessPieceMap();
        for (Map.Entry<ChessPosition, ChessPiece> entry : pieceMap.getPieceMap().entrySet()) {
            ChessPosition pos = entry.getKey();
            ChessPiece piece = entry.getValue();
            tempMap.setPiece(pos, piece);
        }

        ChessPiece piece = tempMap.getPiece(move.start());
        tempMap.removePiece(move.end());
        tempMap.removePiece(move.start());
        tempMap.setPiece(move.end(), piece);

        if (piece instanceof Pawn && move.start().col() != move.end().col() && !pieceMap.hasPiece(move.end())) {
            ChessPosition capturedPawnPos = new ChessPosition(move.end().col(), move.start().row());
            tempMap.removePiece(capturedPawnPos);
        }

        return tempMap;
    }

    // https://en.wikipedia.org/wiki/Threefold_repetition
    public static boolean isThreefoldRepetition(BoardManager boardManager) {
        Map<BoardState, Integer> history = boardManager.getBoardStateHistory();
        String currentFEN = ChessNotationUtils.getFEN(boardManager.getCurrentBoardState());
        int occurrences = history.getOrDefault(boardManager.getCurrentBoardState(), 0);
        logger.debug("Checking threefold repetition (FIDE): FEN={}, occurrences={}", currentFEN, occurrences);
        return occurrences >= 3;
    }

    public static boolean isDeadPosition(ChessPieceMap pieceMap) {
        Map<ChessPosition, ChessPiece> pieces = pieceMap.getPieceMap();
        int whiteKnights = 0, whiteBishops = 0, blackKnights = 0, blackBishops = 0;
        int whiteOther = 0, blackOther = 0;
        int totalPieces = pieces.size();

        // Đếm số quân của mỗi bên
        for (ChessPiece piece : pieces.values()) {
            if (piece.getColor().isWhite()) {
                if (piece instanceof Knight) {
                    whiteKnights++;
                } else if (piece instanceof Bishop) {
                    whiteBishops++;
                } else if (!(piece instanceof King)) {
                    whiteOther++; // Tốt, Xe, Hậu

                }
            } else { // BLACK
                if (piece instanceof Knight) {
                    blackKnights++;
                } else if (piece instanceof Bishop) {
                    blackBishops++;
                } else if (!(piece instanceof King)) {
                    blackOther++;
                }
            }
        }

        // Tổng số quân khác (ngoài vua, mã, tượng) phải bằng 0
        if (whiteOther > 0 || blackOther > 0) {
            return false;
        }

        int totalMinorPieces = whiteKnights + whiteBishops + blackKnights + blackBishops;

        // Trường hợp 1: Chỉ còn hai vua
        if (totalPieces == 2 && whiteKnights == 0 && whiteBishops == 0 && blackKnights == 0 && blackBishops == 0) {
            logger.info("Dead Position: King vs. King");
            return true;
        }

        // Trường hợp 2: Vua + Mã vs. Vua hoặc Vua vs. Vua + Mã
        if (totalPieces == 3 && totalMinorPieces == 1 && (whiteKnights + blackKnights == 1)) {
            logger.info("Dead Position: King + Knight vs. King");
            return true;
        }

        // Trường hợp 3: Vua + Tượng vs. Vua hoặc Vua vs. Vua + Tượng
        if (totalPieces == 3 && totalMinorPieces == 1 && (whiteBishops + blackBishops == 1)) {
            logger.info("Dead Position: King + Bishop vs. King");
            return true;
        }

        // Trường hợp 4: Vua + Tượng vs. Vua + Tượng (cùng màu)
        if (totalPieces == 4 && whiteBishops == 1 && blackBishops == 1 && whiteKnights == 0 && blackKnights == 0) {
            ChessPosition whiteBishopPos = null, blackBishopPos = null;
            for (Map.Entry<ChessPosition, ChessPiece> entry : pieces.entrySet()) {
                if (entry.getValue() instanceof Bishop) {
                    if (entry.getValue().getColor().isWhite()) {
                        whiteBishopPos = entry.getKey();
                    } else {
                        blackBishopPos = entry.getKey();
                    }
                }
            }
            assert whiteBishopPos != null;
            assert blackBishopPos != null;
            boolean sameColor = (whiteBishopPos.col() + whiteBishopPos.row()) % 2 == (blackBishopPos.col() + blackBishopPos.row()) % 2;
            if (sameColor) {
                logger.info("Dead Position: King + Bishop vs. King + Bishop (same color)");
                return true;
            }
        }

        return false;
    }

    public static boolean isStalemate(PieceColor currentPlayerColor, ChessPieceMap pieceMap) {
        if (isKingInCheck(currentPlayerColor, pieceMap)) {
            return false;
        }

        for (Map.Entry<ChessPosition, ChessPiece> entry : pieceMap.getPieceMap().entrySet()) {
            ChessPiece piece = entry.getValue();
            if (piece.getColor() == currentPlayerColor) {
                ChessPosition start = entry.getKey();
                List<ChessMove> moves = piece.generateValidMoves(start, pieceMap);
                for (ChessMove move : moves) {
                    ChessPieceMap tempMap = simulateMove(move, pieceMap);
                    if (!isKingInCheck(currentPlayerColor, tempMap)) {
                        return false;
                    }
                }
            }
        }

        logger.info("Stalemate detected: No legal moves for {}", currentPlayerColor);
        return true;
    }
}
