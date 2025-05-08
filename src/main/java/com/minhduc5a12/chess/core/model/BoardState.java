package com.minhduc5a12.chess.core.model;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.pieces.ChessPieceMap;
import com.minhduc5a12.chess.core.pieces.King;
import com.minhduc5a12.chess.core.pieces.Pawn;
import com.minhduc5a12.chess.core.pieces.Rook;
import com.minhduc5a12.chess.utils.ChessNotationUtils;

import java.util.Objects;

/**
 * A class representing the state of a chessboard, including piece positions,
 * game state, and castling availability. Uses Cartesian coordinates (col, row)
 * for handling positions on the board, where col (0-7) corresponds to files
 * (a-h) and row (0-7) corresponds to ranks (1-8). Validates moves to ensure
 * they are within the board boundaries, throwing an
 * {@link IllegalStateException} if invalid positions (e.g., null king
 * positions) are detected.
 */
public final class BoardState {

    private final ChessPieceMap chessPieceMap;
    private ChessMove lastMove;
    private PieceColor currentPlayerColor = PieceColor.WHITE;
    private int halfmoveClock = 0;
    private int fullmoveNumber = 1;
    private ChessPosition enPassantTargetSquare = null;
    private boolean whiteCanCastleKingside = true;
    private boolean whiteCanCastleQueenside = true;
    private boolean blackCanCastleKingside = true;
    private boolean blackCanCastleQueenside = true;
    // ...
    /**
     * Constructs a new board state with the given piece map and initializes
     * castling availability. Validates the presence and position of both kings,
     * throwing an exception if either is missing.
     *
     * @param chessPieceMap the map containing all pieces and their positions on
     *                      the board
     * @throws IllegalStateException if the white or black king's position is
     *                               null
     */
    public BoardState(ChessPieceMap chessPieceMap) {
        this.chessPieceMap = chessPieceMap;
        if (!chessPieceMap.getPieceMap().isEmpty()) {
            // Check if White King can castle
            ChessPosition whiteKingPosition = chessPieceMap.getKingPosition(PieceColor.WHITE);
            if (whiteKingPosition == null) {
                throw new IllegalStateException("White King position is null");
            }
            King whiteKing = chessPieceMap.getKing(PieceColor.WHITE);
            this.whiteCanCastleKingside = whiteKing.canCastleKingside(whiteKingPosition, chessPieceMap);
            this.whiteCanCastleQueenside = whiteKing.canCastleQueenside(whiteKingPosition, chessPieceMap);

            // Check if Black King can castle
            ChessPosition blackKingPosition = chessPieceMap.getKingPosition(PieceColor.BLACK);
            if (blackKingPosition == null) {
                throw new IllegalStateException("Black King position is null");
            }
            King blackKing = chessPieceMap.getKing(PieceColor.BLACK);
            this.blackCanCastleKingside = blackKing.canCastleKingside(blackKingPosition, chessPieceMap);
            this.blackCanCastleQueenside = blackKing.canCastleQueenside(blackKingPosition, chessPieceMap);
        }
    }

    public ChessPieceMap getChessPieceMap() {
        return chessPieceMap;
    }

    public void setLastMove(ChessMove lastMove) {
        this.lastMove = lastMove;
        updateEnPassantTargetSquare();

        if (lastMove != null) {
            ChessPiece movedPiece = chessPieceMap.getPiece(lastMove.start());
            ChessPosition start = lastMove.start();
            if (movedPiece instanceof King) {
                if (movedPiece.getColor() == PieceColor.WHITE) {
                    whiteCanCastleKingside = false;
                    whiteCanCastleQueenside = false;
                } else {
                    blackCanCastleKingside = false;
                    blackCanCastleQueenside = false;
                }
            } else if (movedPiece instanceof Rook) {
                if (movedPiece.getColor() == PieceColor.WHITE) {
                    if (start.equals(ChessPosition.get("H1"))) {
                        whiteCanCastleKingside = false;
                    } else if (start.equals(ChessPosition.get("A1"))) {
                        whiteCanCastleQueenside = false;
                    }
                } else {
                    if (start.equals(ChessPosition.get("H8"))) {
                        blackCanCastleKingside = false;
                    } else if (start.equals(ChessPosition.get("A8"))) {
                        blackCanCastleQueenside = false;
                    }
                }
            }
        }
    }

