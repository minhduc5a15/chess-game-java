package com.minhduc5a12.chess;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.minhduc5a12.chess.constants.GameMode;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.model.BoardState;
import com.minhduc5a12.chess.model.ChessMove;
import com.minhduc5a12.chess.model.ChessPiece;
import com.minhduc5a12.chess.model.ChessPosition;
import com.minhduc5a12.chess.pieces.Bishop;
import com.minhduc5a12.chess.pieces.ChessPieceMap;
import com.minhduc5a12.chess.pieces.King;
import com.minhduc5a12.chess.pieces.Knight;
import com.minhduc5a12.chess.pieces.Pawn;
import com.minhduc5a12.chess.pieces.Queen;
import com.minhduc5a12.chess.pieces.Rook;
import com.minhduc5a12.chess.players.StockfishPlayer;
import com.minhduc5a12.chess.ui.GameOverDialog;
import com.minhduc5a12.chess.ui.PromotionDialog;
import com.minhduc5a12.chess.utils.BoardUtils;
import com.minhduc5a12.chess.utils.SoundPlayer;

public class ChessController extends BoardManager implements MoveExecutor {

    private static final int FIFTY_MOVE_RULE_LIMIT = 50;

    private boolean gameEnded;
    private JFrame frame;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private StockfishPlayer stockfishPlayer;
    private int gameMode = GameMode.PLAYER_VS_PLAYER;
    private PieceColor humanPlayerColor;

    private final List<PlayerPanelListener> listeners = new ArrayList<>();

    public void addPlayerPanelListener(PlayerPanelListener listener) {
        listeners.add(listener);
    }

