package com.minhduc5a12.chess.core.pieces;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;

import java.util.HashMap;
import java.util.Map;

public class ChessPieceMap {

    private final Map<ChessPosition, ChessPiece> pieceMap;

    public ChessPieceMap() {
        this.pieceMap = new HashMap<>();
    }

    public ChessPiece getPiece(ChessPosition position) {
        return pieceMap.get(position);
    }

    public ChessPiece getPiece(String chessNotation) {
        return getPiece(ChessPosition.toChessPosition(chessNotation));
    }

    public void setPiece(ChessPosition position, ChessPiece piece) {
        pieceMap.put(position, piece);
    }

    public void removePiece(ChessPosition position) {
        pieceMap.remove(position);
    }

    public boolean hasPiece(ChessPosition position) {
        return pieceMap.containsKey(position) && pieceMap.get(position) != null;
    }

    public Map<ChessPosition, ChessPiece> getPieceMap() {
        return pieceMap;
    }

    public void clear() {
        pieceMap.clear();
    }

    public ChessPosition getKingPosition(PieceColor color) {
        for (Map.Entry<ChessPosition, ChessPiece> entry : pieceMap.entrySet()) {
            ChessPiece piece = entry.getValue();
            if (piece instanceof King && piece.getColor() == color) {
                return entry.getKey();
            }
        }
        return null;
    }

    public King getKing(PieceColor color) {
        for (Map.Entry<ChessPosition, ChessPiece> entry : pieceMap.entrySet()) {
            ChessPiece piece = entry.getValue();
            if (piece instanceof King && piece.getColor() == color) {
                return (King) piece;
            }
        }
        return null;
    }

    public int getMaterialAdvantage() {
        int whiteMaterial = 0;
        int blackMaterial = 0;
        for (ChessPiece piece : pieceMap.values()) {
            if (piece == null) {
                continue;
            }
            if (piece.getColor().isWhite()) {
                whiteMaterial += piece.getPieceValue();
            } else {
                blackMaterial += piece.getPieceValue();
            }
        }
        return whiteMaterial - blackMaterial;
    }

    public ChessPieceMap deepCopy() {
        ChessPieceMap copy = new ChessPieceMap();
        for (Map.Entry<ChessPosition, ChessPiece> entry : pieceMap.entrySet()) {
            ChessPosition positionCopy = entry.getKey().deepCopy();
            ChessPiece pieceCopy = entry.getValue() != null ? entry.getValue().deepCopy() : null;
            copy.setPiece(positionCopy, pieceCopy);
        }
        return copy;
    }
}