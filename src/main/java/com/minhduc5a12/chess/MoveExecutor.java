package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.model.Move;
import com.minhduc5a12.chess.pieces.*;
import com.minhduc5a12.chess.utils.SoundPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class MoveExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MoveExecutor.class);
    private final BoardManager boardManager;
    private final JFrame parentFrame;
    private final GameController gameController;

    public MoveExecutor(BoardManager boardManager, JFrame parentFrame, GameController gameController) {
        this.boardManager = boardManager;
        this.parentFrame = parentFrame;
        this.gameController = gameController;
    }

    public boolean executeMove(int startX, int startY, int endX, int endY, String promotion, PieceColor currentPlayerColor) {
        ChessTile[][] board = boardManager.getBoard();
        ChessPiece piece = board[startY][startX].getPiece();
        if (piece == null || piece.getColor() != currentPlayerColor) {
            return false;
        }

        boolean isEnPassant = false;
        boolean isCapture = board[endY][endX].getPiece() != null;

        if (piece instanceof Pawn && board[endY][endX].getPiece() == null && startX != endX) {
            Move lastMove = gameController.getLastMove();
            if (lastMove != null && board[lastMove.endY()][lastMove.endX()].getPiece() instanceof Pawn && Math.abs(lastMove.startY() - lastMove.endY()) == 2 && lastMove.endX() == endX && lastMove.endY() == startY) {
                board[lastMove.endY()][lastMove.endX()].setPiece(null);
                isEnPassant = true;
                isCapture = true;
            }
        }

        ChessPiece targetPiece = board[endY][endX].getPiece();
        board[endY][endX].setPiece(piece);
        board[startY][startX].setPiece(null);
        piece.setHasMoved(true);

        if (targetPiece != null) {
            boardManager.removePiece(targetPiece);
        }
        boardManager.updatePiecePosition(piece, endX, endY);

        if (piece instanceof Pawn && ((piece.getColor() == PieceColor.WHITE && endY == 0) || (piece.getColor() == PieceColor.BLACK && endY == 7))) {
            ChessPiece promotedPiece = promotePawn(endX, endY, piece.getColor(), promotion);
            board[endY][endX].setPiece(promotedPiece);
            boardManager.removePiece(piece);
            boardManager.updatePiecePosition(promotedPiece, endX, endY);
        }

        if (isEnPassant || targetPiece != null) {
            SoundPlayer.playCaptureSound();
        } else {
            SoundPlayer.playMoveSound();
        }

        if (piece instanceof King && Math.abs(endX - startX) == 2) {
            performCastling(startY, endX);
            SoundPlayer.playCastleSound();
        }

        return isCapture || piece instanceof Pawn;
    }

    private ChessPiece promotePawn(int x, int y, PieceColor color, String promotion) {
        if (promotion != null) {
            return switch (promotion.toLowerCase()) {
                case "r" -> new Rook(color, color == PieceColor.WHITE ? "images/white_rook.png" : "images/black_rook.png", gameController);
                case "b" -> new Bishop(color, color == PieceColor.WHITE ? "images/white_bishop.png" : "images/black_bishop.png", gameController);
                case "n" -> new Knight(color, color == PieceColor.WHITE ? "images/white_knight.png" : "images/black_knight.png", gameController);
                default -> new Queen(color, color == PieceColor.WHITE ? "images/white_queen.png" : "images/black_queen.png", gameController);
            };
        } else {
            Object[] options = {"Queen", "Rook", "Bishop", "Knight"};
            int choice = JOptionPane.showOptionDialog(parentFrame, "Promote pawn to:", "Pawn Promotion", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            return switch (choice) {
                case 1 -> new Rook(color, color == PieceColor.WHITE ? "images/white_rook.png" : "images/black_rook.png", gameController);
                case 2 -> new Bishop(color, color == PieceColor.WHITE ? "images/white_bishop.png" : "images/black_bishop.png", gameController);
                case 3 -> new Knight(color, color == PieceColor.WHITE ? "images/white_knight.png" : "images/black_knight.png", gameController);
                default -> new Queen(color, color == PieceColor.WHITE ? "images/white_queen.png" : "images/black_queen.png", gameController);
            };
        }
    }

    public void performCastling(int row, int kingEndX) {
        ChessTile[][] board = boardManager.getBoard();
        int direction = (kingEndX > 4) ? 1 : -1;
        int rookStartX = (direction == 1) ? 7 : 0;
        int rookEndX = (direction == 1) ? kingEndX - 1 : kingEndX + 1;

        ChessPiece rook = board[row][rookStartX].getPiece();
        board[row][rookEndX].setPiece(rook);
        board[row][rookStartX].setPiece(null);
        rook.setHasMoved(true);
        boardManager.updatePiecePosition(rook, rookEndX, row);
    }
}