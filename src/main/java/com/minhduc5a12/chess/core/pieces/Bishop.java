package com.minhduc5a12.chess.core.pieces;

import java.util.ArrayList;
import java.util.List;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;
import com.minhduc5a12.chess.utils.BoardUtils;

public class Bishop extends ChessPiece {

    {
        this.pieceValue = 3;
    }

    public Bishop(PieceColor color) {
        super(color, color.isWhite() ? "white_bishop.png" : "black_bishop.png");
    }

    @Override
    public List<ChessMove> generateValidMoves(ChessPosition start, ChessPieceMap pieceMap) {
        List<ChessMove> moves = new ArrayList<>();
        int startRow = start.row();
        int startCol = start.col();

        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
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
        return this.getColor().isWhite() ? "B" : "b";
    }

    @Override
    public ChessPiece deepCopy() {
        Bishop copy = new Bishop(this.getColor());
        copy.pieceValue = this.pieceValue;
        copy.setHasMoved(this.hasMoved());
        return copy;
    }
}