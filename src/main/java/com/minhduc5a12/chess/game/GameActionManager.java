package com.minhduc5a12.chess.game;

import com.minhduc5a12.chess.constants.GameMode;
import com.minhduc5a12.chess.core.model.BoardState;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;
import com.minhduc5a12.chess.engine.Stockfish;
import com.minhduc5a12.chess.history.GameHistoryManager;
import com.minhduc5a12.chess.ui.board.ChessTile;
import com.minhduc5a12.chess.ui.components.dialogs.GameOverDialog;
import com.minhduc5a12.chess.utils.ChessNotationUtils;
import com.minhduc5a12.chess.utils.SoundPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Map;

/**
 * Manages game actions such as hints, undo/redo, and resignation.
 * Interacts with the Stockfish engine for hints and the game history for state management.
 */
public class GameActionManager {

    private static final Logger logger = LoggerFactory.getLogger(GameActionManager.class);
    private final ChessController controller;
    private JFrame frame;
    private final Stockfish stockfish;
    private final GameHistoryManager historyManager;

    /**
     * Constructs a new GameActionManager for the specified controller.
     *
     * @param controller The ChessController to manage actions for.
     */
    public GameActionManager(ChessController controller) {
        this.controller = controller;
        this.historyManager = controller.getHistoryManager();
        this.stockfish = new Stockfish();
        this.stockfish.start();
        logger.debug("GameActionManager initialized");
    }

    /**
     * Shows a hint for the best move in PLAYER_VS_AI mode using Stockfish.
     */
    public void showHint() {
        if (controller.isGameEnded() || controller.getGameMode() != GameMode.PLAYER_VS_AI) {
            logger.debug("Cannot show hint: game ended or invalid mode");
            return;
        }

        String fen = ChessNotationUtils.getFEN(controller.getBoardManager().getCurrentBoardState());
        String bestMove = stockfish.getBestMove(fen);
        if (bestMove != null && bestMove.length() >= 4) {
            try {
                ChessMove hintMove = parseStockfishMove(bestMove);
                highlightHintMove(hintMove);
                logger.info("Hint provided: {}", bestMove);
            } catch (IllegalArgumentException e) {
                logger.error("Failed to parse Stockfish move: {}", bestMove, e);
            }
        } else {
            logger.error("Invalid or no move received from Stockfish: {}", bestMove);
        }
    }

    /**
     * Undoes the last move, restoring the previous board state.
     */
    public void undoMove() {
        if (controller.isGameEnded() || historyManager.getUndoStack().isEmpty()) {
            logger.debug("Cannot undo: game ended or no moves to undo");
            SoundPlayer.playMoveIllegal();
            return;
        }

        BoardState previousState = historyManager.getUndoStack().pop();
        restoreBoardState(previousState, true);
        controller.notifyHistoryChangeListeners();
        logger.info("Undo move performed, restored to previous state");
    }

    /**
     * Redoes a previously undone move, restoring the next board state.
     */
    public void redoMove() {
        if (controller.isGameEnded() || historyManager.getRedoStack().isEmpty()) {
            logger.debug("Cannot redo: game ended or redo stack empty");
            SoundPlayer.playMoveIllegal();
            return;
        }

        try {
            BoardState nextState = historyManager.getRedoStack().pop();
            restoreBoardState(nextState, false);
            controller.notifyHistoryChangeListeners();
            logger.info("Redo performed, restored state");
        } catch (Exception e) {
            logger.error("Redo failed: {}", e.getMessage(), e);
            SoundPlayer.playMoveIllegal();
        }
    }

