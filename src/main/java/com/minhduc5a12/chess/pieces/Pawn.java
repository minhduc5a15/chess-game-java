package com.minhduc5a12.chess.pieces;

import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.GameController;
import com.minhduc5a12.chess.model.Move;
import com.minhduc5a12.chess.constants.PieceColor;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends ChessPiece {
    private final GameController gameController; // Để truy cập lastMove

    public Pawn(PieceColor color, String imagePath, GameController gameController) {
        super(color, imagePath, gameController);
        this.gameController = gameController;
        this.setPieceValue(1);
    }

    @Override
    public List<Move> generateValidMoves(int startX, int startY, ChessTile[][] board) {
        List<Move> validMoves = new ArrayList<>();
        int direction = (getColor() == PieceColor.WHITE) ? -1 : 1;

        int newY = startY + direction;
        if (newY >= 0 && newY < 8 && board[newY][startX].getPiece() == null) {
            validMoves.add(new Move(startX, startY, startX, newY));
        }

        if ((getColor() == PieceColor.WHITE && startY == 6) || (getColor() == PieceColor.BLACK && startY == 1)) {
            int newY2 = startY + 2 * direction;
            if (newY2 >= 0 && newY2 < 8 && board[newY2][startX].getPiece() == null && board[newY][startX].getPiece() == null) {
                validMoves.add(new Move(startX, startY, startX, newY2));
            }
        }

        int[] captureX = {startX - 1, startX + 1};
        for (int x : captureX) {
            if (x >= 0 && x < 8) {
                ChessPiece targetPiece = board[newY][x].getPiece();
                if (targetPiece != null && targetPiece.getColor() != getColor()) {
                    validMoves.add(new Move(startX, startY, x, newY));
                }
            }
        }

        Move lastMove = gameController.getLastMove();
        if (lastMove != null && board[lastMove.endY()][lastMove.endX()].getPiece() instanceof Pawn) {
            int lastStartY = lastMove.startY();
            int lastEndY = lastMove.endY();
            int lastEndX = lastMove.endX();
            if (Math.abs(lastStartY - lastEndY) == 2) {
                if (lastEndY == startY && (lastEndX == startX - 1 || lastEndX == startX + 1)) {
                    int enPassantY = startY + direction;
                    validMoves.add(new Move(startX, startY, lastEndX, enPassantY));
                }
            }
        }

        return validMoves;
    }
}