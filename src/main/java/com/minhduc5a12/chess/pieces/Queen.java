package com.minhduc5a12.chess.pieces;

import java.util.ArrayList;
import java.util.List;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.model.ChessMove;
import com.minhduc5a12.chess.model.ChessPiece;
import com.minhduc5a12.chess.model.ChessPosition;
import com.minhduc5a12.chess.utils.BoardUtils;

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
}
