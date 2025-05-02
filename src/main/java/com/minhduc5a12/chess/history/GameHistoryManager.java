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
        if (!redoStack.empty()) redoStack.clear();
        boardStateHistory.merge(stateCopy, 1, Integer::sum);
        logger.debug("Saved state for undo. Board state count: {}, Undo stack size: {}",
                boardStateHistory.get(stateCopy), undoStack.size());
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

    public Map<BoardState, Integer> getBoardStateHistory() {
        return boardStateHistory;
    }
}
