package com.minhduc5a12.chess.utils;

import com.minhduc5a12.chess.pieces.*;
import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.GameEngine;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.model.Move;

public class ChessNotationUtils {
    private final GameEngine gameEngine;

    public ChessNotationUtils(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public String getFen() {
        StringBuilder fen = new StringBuilder();
        ChessTile[][] board = gameEngine.getBoard();

        // Phần 1: Vị trí quân cờ
        for (int row = 0; row < 8; row++) {
            int emptyCount = 0;
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col].getPiece();
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(getPieceFenChar(piece));
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row < 7) {
                fen.append("/");
            }
        }

        // Phần 2: Lượt đi hiện tại
        fen.append(" ").append(gameEngine.getCurrentPlayerColor() == PieceColor.WHITE ? "w" : "b");

        // Phần 3: Quyền nhập thành
        fen.append(" ").append(getCastlingAvailability());

        // Phần 4: Ô đích en passant
        fen.append(" ").append(getEnPassantTarget());

        // Phần 5: Số nước không ăn quân hoặc di chuyển tốt
        fen.append(" ").append(gameEngine.getMovesWithoutCaptureOrPawn());

        // Phần 6: Số lượt đầy đủ
        int fullMoveCount = (gameEngine.getBoardStates().size() + 1) / 2;
        fen.append(" ").append(fullMoveCount);

        return fen.toString();
    }

    private char getPieceFenChar(ChessPiece piece) {
        char fenChar = switch (piece) {
            case Pawn pawn -> 'p';
            case Rook rook -> 'r';
            case Knight knight -> 'n';
            case Bishop bishop -> 'b';
            case Queen queen -> 'q';
            case King king -> 'k';
            case null, default -> {
                assert piece != null;
                throw new IllegalStateException("Unknown piece type: " + piece.getClass().getSimpleName());
            }
        };

        return piece.getColor() == PieceColor.WHITE ? Character.toUpperCase(fenChar) : fenChar;
    }

    private String getCastlingAvailability() {
        StringBuilder castling = new StringBuilder();

        ChessPiece whiteKing = null;
        ChessPiece whiteRookKingside = null;
        ChessPiece whiteRookQueenside = null;
        ChessPiece blackKing = null;
        ChessPiece blackRookKingside = null;
        ChessPiece blackRookQueenside = null;

        for (ChessPiece piece : gameEngine.getPiecePositions().keySet()) {
            int[] pos = gameEngine.getPiecePosition(piece);
            if (piece instanceof King) {
                if (piece.getColor() == PieceColor.WHITE) whiteKing = piece;
                else blackKing = piece;
            } else if (piece instanceof Rook) {
                if (piece.getColor() == PieceColor.WHITE) {
                    if (pos[0] == 7 && pos[1] == 7) whiteRookKingside = piece;   // h1
                    else if (pos[0] == 0 && pos[1] == 7) whiteRookQueenside = piece; // a1
                } else {
                    if (pos[0] == 7 && pos[1] == 0) blackRookKingside = piece;   // h8
                    else if (pos[0] == 0 && pos[1] == 0) blackRookQueenside = piece; // a8
                }
            }
        }

        if (whiteKing != null && !whiteKing.hasMoved()) {
            if (whiteRookKingside != null && !whiteRookKingside.hasMoved()) {
                castling.append("K");
            }
            if (whiteRookQueenside != null && !whiteRookQueenside.hasMoved()) {
                castling.append("Q");
            }
        }

        if (blackKing != null && !blackKing.hasMoved()) {
            if (blackRookKingside != null && !blackRookKingside.hasMoved()) {
                castling.append("k");
            }
            if (blackRookQueenside != null && !blackRookQueenside.hasMoved()) {
                castling.append("q");
            }
        }

        return !castling.isEmpty() ? castling.toString() : "-";
    }

    private String getEnPassantTarget() {
        Move lastMove = gameEngine.getLastMove();
        if (lastMove != null && gameEngine.getBoard()[lastMove.endY()][lastMove.endX()].getPiece() instanceof Pawn) {
            if (Math.abs(lastMove.startY() - lastMove.endY()) == 2) {
                int midY = (lastMove.startY() + lastMove.endY()) / 2;
                char file = (char) ('a' + lastMove.endX());
                char rank = (char) ('8' - midY);
                return "" + file + rank;
            }
        }
        return "-";
    }
}