package com.minhduc5a12.chess.pieces;

import com.minhduc5a12.chess.ChessPiece;
import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.model.Move;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.utils.BoardUtils;
import java.util.ArrayList;
import java.util.List;

public class Knight extends ChessPiece {
    public Knight(PieceColor color, String imagePath) {
        super(color, imagePath);
        this.setPieceValue(3);
    }

    @Override
    public List<Move> generateValidMoves(int startX, int startY, ChessTile[][] board) {
        List<Move> validMoves = new ArrayList<>();

        // Các hướng di chuyển của quân Mã
        int[][] moves = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        for (int[] move : moves) {
            int newX = startX + move[0];
            int newY = startY + move[1];

            if (BoardUtils.isWithinBoard(newX, newY)) {
                ChessPiece targetPiece = board[newY][newX].getPiece();
                if (targetPiece == null || targetPiece.getColor() != getColor()) {
                    validMoves.add(new Move(startX, startY, newX, newY));
                }
            }
        }

        return validMoves;
    }
}