package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.model.Move;
import com.minhduc5a12.chess.pieces.*;
import com.minhduc5a12.chess.utils.BoardUtils;
import com.minhduc5a12.chess.utils.SoundPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameEngine {
    private static final Logger logger = LoggerFactory.getLogger(GameEngine.class);
    private final ChessTile[][] board;
    private PieceColor currentPlayerColor;
    private Consumer<PieceColor> onTurnChange;
    private Move lastMove;
    private final Map<ChessPiece, int[]> piecePositions;
    private final Map<PieceColor, Player> players;
    private final List<Move> moveHistory;
    private final List<String> boardStates;
    private int movesWithoutCaptureOrPawn;

    public GameEngine() {
        long start = System.currentTimeMillis();
        this.board = new ChessTile[8][8];
        this.currentPlayerColor = PieceColor.WHITE;
        this.piecePositions = new HashMap<>();
        this.players = new HashMap<>();
        this.moveHistory = new ArrayList<>();
        this.boardStates = new ArrayList<>();
        this.movesWithoutCaptureOrPawn = 0;
        initializeBoard();
        initializePieces();
        initializePlayers();
        logger.info("GameEngine initialization took {} ms", System.currentTimeMillis() - start);
    }

    private void initializePlayers() {
        players.put(PieceColor.BLACK, new Player("Naruto", PieceColor.BLACK, this, "images/avatar1.png"));
        players.put(PieceColor.WHITE, new Player("Sasuke", PieceColor.WHITE, this, "images/avatar2.png"));
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
            {King.class, PieceColor.BLACK, "images/black_king.png", new int[][]{{0, 4}}},
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
                        ChessPiece piece = pieceClass == Pawn.class ? new Pawn(color, imagePath, this) :
                            (ChessPiece) pieceClass.getConstructor(PieceColor.class, String.class, GameEngine.class)
                                .newInstance(color, imagePath, this);
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
                        ChessPiece piece = (ChessPiece) pieceClass.getConstructor(PieceColor.class, String.class, GameEngine.class)
                            .newInstance(color, imagePath, this);
                        board[row][col].setPiece(piece);
                        updatePiecePosition(piece, col, row);
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
        long start = System.currentTimeMillis();
        if (!BoardUtils.isWithinBoard(startX, startY) || !BoardUtils.isWithinBoard(endX, endY)) {
            logger.error("Invalid move coordinates: ({}, {}) -> ({}, {})", startX, startY, endX, endY);
            SoundPlayer.playMoveIllegal();
            return;
        }
        ChessPiece piece = board[startY][startX].getPiece();
        if (piece == null) {
            logger.warn("No piece at starting position ({}, {})", startX, startY);
            return;
        }

        ChessPiece targetPiece = board[endY][endX].getPiece();

        logger.info("Attempting move: {} from ({}, {}) to ({}, {})", piece, startX, startY, endX, endY);
        if (!isMoveValidUnderCheck(startX, startY, endX, endY)) {
            SoundPlayer.playMoveIllegal();
            logger.info("Move invalid under check");
            return;
        }

        board[endY][endX].setPiece(piece);
        board[startY][startX].setPiece(null);
        updatePiecePosition(piece, endX, endY);

        PieceColor opponentColor = (currentPlayerColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        if (isCheckmate(opponentColor)) {
            SoundPlayer.playMoveCheckSound();
            String winner = currentPlayerColor == PieceColor.WHITE ? "Trắng" : "Đen";
            logger.info("Checkmate detected! {} wins", winner);
            JOptionPane.showMessageDialog(null, "Chiếu bí! Bên " + winner + " thắng!", "Kết thúc ván cờ", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (BoardUtils.isDeadPosition(board, opponentColor, piecePositions) ||
            BoardUtils.isThreefoldRepetition(boardStates) ||
            BoardUtils.isFiftyMoveRule(movesWithoutCaptureOrPawn)) {
            logger.info("Stalemate detected! Game ends in a draw");
            JOptionPane.showMessageDialog(null, "Hòa cờ! Không bên nào thắng.", "Kết thúc ván cờ", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (piece instanceof Pawn) {
            if ((piece.getColor() == PieceColor.WHITE && endY == 0) || (piece.getColor() == PieceColor.BLACK && endY == 7)) {
                ChessPiece promotedPiece = promotePawn(endX, endY, piece.getColor());
                removePiece(piece);
                updatePiecePosition(promotedPiece, endX, endY);
            }
        }

        if (piece instanceof Pawn && targetPiece == null && startX != endX) {
            Move lastMoveCheck = getLastMove();
            if (lastMoveCheck != null && board[lastMoveCheck.endY()][lastMoveCheck.endX()].getPiece() instanceof Pawn &&
                Math.abs(lastMoveCheck.startY() - lastMoveCheck.endY()) == 2 && lastMoveCheck.endX() == endX && lastMoveCheck.endY() == startY) {
                board[lastMoveCheck.endY()][lastMoveCheck.endX()].setPiece(null);
                SoundPlayer.playCaptureSound();
            } else {
                SoundPlayer.playMoveSound();
            }
        } else if (targetPiece != null) {
            SoundPlayer.playCaptureSound();
        } else {
            SoundPlayer.playMoveSound();
        }

        if (piece instanceof King && Math.abs(endX - startX) == 2) {
            performCastling(startY, endX);
            SoundPlayer.playCastleSound();
        }

        lastMove = new Move(startX, startY, endX, endY);
        moveHistory.add(lastMove);
        boardStates.add(getBoardState());
        if (targetPiece != null || piece instanceof Pawn) {
            movesWithoutCaptureOrPawn = 0;
        } else {
            movesWithoutCaptureOrPawn++;
        }

        currentPlayerColor = (currentPlayerColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        if (onTurnChange != null) onTurnChange.accept(currentPlayerColor);

        if (isKingInCheck(currentPlayerColor)) {
            logger.info("King of {} in check", currentPlayerColor);
            SoundPlayer.playMoveCheckSound();
        }

        logger.info("makeMove took {} ms", System.currentTimeMillis() - start);
    }

    private void updatePlayerPanels() {
        for (Player player : players.values()) {
            JPanel panel = player.createPanel();
            logger.debug("Updated player panel for player {}", player.getColor());
        }
    }

    private ChessPiece promotePawn(int x, int y, PieceColor color) {
        String[] options = {"Hậu", "Xe", "Mã", "Tượng"};
        int choice = JOptionPane.showOptionDialog(null, "Chọn quân để phong cấp:", "Phong cấp", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        String imagePathPrefix = color == PieceColor.WHITE ? "images/white_" : "images/black_";
        return switch (choice) {
            case 1 -> new Rook(color, imagePathPrefix + "rook.png", this);
            case 2 -> new Knight(color, imagePathPrefix + "knight.png", this);
            case 3 -> new Bishop(color, imagePathPrefix + "bishop.png", this);
            default -> new Queen(color, imagePathPrefix + "queen.png", this);
        };
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
        updatePiecePosition(rook, rookEndX, row);
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
        board[newY][newX].setPiece(piece);
    }

    public void removePiece(ChessPiece piece) {
        int[] position = piecePositions.get(piece);
        piecePositions.remove(piece);
        board[position[1]][position[0]].setPiece(null);
    }

    public Map<ChessPiece, int[]> getPiecePositions() {
        return piecePositions;
    }

    public boolean isMoveValidUnderCheck(int startX, int startY, int endX, int endY) {
        return BoardUtils.isMoveValidUnderCheck(startX, startY, endX, endY, board, currentPlayerColor, piecePositions);
    }

    public Move getLastMove() {
        return lastMove;
    }

    private String getBoardState() {
        StringBuilder state = new StringBuilder();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                ChessPiece piece = board[y][x].getPiece();
                state.append(piece == null ? "." : piece.getClass().getSimpleName().charAt(0) + piece.getColor().toString().charAt(0));
            }
        }
        state.append(currentPlayerColor);
        return state.toString();
    }

    public Player getPlayer(PieceColor color) {
        return players.get(color);
    }
}