    public ChessController() {
        super();
        this.gameEnded = false;
        setupInitialPosition();
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public void setPlayerVsAI(PieceColor humanColor) {
        this.gameMode = GameMode.PLAYER_VS_AI;
        this.humanPlayerColor = humanColor;
        this.stockfishPlayer = new StockfishPlayer(this, humanColor.getOpponent());
        if (humanColor.isBlack()) {
            stockfishPlayer.makeMove();
        }
    }

    public void setAIVsAI() {
        this.gameMode = GameMode.AI_VS_AI;
        this.stockfishPlayer = new StockfishPlayer(this, PieceColor.WHITE);
        stockfishPlayer.makeMove();
    }

    private void notifyScoreUpdated() {
        int materialAdvantage = getChessPieceMap().getMaterialAdvantage();
        for (PlayerPanelListener listener : listeners) {
            listener.onScoreUpdated(PieceColor.WHITE, materialAdvantage);
            listener.onScoreUpdated(PieceColor.BLACK, -materialAdvantage);
        }
    }

    private void notifyTurnChanged() {
        for (PlayerPanelListener listener : listeners) {
            listener.onTurnChanged(getCurrentBoardState().getCurrentPlayerColor());
        }
    }

    private void notifyPieceCaptured(PieceColor capturerColor, ChessPiece capturedPiece) {
        for (PlayerPanelListener listener : listeners) {
            listener.onPieceCaptured(capturerColor, capturedPiece);
        }
    }

    @Override
    public boolean executeMove(ChessMove move) {
        ChessPiece piece = getPiece(move.start());

        BoardState currentBoardState = getCurrentBoardState();

        boolean isCapture = getPiece(move.end()) != null;
        boolean isPawnMove = piece instanceof Pawn;

        ChessTile startTile = getTile(move.start());
        ChessTile endTile = getTile(move.end());

        if (isCapture) {
            ChessPiece capturedPiece = getPiece(move.end());
            notifyPieceCaptured(piece.getColor(), capturedPiece);
        }

        if (piece instanceof Pawn && (move.end().row() == 7 || move.end().row() == 0)) {
            piece = promotePawn(move.end(), piece.getColor());
            SoundPlayer.playMoveSound();
        }

        setLastMove(move);

        removePiece(move.end());
        setPiece(move.end(), piece);
        removePiece(move.start());

        updatePieceMovement(move);

        updateBoardStateHistory();
        if (BoardUtils.isThreefoldRepetition(this)) {
            gameEnded = true;
            SwingUtilities.invokeLater(() -> {
                GameOverDialog dialog = new GameOverDialog(frame, "Draw");
                dialog.setVisible(true);
            });
            logger.info("Game ended due to threefold repetition (FIDE)");
            repaintTiles(startTile, endTile);
            return true;
        }

        repaintTiles(startTile, endTile);
        if (isCapture || isPawnMove) {
            currentBoardState.clearHalfmoveClock();
        } else {
            currentBoardState.incrementHalfmoveClock();
        }

        switchTurn();
        notifyTurnChanged();
        notifyScoreUpdated();

        boolean isCheck = BoardUtils.isKingInCheck(currentBoardState.getCurrentPlayerColor(), currentBoardState.getChessPieceMap());

        if (isCheck) {
            SoundPlayer.playMoveCheckSound();
        } else if (isCapture) {
            SoundPlayer.playCaptureSound();
        } else {
            SoundPlayer.playMoveSound();
        }

        logger.debug("Executed move: {} to {}", move.start().toChessNotation(), move.end().toChessNotation());

        executor.submit(this::checkGameEndConditions);

        return true;
    }

    @Override
    public boolean performCastling(boolean isKingside, PieceColor color) {
        ChessPosition kingPos = getChessPieceMap().getKingPosition(color);
        if (kingPos == null) {
            logger.debug("King not found for color: {}", color);
            return false;
        }

        ChessPiece king = getPiece(kingPos);
        if (!(king instanceof King kingPiece) || king.hasMoved()) {
            logger.debug("King has moved or not found at {}", kingPos.toChessNotation());
            return false;
        }

        boolean canCastle = isKingside ? kingPiece.canCastleKingside(kingPos, getChessPieceMap()) : kingPiece.canCastleQueenside(kingPos, getChessPieceMap());
        if (!canCastle) {
            logger.debug("Cannot castle {} for {}", isKingside ? "kingside" : "queenside", color);
            return false;
        }

        int kingRow = (color.isWhite()) ? 0 : 7;
        int rookCol = isKingside ? 7 : 0;

        ChessPosition rookPos = new ChessPosition(rookCol, kingRow);
        ChessPiece rook = getPiece(rookPos);

        int kingTargetCol = isKingside ? 6 : 2;
        int rookTargetCol = isKingside ? 5 : 3;

        ChessTile kingStartTile = getTile(kingPos);
        ChessTile kingEndTile = getTiles()[kingRow][kingTargetCol];
        ChessTile rookStartTile = getTile(rookPos);
        ChessTile rookEndTile = getTiles()[kingRow][rookTargetCol];

        setLastMove(new ChessMove(kingPos, new ChessPosition(kingTargetCol, kingRow)));

        removePiece(kingPos);
        removePiece(rookPos);
        setPiece(new ChessPosition(kingTargetCol, kingRow), king);
        setPiece(new ChessPosition(rookTargetCol, kingRow), rook);
        king.setHasMoved(true);
        rook.setHasMoved(true);

        updateBoardStateHistory();

        repaintTiles(kingStartTile, kingEndTile, rookStartTile, rookEndTile);
        logger.debug("Castling performed: {} for {}", isKingside ? "Kingside" : "Queenside", color);

        switchTurn();
        notifyScoreUpdated();
        notifyTurnChanged();
        getCurrentBoardState().incrementHalfmoveClock();
        executor.submit(this::checkGameEndConditions);

        return true;
    }

    @Override
    public boolean performEnPassant(ChessMove move) {
        ChessPiece piece = getPiece(move.start());
        if (!(piece instanceof Pawn) || gameEnded) {
            logger.debug("Not a pawn or game ended at {}", move.start().toChessNotation());
            return false;
        }

        ChessMove lastMove = getLastMove();
        if (lastMove == null) {
            logger.debug("No last move for en passant check");
            return false;
        }

        ChessPiece lastMovedPiece = getPiece(lastMove.end());
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

        ChessPieceMap tempMap = BoardUtils.simulateMove(move, getChessPieceMap());
        tempMap.removePiece(lastMove.end());
        if (BoardUtils.isKingInCheck(piece.getColor(), tempMap)) {
            logger.debug("En passant invalid under check");
            return false;
        }

        ChessTile startTile = getTile(move.start());
        ChessTile endTile = getTile(move.end());
        ChessTile capturedTile = getTile(lastMove.end());

        ChessPiece capturedPiece = getPiece(lastMove.end());
        notifyPieceCaptured(piece.getColor(), capturedPiece);

        setLastMove(move);

        removePiece(lastMove.end());
        removePiece(move.start());
        setPiece(move.end(), piece);
        updatePieceMovement(move);
        updateBoardStateHistory();

        repaintTiles(startTile, endTile, capturedTile);
        logger.info("En passant performed: {} to {}, captured at {}", move.start().toChessNotation(), move.end().toChessNotation(), lastMove.end().toChessNotation());

        getCurrentBoardState().clearHalfmoveClock();
        switchTurn();
        notifyScoreUpdated();
        notifyTurnChanged();
        executor.submit(this::checkGameEndConditions);

        return true;
    }

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

    public boolean movePiece(ChessMove move) {
        ChessPiece piece = getPiece(move.start());
        boolean moveSuccessful = false;
        if (piece == null || gameEnded || !getCurrentValidMoves().contains(move) || !BoardUtils.isMoveValidUnderCheck(move, getChessPieceMap())) {
            SoundPlayer.playMoveIllegal();
            logger.debug("No piece found at start position or game ended: {}", move.start().toChessNotation());
            setCurrentLeftClickedTile(null);
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
            case Pawn pawn when getPiece(move.end()) == null && move.start().col() != move.end().col() && Math.abs(move.start().row() - move.end().row()) == 1 -> {
                if (performEnPassant(move)) {
                    SoundPlayer.playCaptureSound();
                    moveSuccessful = true;
                } else {
                    SoundPlayer.playMoveIllegal();
                }
            }
            default -> {
                moveSuccessful = executeMove(move);
            }
        }

        if (moveSuccessful && stockfishPlayer != null) {
            if (gameMode == GameMode.PLAYER_VS_AI && getCurrentBoardState().getCurrentPlayerColor() != humanPlayerColor) {
                stockfishPlayer.makeMove();
            } else if (gameMode == GameMode.AI_VS_AI) {
                stockfishPlayer.makeMove();
            }
        }

        return moveSuccessful;
    }

    private void showGameOverDialog() {
        String winner = (getCurrentBoardState().getCurrentPlayerColor().isWhite()) ? "Black" : "White";
        GameOverDialog dialog = new GameOverDialog(frame, "Checkmate " + winner + " player" + " win!");
        dialog.setVisible(true);
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public void shutdown() {
        executor.shutdown();
        SoundPlayer.shutdown();
        if (this.gameMode == GameMode.AI_VS_AI) {
            stockfishPlayer.shutdown();
        }
    }

    private void checkGameEndConditions() {
        BoardState currentBoardState = getCurrentBoardState();

        if (BoardUtils.isCheckmate(currentBoardState.getCurrentPlayerColor(), getChessPieceMap())) {
            gameEnded = true;
            SwingUtilities.invokeLater(this::showGameOverDialog);
        } else if (currentBoardState.getHalfmoveClock() >= FIFTY_MOVE_RULE_LIMIT) {
            gameEnded = true;
            SwingUtilities.invokeLater(() -> {
                GameOverDialog dialog = new GameOverDialog(frame, "Draw game!!!!");
                dialog.setVisible(true);
            });
            logger.info("Game ended due to 50-move rule");
        } else if (BoardUtils.isDeadPosition(getChessPieceMap())) {
            gameEnded = true;
            SwingUtilities.invokeLater(() -> {
                GameOverDialog dialog = new GameOverDialog(frame, "Draw game!!!!");
                dialog.setVisible(true);
            });
            logger.info("Game ended due to dead position (insufficient material)");
        } else if (BoardUtils.isStalemate(currentBoardState.getCurrentPlayerColor(), getChessPieceMap())) {
            gameEnded = true;
            SwingUtilities.invokeLater(() -> {
                GameOverDialog dialog = new GameOverDialog(frame, "Stalemate!");
                dialog.setVisible(true);
            });
            logger.info("Game ended due to stalemate");
        }
    }

    public int getGameMode() {
        return gameMode;
    }

    public PieceColor getHumanPlayerColor() {
        if (gameMode == GameMode.PLAYER_VS_AI) {
            return humanPlayerColor;
        }
        return null;
    }

    public void resignGame() {
        PieceColor currentPlayerColor = getCurrentBoardState().getCurrentPlayerColor();
        if (!gameEnded) {
            gameEnded = true;
            String winner = currentPlayerColor.isWhite() ? "BLACK" : "WHITE";
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
        }
    }
}
