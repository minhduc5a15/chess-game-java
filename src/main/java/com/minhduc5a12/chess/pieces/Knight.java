package com.minhduc5a12.chess.pieces;

import com.minhduc5a12.chess.ChessPiece;
import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.constants.PieceColor;

public class Knight extends ChessPiece {
    public Knight(PieceColor color, String imagePath) {
        super(color, imagePath);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, ChessTile[][] board) {
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);

        if ((dx == 2 && dy == 1) || (dx == 1 && dy == 2)) {
            ChessPiece targetPiece = board[endY][endX].getPiece();
            return targetPiece == null || targetPiece.getColor() != getColor();
        }

        return false;
    }
}