package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.engine.Stockfish;
import com.minhduc5a12.chess.model.Move;
import com.minhduc5a12.chess.pieces.*;
import com.minhduc5a12.chess.utils.BoardUtils;
import com.minhduc5a12.chess.utils.ChessNotationUtils;
import com.minhduc5a12.chess.utils.SoundPlayer;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameEngine {
    private static final Logger logger = LoggerFactory.getLogger(GameEngine.class);

    private final ChessTile[][] board;
    private final List<Consumer<PieceColor>> turnChangeListeners;
    private final Map<ChessPiece, int[]> piecePositions;
    private final List<String> boardStates;
    private final JFrame parentFrame;
    private final ChessNotationUtils notationUtils;
    private final Stockfish stockfish;

    private PieceColor currentPlayerColor;
    private Move lastMove;
    private int movesWithoutCaptureOrPawn;
    private boolean gameEnded;
    private ChessBoard chessBoard;

    private Player whitePlayer;
    private Player blackPlayer;

    public GameEngine(JFrame parentFrame, ChessBoard chessBoard) {
        this.board = new ChessTile[8][8];
        this.currentPlayerColor = PieceColor.WHITE;
        this.turnChangeListeners = new ArrayList<>();
        this.piecePositions = new HashMap<>();
        this.boardStates = new ArrayList<>();
        this.parentFrame = parentFrame;
        this.chessBoard = chessBoard;
        this.movesWithoutCaptureOrPawn = 0;
        this.gameEnded = false;
        this.notationUtils = new ChessNotationUtils(this);
        this.stockfish = new Stockfish();

        initializeBoard();
        initializePieces();
        updateBoardState();
    }

    public ChessTile[][] getBoard() {
        return board;
    }

    public PieceColor getCurrentPlayerColor() {
        return currentPlayerColor;
    }

    public Move getLastMove() {
        return lastMove;
    }

    public Map<ChessPiece, int[]> getPiecePositions() {
        return piecePositions;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public int getMovesWithoutCaptureOrPawn() {
        return movesWithoutCaptureOrPawn;
    }

    public List<String> getBoardStates() {
        return boardStates;
    }

    public void setChessBoard(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }

    public void setPlayers(Player whitePlayer, Player blackPlayer) {
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
    }

    public void addTurnChangeListener(Consumer<PieceColor> listener) {
        turnChangeListeners.add(listener);
    }

    private void initializeBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = new ChessTile(row, col);
            }
        }
    }

    public void startInitialTimer() {
        if (whitePlayer != null) {
            whitePlayer.startTimer();
        }
        if (blackPlayer != null) {
            blackPlayer.pauseTimer();
        }
        for (Consumer<PieceColor> listener : turnChangeListeners) {
            listener.accept(currentPlayerColor);
        }
    }

    private void initializePieces() {
        Object[][] pieces = {{Pawn.class, PieceColor.WHITE, "images/white_pawn.png", 6}, {Pawn.class, PieceColor.BLACK, "images/black_pawn.png", 1}, {Rook.class, PieceColor.WHITE, "images/white_rook.png", new int[][]{{7, 0}, {7, 7}}}, {Rook.class, PieceColor.BLACK, "images/black_rook.png", new int[][]{{0, 0}, {0, 7}}}, {Knight.class, PieceColor.WHITE, "images/white_knight.png", new int[][]{{7, 1}, {7, 6}}}, {Knight.class, PieceColor.BLACK, "images/black_knight.png", new int[][]{{0, 1}, {0, 6}}}, {Bishop.class, PieceColor.WHITE, "images/white_bishop.png", new int[][]{{7, 2}, {7, 5}}}, {Bishop.class, PieceColor.BLACK, "images/black_bishop.png", new int[][]{{0, 2}, {0, 5}}}, {Queen.class, PieceColor.WHITE, "images/white_queen.png", new int[][]{{7, 3}}}, {Queen.class, PieceColor.BLACK, "images/black_queen.png", new int[][]{{0, 3}}}, {King.class, PieceColor.WHITE, "images/white_king.png", new int[][]{{7, 4}}}, {King.class, PieceColor.BLACK, "images/black_king.png", new int[][]{{0, 4}}}};

        for (Object[] pieceInfo : pieces) {
            Class<?> pieceClass = (Class<?>) pieceInfo[0];
            PieceColor color = (PieceColor) pieceInfo[1];
            String imagePath = (String) pieceInfo[2];
            Object positions = pieceInfo[3];

            if (positions instanceof Integer) {
                int row = (int) positions;
                for (int col = 0; col < 8; col++) {
                    try {
                        ChessPiece piece = pieceClass == Pawn.class ? new Pawn(color, imagePath, this) : (ChessPiece) pieceClass.getConstructor(PieceColor.class, String.class, GameEngine.class).newInstance(color, imagePath, this);
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
                        ChessPiece piece = (ChessPiece) pieceClass.getConstructor(PieceColor.class, String.class, GameEngine.class).newInstance(color, imagePath, this);
                        board[row][col].setPiece(piece);
                        updatePiecePosition(piece, col, row);
                    } catch (Exception e) {
                        logger.error("Error initializing piece at position [{}, {}]: {}", col, row, e.getMessage(), e);
                    }
                }
            }
        }
    }

    public void makeMove(int startX, int startY, int endX, int endY) {
        if (gameEnded) {
            return;
        }

        ChessPiece piece = board[startY][startX].getPiece();
        if (piece == null || piece.getColor() != currentPlayerColor) {
            return;
        }

        if (!isMoveValidUnderCheck(startX, startY, endX, endY)) {
            SoundPlayer.playMoveIllegal();
            return;
        }

        boolean isEnPassant = false;
        boolean isCapture = board[endY][endX].getPiece() != null;

        if (piece instanceof Pawn && board[endY][endX].getPiece() == null && startX != endX) {
            Move lastMove = getLastMove();
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

        if (targetPiece != null) piecePositions.remove(targetPiece);
        updatePiecePosition(piece, endX, endY);

        if (piece instanceof Pawn) {
            if ((piece.getColor() == PieceColor.WHITE && endY == 0) || (piece.getColor() == PieceColor.BLACK && endY == 7)) {
                ChessPiece promotedPiece = promotePawn(endX, endY, piece.getColor());
                board[endY][endX].setPiece(promotedPiece);
                piecePositions.remove(piece);
                updatePiecePosition(promotedPiece, endX, endY);
            }
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

        lastMove = new Move(startX, startY, endX, endY);

        if (isCapture || piece instanceof Pawn) {
            movesWithoutCaptureOrPawn = 0;
        } else {
            movesWithoutCaptureOrPawn++;
        }

        updateBoardState();
        checkGameState();

        if (!gameEnded) {
            switchTurn();
        }

        // Cập nhật giao diện ngay lập tức
        if (chessBoard != null) chessBoard.repaint();

        // Lấy FEN sau khi đổi lượt
        String fen = notationUtils.getFen();
        logger.info("Current FEN after move: {}", fen);

        // Chạy Stockfish trong luồng riêng nếu là nước đi của Trắng
        if (!gameEnded && piece.getColor() == PieceColor.WHITE) {
            resetStockfishSuggestions();
            new Thread(() -> {
                stockfish.sendCommand("position fen " + fen);
                stockfish.sendCommand("go depth 25");
                List<String> output = stockfish.getOutput();
                String bestMove = null;
                for (String line : output) {
                    if (line.startsWith("bestmove")) {
                        bestMove = line.split(" ")[1];
                        logger.info("Stockfish best move for Black: {}", bestMove);
                        break;
                    }
                }
                if (bestMove != null && bestMove.length() >= 4) {
                    int startXStockfish = bestMove.charAt(0) - 'a'; // Ô gốc X
                    int startYStockfish = 7 - (bestMove.charAt(1) - '1'); // Ô gốc Y
                    int endXStockfish = bestMove.charAt(2) - 'a'; // Ô đích X
                    int endYStockfish = 7 - (bestMove.charAt(3) - '1'); // Ô đích Y
                    SwingUtilities.invokeLater(() -> {
                        board[startYStockfish][startXStockfish].setStockfishSuggested(true); // Tô ô gốc
                        board[endYStockfish][endXStockfish].setStockfishSuggested(true); // Tô ô đích
                        if (chessBoard != null) chessBoard.repaint();
                    });
                }
            }).start();
        }
    }

    private void resetStockfishSuggestions() {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                board[y][x].setStockfishSuggested(false);
            }
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

    public void switchTurn() {
        currentPlayerColor = (currentPlayerColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        if (currentPlayerColor == PieceColor.WHITE) {
            resetStockfishSuggestions();
            if (chessBoard != null) chessBoard.repaint();
        }
        for (Consumer<PieceColor> listener : turnChangeListeners) {
            listener.accept(currentPlayerColor);
        }
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

    private void updateBoardState() {
        StringBuilder state = new StringBuilder();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                ChessPiece piece = board[y][x].getPiece();
                state.append(piece == null ? "-" : piece.getColor() + piece.getClass().getSimpleName());
            }
        }
        boardStates.add(state.toString());
    }

    private void checkGameState() {
        PieceColor opponentColor = (currentPlayerColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        if (BoardUtils.isCheckmate(board, opponentColor, piecePositions)) {
            gameEnded = true;
            SoundPlayer.playMoveCheckSound();
            showGameOverDialog(opponentColor + " bị chiếu hết! " + currentPlayerColor + " thắng!");
        } else if (BoardUtils.isKingInCheck(board, opponentColor, piecePositions)) {
            SoundPlayer.playMoveCheckSound();
        } else if (BoardUtils.isDeadPosition(board, opponentColor, piecePositions)) {
            showGameOverDialog("Hòa cờ: Không còn nước đi hợp lệ!");
        } else if (BoardUtils.isThreefoldRepetition(boardStates)) {
            showGameOverDialog("Hòa cờ: Lặp lại 3 lần!");
        } else if (BoardUtils.isFiftyMoveRule(movesWithoutCaptureOrPawn)) {
            showGameOverDialog("Hòa cờ: 50 nước không ăn quân hoặc di chuyển tốt!");
        }
    }

    private void showGameOverDialog(String message) {
        if (chessBoard != null) chessBoard.repaint();
        GameOverDialog dialog = new GameOverDialog(parentFrame, message);
        dialog.setVisible(true);
    }

    private void resetGame() {
        gameEnded = false;
        boardStates.clear();
        movesWithoutCaptureOrPawn = 0;
        lastMove = null;
        piecePositions.clear();
        initializeBoard();
        initializePieces();
        updateBoardState();

        currentPlayerColor = PieceColor.WHITE;

        if (chessBoard != null) {
            chessBoard.initializeTiles();
            chessBoard.repaint();
        }

        if (whitePlayer != null) {
            whitePlayer.resetTimer();
            whitePlayer.startTimer();
        }
        if (blackPlayer != null) {
            blackPlayer.resetTimer();
            blackPlayer.pauseTimer();
        }

        for (Consumer<PieceColor> listener : turnChangeListeners) {
            listener.accept(currentPlayerColor);
        }
    }
}