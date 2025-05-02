package com.minhduc5a12.chess.core.pieces;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.model.BoardState;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;
import com.minhduc5a12.chess.utils.BoardUtils;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends ChessPiece {

    private final BoardState boardState;

    {
        this.pieceValue = 1;
    }

    public Pawn(PieceColor color, BoardState boardState) {
        super(color, color.isWhite() ? "white_pawn.png" : "black_pawn.png");
        this.boardState = boardState;
    }

    public Pawn(PieceColor color) {
        super(color, color.isWhite() ? "white_pawn.png" : "black_pawn.png");
        this.boardState = null;
    }

    @Override
    public List<ChessMove> generateValidMoves(ChessPosition start, ChessPieceMap pieceMap) {
        List<ChessMove> moves = new ArrayList<>();
        int direction = (getColor().isWhite()) ? 1 : -1;
        int startRow = start.row();
        int startCol = start.col();

        // Move forward
        int newRow = startRow + direction;
        if (newRow >= 0 && newRow <= 7) {
            ChessPosition forward = new ChessPosition(startCol, newRow);
            if (!pieceMap.hasPiece(forward)) {
                moves.add(new ChessMove(start, forward));
                if (!hasMoved() && ((getColor().isWhite() && startRow == 1) || (getColor().isBlack() && startRow == 6))) {
                    ChessPosition twoForward = new ChessPosition(startCol, startRow + 2 * direction);
                    if (!pieceMap.hasPiece(twoForward)) {
                        moves.add(new ChessMove(start, twoForward));
                    }
                }
            }
        }

        // Capture opponent's piece
        int[] captureCols = {startCol - 1, startCol + 1};
        for (int col : captureCols) {
            if (BoardUtils.isWithinBoard(col, newRow)) {
                ChessPosition capturePos = new ChessPosition(col, newRow);
                if (pieceMap.hasPiece(capturePos) && pieceMap.getPiece(capturePos).getColor() != getColor()) {
                    moves.add(new ChessMove(start, capturePos));
                }
            }
        }

        // En passant
        ChessMove lastMove = boardState.getLastMove();
        if (lastMove != null) {
            ChessPiece lastMovedPiece = pieceMap.getPiece(lastMove.end());
            if (lastMovedPiece instanceof Pawn && Math.abs(lastMove.start().row() - lastMove.end().row()) == 2 && lastMove.end().row() == startRow && Math.abs(lastMove.end().col() - startCol) == 1) {
                ChessPosition enPassantTarget = new ChessPosition(lastMove.end().col(), startRow + direction);
                moves.add(new ChessMove(start, enPassantTarget));
            }
        }

        return moves;
    }

    @Override
    public String getPieceNotation() {
        return this.getColor().isWhite() ? "P" : "p";
    }

    @Override
    public ChessPiece deepCopy() {
        Pawn copy = new Pawn(this.getColor(), this.boardState);
        copy.pieceValue = this.pieceValue;
        copy.setHasMoved(this.hasMoved());
        return copy;
    }
}