package com.minhduc5a12.chess.pieces;

import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.GameEngine;
import com.minhduc5a12.chess.model.Move;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.utils.BoardUtils;

import java.util.ArrayList;
import java.util.List;

public class King extends ChessPiece {

    public King(PieceColor color, String imagePath, GameEngine gameEngine) {
        super(color, imagePath, gameEngine);
    }


    @Override
    public List<Move> generateValidMoves(int startX, int startY, ChessTile[][] board) {
        List<Move> validMoves = new ArrayList<>();
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int[] dir : directions) {
            int newX = startX + dir[0];
            int newY = startY + dir[1];
            if (BoardUtils.isWithinBoard(newX, newY)) {
                ChessPiece targetPiece = board[newY][newX].getPiece();
                if (targetPiece == null || targetPiece.getColor() != getColor()) {
                    validMoves.add(new Move(startX, startY, newX, newY));
                }
            }
        }
        if (!hasMoved()) {
            if (canCastle(startX, startY, 7, board)) {
                validMoves.add(new Move(startX, startY, startX + 2, startY));
            }
            if (canCastle(startX, startY, 0, board)) {
                validMoves.add(new Move(startX, startY, startX - 2, startY));
            }
        }
        return validMoves;
    }

    private boolean canCastle(int startX, int startY, int rookX, ChessTile[][] board) {
        int direction = (rookX > startX) ? 1 : -1;
        int x = startX + direction;
        while (x != rookX) {
            if (board[startY][x].getPiece() != null) {
                return false;
            }
            x += direction;
        }
        ChessPiece rook = board[startY][rookX].getPiece();
        return rook instanceof Rook && !rook.hasMoved();
    }
}