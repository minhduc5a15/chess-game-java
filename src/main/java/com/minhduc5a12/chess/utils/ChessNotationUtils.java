package com.minhduc5a12.chess.utils;

import com.minhduc5a12.chess.constants.GameConstants;
import com.minhduc5a12.chess.core.model.BoardState;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;
import com.minhduc5a12.chess.core.pieces.ChessPieceMap;

/**
 * Utility class for converting chess positions and board states to standard notations.
 */
public class ChessNotationUtils {

    /**
     * Converts the current board state to a FEN (Forsyth-Edwards Notation)
     * string. The FEN string represents the position of pieces on the board,
     * the active player, castling availability, en passant target square,
     * halfmove clock, and fullmove number.
     *
     * @param boardState The current state of the chessboard.
     * @return A FEN string representing the current board state.
     */
    public static String getFEN(BoardState boardState) {
        ChessPieceMap pieceMap = boardState.getChessPieceMap();
        StringBuilder fen = new StringBuilder();
        // Note: FEN uses a descartes coordinate system (col, row). e.g., a1 = (0, 0)

        // 1. Chessboard position
        for (int row = GameConstants.Board.BOARD_SIZE - 1; row >= 0; row--) {
            int emptyCount = 0;
            for (int col = 0; col <= GameConstants.Board.BOARD_SIZE - 1; col++) {
                ChessPosition position = new ChessPosition(col, row);
                ChessPiece piece = pieceMap.getPiece(position);

                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(piece.getPieceNotation());
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row > 0) {
                fen.append("/");
            }
        }

        // 2. Current player
        fen.append(" ");
        fen.append(boardState.getCurrentPlayerColor().isWhite() ? "w" : "b");

        // 3. Castling availability
        fen.append(" ");
        StringBuilder castling = new StringBuilder();
        if (boardState.canWhiteCastleKingside()) {
            castling.append("K");
        }
        if (boardState.canWhiteCastleQueenside()) {
            castling.append("Q");
        }
        if (boardState.canBlackCastleKingside()) {
            castling.append("k");
        }
        if (boardState.canBlackCastleQueenside()) {
            castling.append("q");
        }
        fen.append(!castling.isEmpty() ? castling.toString() : "-");

        // 4. En passant target square
        fen.append(" ");
        ChessPosition enPassantTargetSquare = boardState.getEnPassantTargetSquare();
        if (enPassantTargetSquare != null) {
            fen.append(enPassantTargetSquare.toChessNotation());
        } else {
            fen.append("-");
        }
        // 5. Halfmove clock
        fen.append(" ");
        fen.append(boardState.getHalfmoveClock());

        // 6. Fullmove number
        fen.append(" ");
        fen.append(boardState.getFullmoveNumber());

        return fen.toString();
    }

    /**
     * Converts the current board state to a FEN string containing only the first four parts
     * (piece placement, active color, castling availability, en passant target square).
     *
     * @param boardState The current state of the chessboard.
     * @return A FEN string representing the first four parts of the board state.
     */
    public static String getFenFourParts(BoardState boardState) {
        ChessPieceMap pieceMap = boardState.getChessPieceMap();
        StringBuilder fen = new StringBuilder();
        // Note: FEN uses a descartes coordinate system (col, row). e.g., a1 = (0, 0)

        // 1. Chessboard position
        for (int row = GameConstants.Board.BOARD_SIZE - 1; row >= 0; row--) {
            int emptyCount = 0;
            for (int col = 0; col <= GameConstants.Board.BOARD_SIZE - 1; col++) {
                ChessPosition position = new ChessPosition(col, row);
                ChessPiece piece = pieceMap.getPiece(position);

                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(piece.getPieceNotation());
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row > 0) {
                fen.append("/");
            }
        }

        // 2. Current player
        fen.append(" ");
        fen.append(boardState.getCurrentPlayerColor().isWhite() ? "w" : "b");

        // 3. Castling availability
        fen.append(" ");
        StringBuilder castling = new StringBuilder();
        if (boardState.canWhiteCastleKingside()) {
            castling.append("K");
        }
        if (boardState.canWhiteCastleQueenside()) {
            castling.append("Q");
        }
        if (boardState.canBlackCastleKingside()) {
            castling.append("k");
        }
        if (boardState.canBlackCastleQueenside()) {
            castling.append("q");
        }
        fen.append(!castling.isEmpty() ? castling.toString() : "-");

        // 4. En passant target square
        fen.append(" ");
        ChessPosition enPassantTargetSquare = boardState.getEnPassantTargetSquare();
        if (enPassantTargetSquare != null) {
            fen.append(enPassantTargetSquare.toChessNotation());
        } else {
            fen.append("-");
        }

        return fen.toString();
    }
}