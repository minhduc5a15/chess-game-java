package com.minhduc5a12.chess.utils;

import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.GameController;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.model.Move;
import com.minhduc5a12.chess.pieces.*;

public class ChessNotationUtils {
    private final GameController gameController;

    public ChessNotationUtils(GameController gameController) {
        this.gameController = gameController;
    }

    public String getFen() {
        StringBuilder fen = new StringBuilder();
        ChessTile[][] board = gameController.getBoard();

        // 1. Vị trí quân cờ
        for (int y = 0; y < 8; y++) {
            int emptyCount = 0;
            for (int x = 0; x < 8; x++) {
                ChessPiece piece = board[y][x].getPiece();
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(getPieceSymbol(piece));
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (y < 7) {
                fen.append("/");
            }
        }

        // 2. Lượt chơi hiện tại
        fen.append(" ");
        fen.append(gameController.getCurrentPlayerColor() == PieceColor.WHITE ? "w" : "b");

        // 3. Quyền nhập thành
        fen.append(" ");
        fen.append(getCastlingAvailability());

        // 4. En passant target square
        fen.append(" ");
        fen.append(getEnPassantTarget());

        // 5. Halfmove clock (số nước kể từ lần cuối ăn quân hoặc di chuyển tốt)
        fen.append(" ");
        fen.append(gameController.getMovesWithoutCaptureOrPawn());

        // 6. Fullmove number (số lượt đầy đủ, tăng sau mỗi lượt của đen)
        fen.append(" ");
        fen.append((gameController.getBoardStates().size() + 1) / 2);

        return fen.toString();
    }

    private String getPieceSymbol(ChessPiece piece) {
        char symbol;
        if (piece instanceof Pawn) symbol = 'p';
        else if (piece instanceof Rook) symbol = 'r';
        else if (piece instanceof Knight) symbol = 'n';
        else if (piece instanceof Bishop) symbol = 'b';
        else if (piece instanceof Queen) symbol = 'q';
        else if (piece instanceof King) symbol = 'k';
        else throw new IllegalArgumentException("Unknown piece type");

        return piece.getColor() == PieceColor.WHITE ? String.valueOf(Character.toUpperCase(symbol)) : String.valueOf(symbol);
    }

    private String getCastlingAvailability() {
        ChessTile[][] board = gameController.getBoard();
        StringBuilder castling = new StringBuilder();

        // Kiểm tra quyền nhập thành trắng
        ChessPiece whiteKing = board[7][4].getPiece();
        if (whiteKing instanceof King && !whiteKing.hasMoved()) {
            ChessPiece rookH = board[7][7].getPiece();
            if (rookH instanceof Rook && !rookH.hasMoved()) {
                castling.append("K");
            }
            ChessPiece rookA = board[7][0].getPiece();
            if (rookA instanceof Rook && !rookA.hasMoved()) {
                castling.append("Q");
            }
        }

        // Kiểm tra quyền nhập thành đen
        ChessPiece blackKing = board[0][4].getPiece();
        if (blackKing instanceof King && !blackKing.hasMoved()) {
            ChessPiece rookH = board[0][7].getPiece();
            if (rookH instanceof Rook && !rookH.hasMoved()) {
                castling.append("k");
            }
            ChessPiece rookA = board[0][0].getPiece();
            if (rookA instanceof Rook && !rookA.hasMoved()) {
                castling.append("q");
            }
        }

        return castling.length() > 0 ? castling.toString() : "-";
    }

    private String getEnPassantTarget() {
        Move lastMove = gameController.getLastMove();
        if (lastMove != null) {
            ChessPiece piece = gameController.getBoard()[lastMove.endY()][lastMove.endX()].getPiece();
            if (piece instanceof Pawn && Math.abs(lastMove.startY() - lastMove.endY()) == 2) {
                int enPassantY = (lastMove.startY() + lastMove.endY()) / 2;
                char file = (char) ('a' + lastMove.endX());
                return "" + file + (8 - enPassantY);
            }
        }
        return "-";
    }

    // Getter cho movesWithoutCaptureOrPawn (nếu cần)
    public int getMovesWithoutCaptureOrPawn() {
        return gameController.getMovesWithoutCaptureOrPawn();
    }
}