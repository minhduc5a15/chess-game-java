package com.minhduc5a12.chess;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.pieces.Bishop;
import com.minhduc5a12.chess.pieces.ChessPiece;
import com.minhduc5a12.chess.pieces.King;
import com.minhduc5a12.chess.pieces.Knight;
import com.minhduc5a12.chess.pieces.Pawn;
import com.minhduc5a12.chess.pieces.Queen;
import com.minhduc5a12.chess.pieces.Rook;

public class BoardManager {
    private static final Logger logger = LoggerFactory.getLogger(BoardManager.class);
    private final ChessTile[][] board = new ChessTile[8][8];
    private final Map<ChessPiece, int[]> piecePositions = new HashMap<>();
    private final GameController gameController; // Thêm để truyền vào ChessPiece

    public BoardManager(GameController gameController) {
        this.gameController = gameController;
        initializeBoard();
        initializePieces();
    }

    private void initializeBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = new ChessTile(row, col);
            }
        }
    }

    private void initializePieces() {
        Object[][] pieces = {
            {Pawn.class, PieceColor.WHITE, "images/white_pawn.png", 6},
            {Pawn.class, PieceColor.BLACK, "images/black_pawn.png", 1},
            {Rook.class, PieceColor.WHITE, "images/white_rook.png", new int[][]{{7, 0}, {7, 7}}},
            {Rook.class, PieceColor.BLACK, "images/black_rook.png", new int[][]{{0, 0}, {0, 7}}},
            {Knight.class, PieceColor.WHITE, "images/white_knight.png", new int[][]{{7, 1}, {7, 6}}},
            {Knight.class, PieceColor.BLACK, "images/black_knight.png", new int[][]{{0, 1}, {0, 6}}},
            {Bishop.class, PieceColor.WHITE, "images/white_bishop.png", new int[][]{{7, 2}, {7, 5}}},
            {Bishop.class, PieceColor.BLACK, "images/black_bishop.png", new int[][]{{0, 2}, {0, 5}}},
            {Queen.class, PieceColor.WHITE, "images/white_queen.png", new int[][]{{7, 3}}},
            {Queen.class, PieceColor.BLACK, "images/black_queen.png", new int[][]{{0, 3}}},
            {King.class, PieceColor.WHITE, "images/white_king.png", new int[][]{{7, 4}}},
            {King.class, PieceColor.BLACK, "images/black_king.png", new int[][]{{0, 4}}}
        };

        for (Object[] pieceInfo : pieces) {
            Class<?> pieceClass = (Class<?>) pieceInfo[0];
            PieceColor color = (PieceColor) pieceInfo[1];
            String imagePath = (String) pieceInfo[2];
            Object positions = pieceInfo[3];

            if (positions instanceof Integer) {
                int row = (int) positions;
                for (int col = 0; col < 8; col++) {
                    try {
                        ChessPiece piece = pieceClass == Pawn.class ? new Pawn(color, imagePath, gameController) : (ChessPiece) pieceClass.getConstructor(PieceColor.class, String.class, GameController.class).newInstance(color, imagePath, gameController);
                        board[row][col].setPiece(piece);
                        updatePiecePosition(piece, col, row);
                    } catch (Exception e) {
                        logger.error("Error initializing piece at position [{}, {}]: {}", col, row, e.getMessage(), e);
                    }
                }
            } else if (positions instanceof int[][] posArray) {
                for (int[] pos : posArray) {
                    int row = pos[0];
                    int col = pos[1];
                    try {
                        ChessPiece piece = (ChessPiece) pieceClass.getConstructor(PieceColor.class, String.class, GameController.class).newInstance(color, imagePath, gameController);
                        board[row][col].setPiece(piece);
                        updatePiecePosition(piece, col, row);
                    } catch (Exception e) {
                        logger.error("Error initializing piece at position [{}, {}]: {}", col, row, e.getMessage(), e);
                    }
                }
            }
        }
    }

    public void updatePiecePosition(ChessPiece piece, int newX, int newY) {
        piecePositions.put(piece, new int[]{newX, newY});
    }

    public void removePiece(ChessPiece piece) {
        piecePositions.remove(piece);
    }

    public ChessTile[][] getBoard() {
        return board;
    }

    public Map<ChessPiece, int[]> getPiecePositions() {
        return piecePositions;
    }

    public int[] getPiecePosition(ChessPiece piece) {
        return piecePositions.get(piece);
    }
}