    /**
     * Resigns the game, declaring the opponent as the winner.
     */
    public void resignGame() {
        if (controller.isGameEnded()) {
            logger.debug("Cannot resign: game already ended");
            return;
        }

        controller.setGameEnded(true);
        String currentPlayerColor = controller.getBoardManager().getCurrentBoardState().getCurrentPlayerColor().toString();
        String winner = controller.getBoardManager().getCurrentBoardState().getCurrentPlayerColor().isWhite() ? "Black" : "White";
        String message = "Game resigned by " + currentPlayerColor + ". " + winner + " wins!";

        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                GameOverDialog dialog = new GameOverDialog(frame, message);
                dialog.setVisible(true);
            } else {
                logger.error("Cannot show GameOverDialog: parent frame is null");
            }
        });

        logger.info("Game ended due to resignation by {}", currentPlayerColor);
        notifyGameStateListeners();
    }

    public void switchTurn() {
        BoardState currentBoardState = controller.getBoardManager().getCurrentBoardState();
        currentBoardState.setCurrentPlayerColor(currentBoardState.getCurrentPlayerColor().getOpponent());
        if (controller.getBoardManager().getCurrentPlayerColor().isWhite()) currentBoardState.incrementFullmoveNumber();
        controller.notifyGameStateChanged();
        controller.notifyTurnChanged();
        controller.notifyScoreUpdated();
    }
    /**
     * Shuts down the manager, stopping the Stockfish engine.
     */
    public void shutdown() {
        stockfish.stopEngine();
        logger.debug("GameActionManager shutdown");
    }

    /**
     * Parses a Stockfish move notation into a ChessMove.
     *
     * @param moveNotation The move notation (e.g., "e2e4").
     * @return The parsed ChessMove.
     * @throws IllegalArgumentException If the notation is invalid.
     */
    private ChessMove parseStockfishMove(String moveNotation) {
        if (moveNotation == null || moveNotation.length() < 4) {
            throw new IllegalArgumentException("Invalid move notation: " + moveNotation);
        }
        String startNotation = moveNotation.substring(0, 2);
        String endNotation = moveNotation.substring(2, 4);
        ChessPosition start = ChessPosition.toChessPosition(startNotation);
        ChessPosition end = ChessPosition.toChessPosition(endNotation);
        return new ChessMove(start, end);
    }

    /**
     * Highlights the hint move on the board.
     *
     * @param move The move to highlight.
     */
    private void highlightHintMove(ChessMove move) {
        ChessTile startTile = controller.getBoardUI().getTile(move.start());
        ChessTile endTile = controller.getBoardUI().getTile(move.end());
        startTile.setHintHighlightedSquare(true);
        endTile.setHintHighlightedSquare(true);
        controller.getBoardUI().repaintTiles(startTile, endTile);
    }

    /**
     * Restores the board to a specified state, updating UI and listeners.
     *
     * @param state  The BoardState to restore.
     * @param isUndo True if this is an undo operation, false for redo.
     */
    private void restoreBoardState(BoardState state, boolean isUndo) {
        historyManager.decrementBoardStateCount(controller.getBoardManager().getCurrentBoardState());
        if (isUndo) {
            historyManager.saveStateForRedo(controller.getBoardManager().getCurrentBoardState());
            historyManager.decrementBoardStateCount(state);
        } else {
            historyManager.saveStateForUndo(controller.getBoardManager().getCurrentBoardState());
            historyManager.incrementBoardStateCount(state);
        }

        controller.getBoardUI().clearLastMoveHighlights();
        controller.getBoardUI().setCurrentLeftClickedTile(null);
        controller.clear();

        for (Map.Entry<ChessPosition, ChessPiece> entry : state.getChessPieceMap().getPieceMap().entrySet()) {
            controller.getBoardManager().setPiece(entry.getKey(), entry.getValue());
        }

        controller.getBoardManager().getCurrentBoardState().updateFrom(state);
        controller.getBoardManager().setLastMove(state.getLastMove());
        controller.getBoardUI().repaintPieces();
        controller.getBoardUI().highlightLastMove();

        notifyGameStateListeners();
        SoundPlayer.playMoveSound();
        controller.checkGameEndConditionsAsync();
    }

    /**
     * Notifies game state listeners of a change.
     */
    private void notifyGameStateListeners() {
        controller.notifyGameStateChanged();
    }

    // Setters

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }
}