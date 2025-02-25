package com.minhduc5a12.chess.pieces;

import com.minhduc5a12.chess.ChessPiece;
import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.model.Move;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.utils.BoardUtils;

import java.util.ArrayList;
import java.util.List;

public class Rook extends ChessPiece {
    public Rook(PieceColor color, String imagePath) {
        super(color, imagePath);
        this.setPieceValue(5);
    }

    @Override
    public List<Move> generateValidMoves(int startX, int startY, ChessTile[][] board) {
        List<Move> validMoves = new ArrayList<>();

        // Di chuyển theo hàng ngang và dọc
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : directions) {
            int x = startX + dir[0];
            int y = startY + dir[1];
            while (BoardUtils.isWithinBoard(x, y)) {
                ChessPiece targetPiece = board[y][x].getPiece();
                if (targetPiece == null) {
                    validMoves.add(new Move(startX, startY, x, y));
                } else {
                    if (targetPiece.getColor() != getColor()) {
                        validMoves.add(new Move(startX, startY, x, y));
                    }
                    break; // Dừng lại nếu gặp quân cờ chặn đường
                }
                x += dir[0];
                y += dir[1];
            }
        }

        return validMoves;
    }
}