package com.minhduc5a12.chess.utils;

import com.minhduc5a12.chess.constants.GameConstants;
import com.minhduc5a12.chess.model.BoardState;
import com.minhduc5a12.chess.model.ChessMove;
import com.minhduc5a12.chess.model.ChessPiece;
import com.minhduc5a12.chess.model.ChessPosition;
import com.minhduc5a12.chess.pieces.ChessPieceMap;
import com.minhduc5a12.chess.pieces.Pawn;

public class ChessNotationUtils {

    public String getFEN(BoardState boardState) {
        ChessPieceMap pieceMap = boardState.getChessPieceMap();
        StringBuilder fen = new StringBuilder();

        // 1. Vị trí quân cờ
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

        // 2. Lượt đi
        fen.append(" ");
        fen.append(boardState.getCurrentPlayerColor().isWhite() ? "w" : "b");

        // 3. Quyền nhập thành
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

        // 4. Mục tiêu en passant
        fen.append(" ");
        ChessPosition enPassantTargetSquare = boardState.getEnPassantTargetSquare();
        if (enPassantTargetSquare != null) {
            fen.append(enPassantTargetSquare.toChessNotation());
        } else {
            fen.append("-");
        }
        // 5. Đồng hồ nửa nước
        fen.append(" ");
        fen.append(boardState.getHalfmoveClock());

        // 6. Số nước đi đầy đủ
        fen.append(" ");
        fen.append(boardState.getFullmoveNumber());

        return fen.toString();
    }
}
