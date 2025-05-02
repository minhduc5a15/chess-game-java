package com.minhduc5a12.chess.core.pieces;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;
import com.minhduc5a12.chess.utils.BoardUtils;

import java.util.ArrayList;
import java.util.List;

public class Queen extends ChessPiece {

    {
        this.pieceValue = 9;
    }

    public Queen(PieceColor color) {
        super(color, color.isWhite() ? "white_queen.png" : "black_queen.png");
    }

    @Override
    public List<ChessMove> generateValidMoves(ChessPosition start, ChessPieceMap pieceMap) {
        List<ChessMove> moves = new ArrayList<>();
        int startRow = start.row();
        int startCol = start.col();

        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int[] dir : directions) {
            int dCol = dir[0];
            int dRow = dir[1];
            int newCol = startCol + dCol;
            int newRow = startRow + dRow;

            while (BoardUtils.isWithinBoard(newCol, newRow)) {
                ChessPosition pos = new ChessPosition(newCol, newRow);
                if (!pieceMap.hasPiece(pos)) {
                    moves.add(new ChessMove(start, pos));
                } else {
                    if (pieceMap.getPiece(pos).getColor() != getColor()) {
                        moves.add(new ChessMove(start, pos));
                    }
                    break;
                }
                newCol += dCol;
                newRow += dRow;
            }
        }
        return moves;
    }

    @Override
    public String getPieceNotation() {
        return this.getColor().isWhite() ? "Q" : "q";
    }

    @Override
    public ChessPiece deepCopy() {
        Queen copy = new Queen(this.getColor());
        copy.pieceValue = this.pieceValue;
        copy.setHasMoved(this.hasMoved());
        return copy;
    }
}