    public ChessMove getLastMove() {
        return lastMove;
    }

    public PieceColor getCurrentPlayerColor() {
        return currentPlayerColor;
    }

    public void setCurrentPlayerColor(PieceColor currentPlayerColor) {
        this.currentPlayerColor = currentPlayerColor;
    }

    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    /**
     * Resets the halfmove clock to zero, typically after a capture or pawn
     * move.
     */
    public void clearHalfmoveClock() {
        this.halfmoveClock = 0;
    }

    /**
     * Increments the halfmove clock by one, used for moves that do not involve
     * captures or pawn advances.
     */
    public void incrementHalfmoveClock() {
        this.halfmoveClock++;
    }

    public int getFullmoveNumber() {
        return fullmoveNumber;
    }

    /**
     * Increments the fullmove number by one, typically after Black's move.
     */
    public void incrementFullmoveNumber() {
        this.fullmoveNumber++;
    }

    public ChessPosition getEnPassantTargetSquare() {
        return enPassantTargetSquare;
    }

    /**
     * Updates the en passant target square based on the last move. Sets the
     * target square if the last move was a two-square pawn advance; otherwise,
     * clears it.
     */
    public void updateEnPassantTargetSquare() {
        if (lastMove != null && chessPieceMap.getPiece(lastMove.end()) instanceof Pawn && Math.abs(lastMove.start().row() - lastMove.end().row()) == 2) {
            int enPassantRow = (lastMove.start().row() + lastMove.end().row()) / 2;
            enPassantTargetSquare = new ChessPosition(lastMove.end().col(), enPassantRow);
        } else {
            enPassantTargetSquare = null;
        }
    }

    public boolean canWhiteCastleKingside() {
        return whiteCanCastleKingside;
    }

    public boolean canBlackCastleKingside() {
        return blackCanCastleKingside;
    }

    public boolean canWhiteCastleQueenside() {
        return whiteCanCastleQueenside;
    }

    public boolean canBlackCastleQueenside() {
        return blackCanCastleQueenside;
    }

    /**
     * Generates a hash code for the board state based on its FEN
     * representation.
     *
     * @return the hash code of the board's FEN string
     */
    @Override
    public int hashCode() {
        return ChessNotationUtils.getFenFourParts(this).hashCode();
    }

    /**
     * Compares this board state with another object for equality based on their
     * FEN representations.
     *
     * @param obj the object to compare with
     * @return true if the FEN representations are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BoardState other = (BoardState) obj;
        return Objects.equals(ChessNotationUtils.getFenFourParts(this), ChessNotationUtils.getFenFourParts(other));
    }

    public BoardState deepCopy() {
        ChessPieceMap chessPieceMapCopy = this.chessPieceMap.deepCopy();
        BoardState copy = new BoardState(chessPieceMapCopy);
        copy.lastMove = this.lastMove != null ? this.lastMove.deepCopy() : null;
        copy.currentPlayerColor = this.currentPlayerColor;
        copy.halfmoveClock = this.halfmoveClock;
        copy.fullmoveNumber = this.fullmoveNumber;
        copy.enPassantTargetSquare = this.enPassantTargetSquare != null ? this.enPassantTargetSquare.deepCopy() : null;
        copy.whiteCanCastleKingside = this.whiteCanCastleKingside;
        copy.whiteCanCastleQueenside = this.whiteCanCastleQueenside;
        copy.blackCanCastleKingside = this.blackCanCastleKingside;
        copy.blackCanCastleQueenside = this.blackCanCastleQueenside;
        return copy;
    }

    public void updateFrom(BoardState other) {
        this.lastMove = other.lastMove != null ? other.lastMove.deepCopy() : null;
        this.currentPlayerColor = other.currentPlayerColor;
        this.halfmoveClock = other.halfmoveClock;
        this.fullmoveNumber = other.fullmoveNumber;
        this.enPassantTargetSquare = other.enPassantTargetSquare != null ? other.enPassantTargetSquare.deepCopy() : null;
        this.whiteCanCastleKingside = other.whiteCanCastleKingside;
        this.whiteCanCastleQueenside = other.whiteCanCastleQueenside;
        this.blackCanCastleKingside = other.blackCanCastleKingside;
        this.blackCanCastleQueenside = other.blackCanCastleQueenside;
    }
}
