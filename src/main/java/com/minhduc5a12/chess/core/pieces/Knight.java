package com.minhduc5a12.chess.core.pieces;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;
import com.minhduc5a12.chess.utils.BoardUtils;

import java.util.ArrayList;
import java.util.List;

public class Knight extends ChessPiece {
    {
        this.pieceValue = 3;
    }

    public Knight(PieceColor color) {
        super(color, color.isWhite() ? "white_knight.png" : "black_knight.png");
    }

    @Override
    public List<ChessMove> generateValidMoves(ChessPosition start, ChessPieceMap pieceMap) {
        List<ChessMove> moves = new ArrayList<>();
        int startRow = start.row();
        int startCol = start.col();

        int[][] offsets = {{1, 2}, {1, -2}, {-1, 2}, {-1, -2}, {2, 1}, {2, -1}, {-2, 1}, {-2, -1}};

        for (int[] offset : offsets) {
            int newCol = startCol + offset[0];
            int newRow = startRow + offset[1];
            if (BoardUtils.isWithinBoard(newCol, newRow)) {
                ChessPosition pos = new ChessPosition(newCol, newRow);
                if (!pieceMap.hasPiece(pos) || (pieceMap.hasPiece(pos) && pieceMap.getPiece(pos).getColor() != getColor())) {
                    moves.add(new ChessMove(start, pos));
                }
            }
        }

        return moves;
    }

    @Override
    public String getPieceNotation() {
        return this.getColor().isWhite() ? "N" : "n";
    }

    @Override
    public ChessPiece deepCopy() {
        Knight copy = new Knight(this.getColor());
        copy.pieceValue = this.pieceValue;
        copy.setHasMoved(this.hasMoved());
        return copy;
    }
}