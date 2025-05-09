package com.minhduc5a12.chess.history;

import com.minhduc5a12.chess.core.model.BoardState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class GameHistoryManager {

    private static final Logger logger = LoggerFactory.getLogger(GameHistoryManager.class);

    private final Map<BoardState, Integer> boardStateHistory;
    private final Stack<BoardState> undoStack;
    private final Stack<BoardState> redoStack;

    public GameHistoryManager() {
        this.boardStateHistory = new ConcurrentHashMap<>();
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    public void saveStateForUndo(BoardState state) {
        if (state == null) {
            logger.warn("Attempted to save null BoardState for undo");
            return;
        }

        BoardState stateCopy = state.deepCopy();
        undoStack.push(stateCopy);
        incrementBoardStateCount(stateCopy);
        logger.debug("Saved state for undo. Last move: {}, Undo stack size: {}", stateCopy.getLastMove(), undoStack.size());
    }

    public void saveStateForRedo(BoardState state) {
        if (state == null) {
            logger.warn("Attempted to save null BoardState for redo");
            return;
        }
        BoardState stateCopy = state.deepCopy();
        redoStack.push(stateCopy);
        logger.debug("Saved state for redo. Redo stack size: {}", redoStack.size());
    }

    public Stack<BoardState> getUndoStack() {
        return undoStack;
    }

    public Stack<BoardState> getRedoStack() {
        return redoStack;
    }

    public void clearRedoStack() {
        this.redoStack.clear();
    }

    public void incrementBoardStateCount(BoardState state) {
        if (state == null) {
            logger.warn("Attempted to increment count for null BoardState");
            return;
        }
        boardStateHistory.merge(state, 1, Integer::sum);
        logger.debug("Incremented board state count: {} -> {}", state, boardStateHistory.get(state));
    }

    public void decrementBoardStateCount(BoardState state) {
        if (state == null) {
            logger.warn("Attempted to decrement count for null BoardState");
            return;
        }
        boardStateHistory.computeIfPresent(state, (k, v) -> v > 1 ? v - 1 : null);
        logger.debug("Decremented board state count: {} -> {}", state, boardStateHistory.getOrDefault(state, 0));
    }
}
