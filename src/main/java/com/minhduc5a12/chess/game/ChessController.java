package com.minhduc5a12.chess.game;

import com.minhduc5a12.chess.constants.GameMode;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.model.BoardState;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;
import com.minhduc5a12.chess.core.pieces.*;
import com.minhduc5a12.chess.history.GameHistoryManager;
import com.minhduc5a12.chess.history.HistoryChangeListener;
import com.minhduc5a12.chess.players.HumanPlayer;
import com.minhduc5a12.chess.players.Player;
import com.minhduc5a12.chess.players.StockfishPlayer;
import com.minhduc5a12.chess.ui.PlayerPanelListener;
import com.minhduc5a12.chess.ui.board.ChessBoardUI;
import com.minhduc5a12.chess.ui.board.ChessTile;
import com.minhduc5a12.chess.ui.components.dialogs.GameOverDialog;
import com.minhduc5a12.chess.ui.components.dialogs.PromotionDialog;
import com.minhduc5a12.chess.utils.BoardUtils;
import com.minhduc5a12.chess.utils.SoundPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages the chess game, coordinating between the board state, players, and UI.
 * Implements game logic for moves, including castling, en passant, and pawn promotion.
 */
public final class ChessController implements MoveExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ChessController.class);
    private static final int FIFTY_MOVE_RULE_LIMIT = 50;

    private JFrame frame;
    private ChessBoard chessBoard;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Player whitePlayer;
    private Player blackPlayer;
    private int gameMode;
    private PieceColor humanPlayerColor;
    private boolean gameEnded;

    private final GameHistoryManager historyManager;
    private final GameActionManager actionManager;
    private final BoardManager boardManager;

    private final ChessBoardUI boardUI;

    private final List<PlayerPanelListener> playerPanelListeners = new ArrayList<>();
    private final List<GameStateListener> gameStateListeners = new ArrayList<>();
    private final List<HistoryChangeListener> historyChangeListeners = new ArrayList<>();

    /**
     * Constructs a new ChessController, initializing the game with default settings.
     * Sets up the board, UI, and game managers for a player vs. player game mode.
     */
    public ChessController() {
        super();
        this.gameEnded = false;
        this.historyManager = new GameHistoryManager();
        this.actionManager = new GameActionManager(this);
        this.boardManager = new BoardManager();
        this.boardUI = new ChessBoardUI(this, boardManager);
        this.gameMode = GameMode.PLAYER_VS_PLAYER;
        setupInitialPosition();
        boardManager.updateBoardStateHistory();
    }

    // --- Game Setup Methods ---

    /**
     * Sets up a player vs. player game mode with two human players.
     */
    public void setPlayerVsPlayer() {
        this.gameMode = GameMode.PLAYER_VS_PLAYER;
        this.whitePlayer = new HumanPlayer(this, PieceColor.WHITE);
        this.blackPlayer = new HumanPlayer(this, PieceColor.BLACK);
        notifyGameStateChanged();
    }

    /**
     * Sets up a player vs. AI game mode.
     *
     * @param humanColor the color of the human player (white or black)
     */
    public void setPlayerVsAI(PieceColor humanColor) {
        this.gameMode = GameMode.PLAYER_VS_AI;
        this.humanPlayerColor = humanColor;
        if (humanColor.isWhite()) {
            this.whitePlayer = new HumanPlayer(this, PieceColor.WHITE);
            this.blackPlayer = new StockfishPlayer(this, PieceColor.BLACK);
        } else {
            this.whitePlayer = new StockfishPlayer(this, PieceColor.WHITE);
            this.blackPlayer = new HumanPlayer(this, PieceColor.BLACK);
        }
        notifyGameStateChanged();
        if (humanColor.isBlack()) {
            whitePlayer.makeMove();
        }
    }

    /**
     * Sets up an AI vs. AI game mode with two AI players.
     */
    public void setAIVsAI() {
        this.gameMode = GameMode.AI_VS_AI;
        this.whitePlayer = new StockfishPlayer(this, PieceColor.WHITE);
        this.blackPlayer = new StockfishPlayer(this, PieceColor.BLACK);
        notifyGameStateChanged();
        whitePlayer.makeMove();
    }

    /**
     * Initializes the chess board to the standard starting position.
     */
    public void setupInitialPosition() {
        boardManager.setupInitialPosition();
        boardUI.repaintPieces();
    }

    /**
     * Clears the board and UI, resetting the game state.
     */
    public void clear() {
        boardManager.clear();
        boardUI.clear();
    }

    // --- Move Execution Methods ---

    /**
     * Executes a standard chess move, handling captures and pawn promotion.
     *
     * @param move           the move to execute
     * @param promotionPiece the piece to promote to, if applicable
     * @return true if the move was executed successfully, false otherwise
     */
    @Override
    public boolean executeMove(ChessMove move, ChessPiece promotionPiece) {
        ChessPiece piece = boardManager.getPiece(move.start());
        BoardState currentBoardState = boardManager.getCurrentBoardState();

        historyManager.clearRedoStack();

        boolean isCapture = boardManager.getPiece(move.end()) != null;
        boolean isPawnMove = piece instanceof Pawn;

        ChessTile startTile = boardUI.getTile(move.start());
        ChessTile endTile = boardUI.getTile(move.end());

        if (isCapture) {
            ChessPiece capturedPiece = boardManager.getPiece(move.end());
            notifyPieceCaptured(piece.getColor(), capturedPiece);
        }

        if (piece instanceof Pawn && (move.end().row() == 7 || move.end().row() == 0)) {
            if (promotionPiece != null) {
                piece = promotionPiece;
                logger.info("AI pawn promoted to {} at {}", piece.getClass().getSimpleName(), move.end().toChessNotation());
            } else {
                piece = promotePawn(move.end(), piece.getColor());
            }
            SoundPlayer.playMoveSound();
        }

        historyManager.saveStateForUndo(currentBoardState);

        boardManager.setLastMove(move);

        boardUI.updateBoardUI();

        boardManager.removePiece(move.end());
        boardManager.setPiece(move.end(), piece);
        boardManager.removePiece(move.start());

        boardManager.updatePieceMovement(move);

        startTile.setPiece(null);
        endTile.setPiece(piece);

        if (BoardUtils.isThreefoldRepetition(this.boardManager)) {
            gameEnded = true;
            SwingUtilities.invokeLater(() -> {
                GameOverDialog dialog = new GameOverDialog(frame, "Draw");
                dialog.setVisible(true);
            });
            logger.info("Game ended due to threefold repetition (FIDE)");
            boardUI.repaintTiles(startTile, endTile);
            notifyGameStateChanged();
            return true;
        }

        boardUI.repaintTiles(startTile, endTile);

        if (isCapture || isPawnMove) {
            currentBoardState.clearHalfmoveClock();
        } else {
            currentBoardState.incrementHalfmoveClock();
        }

        logger.debug("Executed move: {} to {}", move.start().toChessNotation(), move.end().toChessNotation());
        boardManager.updateBoardStateHistory();
        actionManager.switchTurn();
        notifyHistoryChangeListeners();

        boolean isCheck = BoardUtils.isKingInCheck(currentBoardState.getCurrentPlayerColor(), currentBoardState.getChessPieceMap());

        logger.debug("isCheck: {}", isCheck);

        if (isCheck) {
            SoundPlayer.playMoveCheckSound();
        } else if (isCapture) {
            SoundPlayer.playCaptureSound();
        } else {
            SoundPlayer.playMoveSound();
        }

        executor.submit(this::checkGameEndConditions);

        return true;
    }

    /**
     * Performs a castling move for the specified side and color.
     *
     * @param isKingside true for kingside castling, false for queenside
     * @param color      the color of the player performing the castling
     * @return true if castling was successful, false otherwise
     */
    @Override
    public boolean performCastling(boolean isKingside, PieceColor color) {
        ChessPosition kingPos = boardManager.getChessPieceMap().getKingPosition(color);
        BoardState currentBoardState = boardManager.getCurrentBoardState();
        if (kingPos == null) {
            logger.debug("King not found for color: {}", color);
            return false;
        }

        ChessPiece king = boardManager.getPiece(kingPos);
        if (!(king instanceof King kingPiece) || king.hasMoved()) {
            logger.debug("King has moved or not found at {}", kingPos.toChessNotation());
            return false;
        }

        boolean canCastle = isKingside ? kingPiece.canCastleKingside(kingPos, boardManager.getChessPieceMap()) : kingPiece.canCastleQueenside(kingPos, boardManager.getChessPieceMap());
        if (!canCastle) {
            logger.debug("Cannot castle {} for {}", isKingside ? "kingside" : "queenside", color);
            return false;
        }

        historyManager.clearRedoStack();

        int kingRow = (color.isWhite()) ? 0 : 7;
        int rookCol = isKingside ? 7 : 0;

        ChessPosition rookPos = new ChessPosition(rookCol, kingRow);
        ChessPiece rook = boardManager.getPiece(rookPos);

        int kingTargetCol = isKingside ? 6 : 2;
        int rookTargetCol = isKingside ? 5 : 3;

        ChessTile kingStartTile = boardUI.getTile(kingPos);
        ChessTile kingEndTile = boardUI.getTile(new ChessPosition(kingTargetCol, kingRow));
        ChessTile rookStartTile = boardUI.getTile(rookPos);
        ChessTile rookEndTile = boardUI.getTile(new ChessPosition(rookTargetCol, kingRow));

        historyManager.saveStateForUndo(currentBoardState);

        boardManager.setLastMove(new ChessMove(kingPos, new ChessPosition(kingTargetCol, kingRow)));

        boardUI.updateBoardUI();

        boardManager.removePiece(kingPos);
        boardManager.removePiece(rookPos);
        boardManager.setPiece(new ChessPosition(kingTargetCol, kingRow), king);
        boardManager.setPiece(new ChessPosition(rookTargetCol, kingRow), rook);

        king.setHasMoved(true);
        rook.setHasMoved(true);

        kingStartTile.setPiece(null);
        rookStartTile.setPiece(null);
        kingEndTile.setPiece(king);
        rookEndTile.setPiece(rook);

        boardManager.getCurrentBoardState().incrementHalfmoveClock();

        boardManager.updateBoardStateHistory();

        boardUI.repaintTiles(kingStartTile, kingEndTile, rookStartTile, rookEndTile);
        logger.debug("Castling performed: {} for {}", isKingside ? "Kingside" : "Queenside", color);
        executor.submit(this::checkGameEndConditions);

        actionManager.switchTurn();

        boolean isCheck = BoardUtils.isKingInCheck(boardManager.getCurrentPlayerColor(), boardManager.getChessPieceMap());

        if (isCheck) {
            SoundPlayer.playMoveCheckSound();
        }

        return true;
    }

    /**
     * Performs an en passant move.
     *
     * @param move the en passant move to execute
     * @return true if the move was successful, false otherwise
     */
    @Override
    public boolean performEnPassant(ChessMove move) {
        ChessPiece piece = boardManager.getPiece(move.start());
        BoardState currentBoardState = boardManager.getCurrentBoardState();
        if (!(piece instanceof Pawn) || gameEnded) {
            logger.debug("Not a pawn or game ended at {}", move.start().toChessNotation());
            return false;
        }

        ChessMove lastMove = boardManager.getLastMove();
        if (lastMove == null) {
            logger.debug("No last move for en passant check");
            return false;
        }

        ChessPiece lastMovedPiece = boardManager.getPiece(lastMove.end());
        if (!(lastMovedPiece instanceof Pawn) || Math.abs(lastMove.start().row() - lastMove.end().row()) != 2 || lastMove.end().row() != move.start().row() || Math.abs(lastMove.end().col() - move.start().col()) != 1) {
            logger.debug("Last move does not qualify for en passant");
            return false;
        }

        int direction = piece.getColor().isWhite() ? 1 : -1;
        ChessPosition targetPos = move.end();
        if (targetPos.row() != move.start().row() + direction || targetPos.col() != lastMove.end().col()) {
            logger.debug("Invalid en passant target position");
            return false;
        }

        ChessPieceMap tempMap = BoardUtils.simulateMove(move, boardManager.getChessPieceMap());
        tempMap.removePiece(lastMove.end());
        if (BoardUtils.isKingInCheck(piece.getColor(), tempMap)) {
            logger.debug("En passant invalid under check");
            return false;
        }

        historyManager.clearRedoStack();
        historyManager.saveStateForUndo(currentBoardState);

        ChessTile startTile = boardUI.getTile(move.start());
        ChessTile endTile = boardUI.getTile(move.end());
        ChessTile capturedTile = boardUI.getTile(lastMove.end());

        ChessPiece capturedPiece = boardManager.getPiece(lastMove.end());
        notifyPieceCaptured(piece.getColor(), capturedPiece);

        boardManager.setLastMove(move);

        boardUI.updateBoardUI();

        boardManager.removePiece(lastMove.end());
        boardManager.removePiece(move.start());
        boardManager.setPiece(move.end(), piece);
        boardManager.updatePieceMovement(move);
        boardManager.updateBoardStateHistory();

        startTile.setPiece(null);
        endTile.setPiece(piece);
        capturedTile.setPiece(null);

        boardUI.repaintTiles(startTile, endTile, capturedTile);
        logger.info("En passant performed: {} to {}, captured at {}", move.start().toChessNotation(), move.end().toChessNotation(), lastMove.end().toChessNotation());

        boardManager.getCurrentBoardState().clearHalfmoveClock();

        actionManager.switchTurn();

        executor.submit(this::checkGameEndConditions);

        return true;
    }

    /**
     * Promotes a pawn at the specified position to a chosen piece.
     *
     * @param position the position of the pawn to promote
     * @param color    the color of the pawn
     * @return the promoted piece (Queen, Rook, Bishop, or Knight)
     */
    @Override
    public ChessPiece promotePawn(ChessPosition position, PieceColor color) {
        PromotionDialog dialog = new PromotionDialog(frame, color);
        dialog.setVisible(true);
        String selectedPiece = dialog.getSelectedPiece();
        ChessPiece promotedPiece;

        switch (selectedPiece) {
            case "Queen" -> promotedPiece = new Queen(color);
            case "Rook" -> promotedPiece = new Rook(color);
            case "Bishop" -> promotedPiece = new Bishop(color);
            case "Knight" -> promotedPiece = new Knight(color);
            default -> {
                promotedPiece = new Queen(color);
                logger.error("Invalid promotion choice: {}, defaulting to Queen", selectedPiece);
            }
        }

        logger.info("Pawn promoted to {} at {}", selectedPiece, position.toChessNotation());
        return promotedPiece;
    }

    /**
     * Attempts to move a piece, handling special moves like castling, en passant, or promotion.
     *
     * @param move           the move to attempt
     * @param promotionPiece the piece to promote to, if applicable
     * @return true if the move was successful, false otherwise
     */
    public boolean movePiece(ChessMove move, ChessPiece promotionPiece) {
        ChessPiece piece = boardManager.getPiece(move.start());
        boolean moveSuccessful = false;
        if (piece == null || gameEnded || !boardUI.getCurrentValidMoves().contains(move) || !BoardUtils.isMoveValidUnderCheck(move, boardManager.getChessPieceMap())) {
            SoundPlayer.playMoveIllegal();
            logger.debug("No piece found at start position or game ended: {}", move.start().toChessNotation());
            boardUI.setCurrentLeftClickedTile(null);
            return false;
        }

        switch (piece) {
            case King king when Math.abs(move.end().col() - move.start().col()) == 2 -> {
                boolean isKingside = move.end().col() > move.start().col();
                if (performCastling(isKingside, piece.getColor())) {
                    SoundPlayer.playCastleSound();
                    moveSuccessful = true;
                } else {
                    SoundPlayer.playMoveIllegal();
                }
            }
            case Pawn pawn when boardManager.getPiece(move.end()) == null && move.start().col() != move.end().col() && Math.abs(move.start().row() - move.end().row()) == 1 -> {
                if (performEnPassant(move)) {
                    SoundPlayer.playCaptureSound();
                    moveSuccessful = true;
                } else {
                    SoundPlayer.playMoveIllegal();
                }
            }
            default -> {
                moveSuccessful = executeMove(move, promotionPiece);
            }
        }

        if (moveSuccessful) {
            Player nextPlayer = boardManager.getCurrentBoardState().getCurrentPlayerColor().isWhite() ? whitePlayer : blackPlayer;
            if (gameMode == GameMode.PLAYER_VS_AI && nextPlayer.getColor() != humanPlayerColor) {
                nextPlayer.makeMove();
            } else if (gameMode == GameMode.AI_VS_AI) {
                nextPlayer.makeMove();
            }
        }

        return moveSuccessful;
    }

    /**
     * Attempts to move a piece without specifying a promotion piece.
     *
     * @param move the move to attempt
     * @return true if the move was successful, false otherwise
     */
    public boolean movePiece(ChessMove move) {
        return movePiece(move, null);
    }

    // --- Listener Management ---

    /**
     * Adds a listener for player panel updates.
     *
     * @param listener the listener to add
     */
    public void addPlayerPanelListener(PlayerPanelListener listener) {
        playerPanelListeners.add(listener);
    }

    /**
     * Adds a listener for game state changes.
     *
     * @param listener the listener to add
     */
    public void addGameStateListener(GameStateListener listener) {
        gameStateListeners.add(listener);
    }

    /**
     * Adds a listener for history changes.
     *
     * @param listener the listener to add
     */
    public void addHistoryChangeListener(HistoryChangeListener listener) {
        historyChangeListeners.add(listener);
    }

    /**
     * Notifies all history change listeners of a history update.
     */
    void notifyHistoryChangeListeners() {
        logger.debug("Notifying {} HistoryChangeListeners of history change", historyChangeListeners.size());
        for (HistoryChangeListener listener : new ArrayList<>(historyChangeListeners)) {
            SwingUtilities.invokeLater(listener::onHistoryChanged);
        }
    }

    /**
     * Notifies all game state listeners of a change in game state.
     */
    void notifyGameStateChanged() {
        logger.debug("Notifying {} GameStateListeners of game state change", gameStateListeners.size());
        for (GameStateListener listener : new ArrayList<>(gameStateListeners)) {
            SwingUtilities.invokeLater(listener::onGameStateChanged);
        }
    }

    /**
     * Updates the score for both players based on material advantage.
     */
    void notifyScoreUpdated() {
        int materialAdvantage = boardManager.getChessPieceMap().getMaterialAdvantage();
        for (PlayerPanelListener listener : playerPanelListeners) {
            listener.onScoreUpdated(PieceColor.WHITE, materialAdvantage);
            listener.onScoreUpdated(PieceColor.BLACK, -materialAdvantage);
        }
    }

    /**
     * Notifies listeners of a turn change.
     */
    void notifyTurnChanged() {
        for (PlayerPanelListener listener : playerPanelListeners) {
            listener.onTurnChanged(boardManager.getCurrentBoardState().getCurrentPlayerColor());
        }
    }

    /**
     * Notifies listeners when a piece is captured.
     *
     * @param capturerColor the color of the capturing player
     * @param capturedPiece the captured piece
     */
    private void notifyPieceCaptured(PieceColor capturerColor, ChessPiece capturedPiece) {
        for (PlayerPanelListener listener : playerPanelListeners) {
            listener.onPieceCaptured(capturerColor, capturedPiece);
        }
    }

    // --- Game End Conditions ---

    /**
     * Checks for game-ending conditions such as checkmate, stalemate, or draws.
     */
    private void checkGameEndConditions() {
        BoardState currentBoardState = boardManager.getCurrentBoardState();

        if (BoardUtils.isCheckmate(currentBoardState.getCurrentPlayerColor(), boardManager.getChessPieceMap())) {
            gameEnded = true;
            SwingUtilities.invokeLater(this::showGameOverDialog);
        } else if (currentBoardState.getHalfmoveClock() >= FIFTY_MOVE_RULE_LIMIT) {
            gameEnded = true;
            SwingUtilities.invokeLater(() -> {
                GameOverDialog dialog = new GameOverDialog(frame, "Draw game!!!!");
                dialog.setVisible(true);
            });
            logger.info("Game ended due to 50-move rule");
        } else if (BoardUtils.isDeadPosition(boardManager.getChessPieceMap())) {
            gameEnded = true;
            SwingUtilities.invokeLater(() -> {
                GameOverDialog dialog = new GameOverDialog(frame, "Draw game!!!!");
                dialog.setVisible(true);
            });
            logger.info("Game ended due to dead position (insufficient material)");
        } else if (BoardUtils.isStalemate(currentBoardState.getCurrentPlayerColor(), boardManager.getChessPieceMap())) {
            gameEnded = true;
            SwingUtilities.invokeLater(() -> {
                GameOverDialog dialog = new GameOverDialog(frame, "Stalemate!");
                dialog.setVisible(true);
            });
            logger.info("Game ended due to stalemate");
        }
        if (gameEnded) {
            notifyGameStateChanged();
        }
    }

    /**
     * Asynchronously checks for game-ending conditions.
     */
    public void checkGameEndConditionsAsync() {
        executor.submit(this::checkGameEndConditions);
    }

    /**
     * Displays a game-over dialog indicating the winner or draw.
     */
    private void showGameOverDialog() {
        String winner = (boardManager.getCurrentBoardState().getCurrentPlayerColor().isWhite()) ? "Black" : "White";
        GameOverDialog dialog = new GameOverDialog(frame, "Checkmate " + winner + " player" + " win!");
        dialog.setVisible(true);
    }

    // --- Shutdown ---

    /**
     * Shuts down the controller, releasing resources and stopping players.
     */
    public void shutdown() {
        executor.shutdown();
        SoundPlayer.shutdown();
        if (whitePlayer != null) {
            whitePlayer.shutdown();
        }
        if (blackPlayer != null) {
            blackPlayer.shutdown();
        }
        actionManager.shutdown();
        logger.debug("Shutting down ChessController");
    }

    // --- Getters and Setters ---

    public GameActionManager getActionManager() {
        return actionManager;
    }

    public ChessBoard getChessBoard() {
        return chessBoard;
    }

    public GameHistoryManager getHistoryManager() {
        return historyManager;
    }

    public int getGameMode() {
        return gameMode;
    }

    public BoardManager getBoardManager() {
        return boardManager;
    }

    public PieceColor getHumanPlayerColor() {
        if (gameMode == GameMode.PLAYER_VS_AI) {
            return humanPlayerColor;
        }
        return null;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public ChessBoardUI getBoardUI() {
        return boardUI;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
        actionManager.setFrame(frame);
    }

    public void setChessBoard(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }

    public void setGameEnded(boolean gameEnded) {
        this.gameEnded = gameEnded;
    }
}