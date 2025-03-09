package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.engine.StockfishPlayer;
import com.minhduc5a12.chess.model.Move;
import com.minhduc5a12.chess.pieces.ChessPiece;
import com.minhduc5a12.chess.utils.ChessNotationUtils;
import com.minhduc5a12.chess.utils.SoundPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    private final BoardManager boardManager;
    private final MoveExecutor moveExecutor;
    private final GameStateChecker stateChecker;
    private final ChessNotationUtils notationUtils;
    private final StockfishPlayer stockfishPlayer;
    private final ChessBoard chessBoard;
    private final JFrame parentFrame;
    private final List<Consumer<PieceColor>> turnChangeListeners = new ArrayList<>();
    private final List<String> boardStates = new ArrayList<>();

    private PieceColor currentPlayerColor = PieceColor.WHITE;
    private Move lastMove;
    private int movesWithoutCaptureOrPawn = 0;
    private boolean gameEnded = false;
    private Player whitePlayer;
    private Player blackPlayer;

    public GameController(JFrame parentFrame, ChessBoard chessBoard) {
        this.boardManager = new BoardManager(this);
        this.moveExecutor = new MoveExecutor(boardManager, parentFrame, this);
        this.stateChecker = new GameStateChecker(boardManager);
        this.notationUtils = new ChessNotationUtils(this);
        this.stockfishPlayer = new StockfishPlayer(this);
        this.chessBoard = chessBoard;
        this.parentFrame = parentFrame;
        updateBoardState();
    }

    public void makeMove(int startX, int startY, int endX, int endY) {
        makeMove(startX, startY, endX, endY, null);
    }

    public void makeMove(int startX, int startY, int endX, int endY, String promotion) {
        if (gameEnded || !stateChecker.isValidMove(startX, startY, endX, endY, currentPlayerColor)) {
            return;
        }

        if (!stateChecker.isMoveValidUnderCheck(startX, startY, endX, endY, currentPlayerColor)) {
            SoundPlayer.playMoveIllegal();
            return;
        }

        boolean isCaptureOrPawnMove = moveExecutor.executeMove(startX, startY, endX, endY, promotion, currentPlayerColor);
        lastMove = new Move(startX, startY, endX, endY);

        if (isCaptureOrPawnMove) {
            movesWithoutCaptureOrPawn = 0;
        } else {
            movesWithoutCaptureOrPawn++;
        }

        updateBoardState();
        checkGameState();

        if (!gameEnded) {
            switchTurn();
        }

        repaintChessBoard();

        String fen = notationUtils.getFen();
        logger.info("Current FEN after move: {}", fen);

        if (!gameEnded && currentPlayerColor == PieceColor.BLACK) {
            resetStockfishSuggestions();
            new Thread(stockfishPlayer::makeMove).start();
        }
    }

    public void switchTurn() {
        currentPlayerColor = (currentPlayerColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        if (currentPlayerColor == PieceColor.WHITE) {
            resetStockfishSuggestions();
            repaintChessBoard();
        }
        for (Consumer<PieceColor> listener : turnChangeListeners) {
            listener.accept(currentPlayerColor);
        }
    }

    private void resetStockfishSuggestions() {
        ChessTile[][] board = boardManager.getBoard();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                board[y][x].setStockfishSuggested(false);
            }
        }
    }

    public void updateBoardState() {
        StringBuilder state = new StringBuilder();
        ChessTile[][] board = boardManager.getBoard();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                ChessPiece piece = board[y][x].getPiece();
                state.append(piece == null ? "-" : piece.getColor() + piece.getClass().getSimpleName());
            }
        }
        boardStates.add(state.toString());
    }

    public void checkGameState() {
        PieceColor opponentColor = (currentPlayerColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        if (stateChecker.isCheckmate(opponentColor)) {
            gameEnded = true;
            SoundPlayer.playMoveCheckSound();
            showGameOverDialog(opponentColor + " bị chiếu hết! " + currentPlayerColor + " thắng!");
        } else if (stateChecker.isKingInCheck(opponentColor)) {
            SoundPlayer.playMoveCheckSound();
        } else if (stateChecker.isDeadPosition(opponentColor)) {
            showGameOverDialog("Hòa cờ: Không còn nước đi hợp lệ!");
        } else if (stateChecker.isThreefoldRepetition(boardStates)) {
            showGameOverDialog("Hòa cờ: Lặp lại 3 lần!");
        } else if (stateChecker.isFiftyMoveRule(movesWithoutCaptureOrPawn)) {
            showGameOverDialog("Hòa cờ: 50 nước không ăn quân hoặc di chuyển tốt!");
        }
    }

    private void showGameOverDialog(String message) {
        repaintChessBoard();
        GameOverDialog dialog = new GameOverDialog(parentFrame, message);
        dialog.setVisible(true);
    }

    public void repaintChessBoard() {
        if (chessBoard != null) {
            chessBoard.repaint();
        }
    }

    public void updatePiecePosition(ChessPiece piece, int newX, int newY) {
        boardManager.updatePiecePosition(piece, newX, newY);
    }

    public void performCastling(int row, int kingEndX) {
        moveExecutor.performCastling(row, kingEndX);
    }

    // Thêm phương thức setPlayers
    public void setPlayers(Player whitePlayer, Player blackPlayer) {
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
    }

    // Thêm phương thức startInitialTimer
    public void startInitialTimer() {
        if (whitePlayer != null) {
            whitePlayer.startTimer(); // Giả định Player có phương thức startTimer
        }
    }

    // Getters và Setters
    public ChessNotationUtils getNotationUtils() { return notationUtils; }
    public ChessTile[][] getBoard() { return boardManager.getBoard(); }
    public PieceColor getCurrentPlayerColor() { return currentPlayerColor; }
    public Move getLastMove() { return lastMove; }
    public Map<ChessPiece, int[]> getPiecePositions() { return boardManager.getPiecePositions(); }
    public boolean isGameEnded() { return gameEnded; }
    public void setLastMove(Move lastMove) { this.lastMove = lastMove; }
    public void resetMovesWithoutCaptureOrPawn() { this.movesWithoutCaptureOrPawn = 0; }
    public void incrementMovesWithoutCaptureOrPawn() { this.movesWithoutCaptureOrPawn++; }
    public ChessBoard getChessBoard() { return chessBoard; }
    public void addTurnChangeListener(Consumer<PieceColor> listener) { turnChangeListeners.add(listener); }
    public boolean isMoveValidUnderCheck(int startX, int startY, int endX, int endY) {
        return stateChecker.isMoveValidUnderCheck(startX, startY, endX, endY, currentPlayerColor);
    }
    public int getMovesWithoutCaptureOrPawn() { return movesWithoutCaptureOrPawn; }
    public List<String> getBoardStates() { return boardStates; }
}