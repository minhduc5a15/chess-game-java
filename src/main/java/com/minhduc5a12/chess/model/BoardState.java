package com.minhduc5a12.chess.model;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.pieces.ChessPieceMap;
import com.minhduc5a12.chess.pieces.King;
import com.minhduc5a12.chess.pieces.Pawn;
import com.minhduc5a12.chess.utils.ChessNotationUtils;

import java.util.Objects;

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

    public void clearHalfmoveClock() {
        this.halfmoveClock = 0;
    }

    public void incrementHalfmoveClock() {
        this.halfmoveClock++;
    }

    public int getFullmoveNumber() {
        return fullmoveNumber;
    }

    public void incrementFullmoveNumber() {
        this.fullmoveNumber++;
    }

    public ChessPosition getEnPassantTargetSquare() {
        return enPassantTargetSquare;
    }

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

    @Override
    public int hashCode() {
        return new ChessNotationUtils().getFEN(this).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BoardState other = (BoardState) obj;
        ChessNotationUtils utils = new ChessNotationUtils();
        return Objects.equals(utils.getFEN(this), utils.getFEN(other));
    }
}
