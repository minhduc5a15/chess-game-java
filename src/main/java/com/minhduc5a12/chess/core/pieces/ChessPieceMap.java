package com.minhduc5a12.chess.core.pieces;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the mapping of chess pieces to their positions on the board.
 * Provides methods to access, modify, and analyze the state of the board.
 */
public class ChessPieceMap {

    private final Map<ChessPosition, ChessPiece> pieceMap;

    /**
     * Constructs an empty ChessPieceMap.
     */
    public ChessPieceMap() {
        this.pieceMap = new HashMap<>();
    }

    // --- Getters and Setters ---

    public ChessPiece getPiece(ChessPosition position) {
        return pieceMap.get(position);
    }

    public Map<ChessPosition, ChessPiece> getPieceMap() {
        return pieceMap;
    }

    public void setPiece(ChessPosition position, ChessPiece piece) {
        pieceMap.put(position, piece);
    }

    public void removePiece(ChessPosition position) {
        pieceMap.remove(position);
    }

    // --- Board State Queries ---

    /**
     * Retrieves a piece at the position specified by chess notation (e.g., "e4").
     *
     * @param chessNotation the position in chess notation
     * @return the piece at the specified position, or null if none exists
     */
    public ChessPiece getPiece(String chessNotation) {
        return getPiece(ChessPosition.toChessPosition(chessNotation));
    }

    /**
     * Checks if a position contains a piece.
     *
     * @param position the position to check
     * @return true if the position contains a non-null piece, false otherwise
     */
    public boolean hasPiece(ChessPosition position) {
        return pieceMap.containsKey(position) && pieceMap.get(position) != null;
    }

    /**
     * Retrieves the position of the king for the specified color.
     *
     * @param color the color of the king (white or black)
     * @return the position of the king, or null if not found
     */
    public ChessPosition getKingPosition(PieceColor color) {
        for (Map.Entry<ChessPosition, ChessPiece> entry : pieceMap.entrySet()) {
            ChessPiece piece = entry.getValue();
            if (piece instanceof King && piece.getColor() == color) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Retrieves the king piece for the specified color.
     *
     * @param color the color of the king (white or black)
     * @return the king piece, or null if not found
     */
    public King getKing(PieceColor color) {
        for (Map.Entry<ChessPosition, ChessPiece> entry : pieceMap.entrySet()) {
            ChessPiece piece = entry.getValue();
            if (piece instanceof King && piece.getColor() == color) {
                return (King) piece;
            }
        }
        return null;
    }

    /**
     * Calculates the material advantage of white over black based on piece values.
     *
     * @return the material advantage (positive if white is ahead, negative if black is ahead)
     */
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

    // --- Board Manipulation ---

    /**
     * Clears all pieces from the board.
     */
    public void clear() {
        pieceMap.clear();
    }

    /**
     * Creates a deep copy of the current piece map.
     *
     * @return a new ChessPieceMap with copied positions and pieces
     */
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