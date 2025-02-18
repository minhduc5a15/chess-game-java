package com.minhduc5a12.chess.pieces;

import com.minhduc5a12.chess.ChessPiece;
import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.utils.BoardUtils;

public class Bishop extends ChessPiece {

    public Bishop(PieceColor color, String imagePath) {
        super(color, imagePath);
        setPieceValue(3);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, ChessTile[][] board) {
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);
        if (dx != dy) {
            return false;
        }

        int directionX = (endX > startX) ? 1 : -1;
        int directionY = (endY > startY) ? 1 : -1;

        int x = startX + directionX;
        int y = startY + directionY;
        while (x != endX && y != endY) {
            if (!BoardUtils.isWithinBoard(x, y) || board[y][x].getPiece() != null) {
                return false;
            }
            x += directionX;
            y += directionY;
        }

        ChessPiece targetPiece = board[endY][endX].getPiece();
        return targetPiece == null || targetPiece.getColor() != getColor();
    }
}