package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.model.Move;
import com.minhduc5a12.chess.pieces.*;
import com.minhduc5a12.chess.utils.BoardUtils;
import com.minhduc5a12.chess.utils.SoundPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class GameEngine {
    private final ChessTile[][] board;
    private PieceColor currentPlayerColor;
    private Consumer<PieceColor> onTurnChange;
    private Move lastMove;
    private final Map<ChessPiece, int[]> piecePositions;
    private static final Logger logger = LoggerFactory.getLogger(GameEngine.class);

    public GameEngine() {
        this.board = new ChessTile[8][8];
        this.currentPlayerColor = PieceColor.WHITE;
        this.piecePositions = new HashMap<>();
        initializeBoard();
        initializePieces();
    }

    public void setOnTurnChange(Consumer<PieceColor> onTurnChange) {
        this.onTurnChange = onTurnChange;
    }

    private void initializeBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = new ChessTile(row, col);
            }
        }
    }

    private void initializePieces() {
        Object[][] pieces = {{Pawn.class, PieceColor.WHITE, "images/white_pawn.png", 6}, {Pawn.class, PieceColor.BLACK, "images/black_pawn.png", 1}, {Rook.class, PieceColor.WHITE, "images/white_rook.png", new int[][]{{7, 0}, {7, 7}}}, {Rook.class, PieceColor.BLACK, "images/black_rook.png", new int[][]{{0, 0}, {0, 7}}}, {Knight.class, PieceColor.WHITE, "images/white_knight.png", new int[][]{{7, 1}, {7, 6}}}, {Knight.class, PieceColor.BLACK, "images/black_knight.png", new int[][]{{0, 1}, {0, 6}}}, {Bishop.class, PieceColor.WHITE, "images/white_bishop.png", new int[][]{{7, 2}, {7, 5}}}, {Bishop.class, PieceColor.BLACK, "images/black_bishop.png", new int[][]{{0, 2}, {0, 5}}}, {Queen.class, PieceColor.WHITE, "images/white_queen.png", new int[][]{{7, 3}}}, {Queen.class, PieceColor.BLACK, "images/black_queen.png", new int[][]{{0, 3}}}, {King.class, PieceColor.WHITE, "images/white_king.png", new int[][]{{7, 4}}}, {King.class, PieceColor.BLACK, "images/black_king.png", new int[][]{{0, 4}}},};

        for (Object[] pieceInfo : pieces) {
            Class<?> pieceClass = (Class<?>) pieceInfo[0];
            PieceColor color = (PieceColor) pieceInfo[1];
            String imagePath = (String) pieceInfo[2];
            Object positions = pieceInfo[3];

            if (positions instanceof Integer) {
                int row = (int) positions;
                for (int col = 0; col < 8; col++) {
                    try {
                        ChessPiece piece = pieceClass == Pawn.class ? new Pawn(color, imagePath, this) : (ChessPiece) pieceClass.getConstructor(PieceColor.class, String.class).newInstance(color, imagePath);
                        board[row][col].setPiece(piece);
                        piecePositions.put(piece, new int[]{col, row});
                    } catch (Exception e) {
                        logger.error("Error initializing piece at position [{}, {}]: {}", col, row, e.getMessage(), e);
                    }
                }
            } else if (positions instanceof int[][]) {
                int[][] posArray = (int[][]) positions;
                for (int[] pos : posArray) {
                    int row = pos[0];
                    int col = pos[1];
                    try {
                        ChessPiece piece = (ChessPiece) pieceClass.getConstructor(PieceColor.class, String.class).newInstance(color, imagePath);
                        board[row][col].setPiece(piece);
                        piecePositions.put(piece, new int[]{col, row});
                    } catch (Exception e) {
                        logger.error("Error initializing piece at position [{}, {}]: {}", col, row, e.getMessage(), e);
                    }
                }
            }
        }
    }

    public ChessTile[][] getBoard() {
        return board;
    }

    public PieceColor getCurrentPlayerColor() {
        return currentPlayerColor;
    }


    public void makeMove(int startX, int startY, int endX, int endY) {
        ChessPiece piece = board[startY][startX].getPiece();
        if (piece == null) {
            return;
        }

        ChessPiece targetPiece = board[endY][endX].getPiece();

        if (!isMoveValidUnderCheck(startX, startY, endX, endY)) {
            SoundPlayer.playMoveIllegal();
            return;
        }

        // Xử lý "en passant"
        boolean isEnPassant = false;
        if (piece instanceof Pawn && targetPiece == null && startX != endX) {
            Move lastMove = getLastMove();
            if (lastMove != null && board[lastMove.getEndY()][lastMove.getEndX()].getPiece() instanceof Pawn && Math.abs(lastMove.getStartY() - lastMove.getEndY()) == 2 && lastMove.getEndX() == endX && lastMove.getEndY() == startY) {
                board[lastMove.getEndY()][lastMove.getEndX()].setPiece(null);
                isEnPassant = true;
            }
        }

        // Di chuyển quân cờ
        board[endY][endX].setPiece(piece);
        board[startY][startX].setPiece(null);
        piece.setHasMoved(true);

        // Kiểm tra và xử lý phong cấp cho Tốt
        if (piece instanceof Pawn) {
            if ((piece.getColor() == PieceColor.WHITE && endY == 0) || (piece.getColor() == PieceColor.BLACK && endY == 7)) {
                ChessPiece promotedPiece = promotePawn(endX, endY, piece.getColor());
                board[endY][endX].setPiece(promotedPiece);
                piecePositions.remove(piece); // Xóa Tốt cũ khỏi piecePositions
                piecePositions.put(promotedPiece, new int[]{endX, endY}); // Thêm quân mới
            }
        }

        // Phát âm thanh
        if (isEnPassant || targetPiece != null) {
            SoundPlayer.playCaptureSound();
        } else {
            SoundPlayer.playMoveSound();
        }

        // Xử lý nhập thành
        if (piece instanceof King && Math.abs(endX - startX) == 2) {
            performCastling(startY, endX);
            SoundPlayer.playCastleSound();
        }

        // Lưu nước đi cuối cùng
        lastMove = new Move(startX, startY, endX, endY);

        // Đổi lượt chơi
        currentPlayerColor = (currentPlayerColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        if (onTurnChange != null) {
            onTurnChange.accept(currentPlayerColor);
        }

        if (isKingInCheck(currentPlayerColor)) {
            SoundPlayer.playMoveCheckSound();
        }
    }

    private ChessPiece promotePawn(int x, int y, PieceColor color) {
        String[] options = {"Hậu", "Xe", "Mã", "Tượng"};
        int choice = JOptionPane.showOptionDialog(null, "Chọn quân để phong cấp:", "Phong cấp", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        String imagePathPrefix = color == PieceColor.WHITE ? "images/white_" : "images/black_";
        switch (choice) {
            case 0: // Hậu
                return new Queen(color, imagePathPrefix + "queen.png");
            case 1: // Xe
                return new Rook(color, imagePathPrefix + "rook.png");
            case 2: // Mã
                return new Knight(color, imagePathPrefix + "knight.png");
            case 3: // Tượng
                return new Bishop(color, imagePathPrefix + "bishop.png");
            default: // Mặc định là Hậu nếu người chơi không chọn
                return new Queen(color, imagePathPrefix + "queen.png");
        }
    }

    private void performCastling(int row, int kingEndX) {
        int direction = (kingEndX > 4) ? 1 : -1;
        int rookStartX = (direction == 1) ? 7 : 0;
        int rookEndX = (direction == 1) ? kingEndX - 1 : kingEndX + 1;

        ChessTile rookStartTile = board[row][rookStartX];
        ChessPiece rook = rookStartTile.getPiece();
        board[row][rookEndX].setPiece(rook);
        rookStartTile.setPiece(null);
        rook.setHasMoved(true);
        piecePositions.put(rook, new int[]{rookEndX, row}); // Cập nhật vị trí Xe
    }

    public boolean isValidMove(int startX, int startY, int endX, int endY) {
        ChessPiece piece = board[startY][startX].getPiece();
        if (piece == null || piece.getColor() != currentPlayerColor) {
            return false;
        }
        return piece.isValidMove(startX, startY, endX, endY, board);
    }

    public int[] getPiecePosition(ChessPiece piece) {
        return piecePositions.get(piece);
    }

    public boolean isKingInCheck(PieceColor color) {
        return BoardUtils.isKingInCheck(board, color, piecePositions);
    }

    public boolean isCheckmate(PieceColor color) {
        return BoardUtils.isCheckmate(board, color, piecePositions);
    }

    public void updatePiecePosition(ChessPiece piece, int newX, int newY) {
        piecePositions.put(piece, new int[]{newX, newY});
    }

    public boolean isMoveValidUnderCheck(int startX, int startY, int endX, int endY) {
        return BoardUtils.isMoveValidUnderCheck(startX, startY, endX, endY, board, currentPlayerColor, piecePositions);
    }

    public Move getLastMove() {
        return lastMove;
    }
}