package com.minhduc5a12.chess.players;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.core.model.ChessPosition;
import com.minhduc5a12.chess.engine.Stockfish;
import com.minhduc5a12.chess.game.ChessController;
import com.minhduc5a12.chess.ui.board.ChessTile;
import com.minhduc5a12.chess.utils.ChessNotationUtils;

/**
 * Represents an AI player powered by the Stockfish chess engine.
 */
public class StockfishPlayer implements Player {

    private static final Logger logger = LoggerFactory.getLogger(StockfishPlayer.class);
    private final Stockfish stockfishEngine;
    private final ChessController chessController;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static final int MOVE_DELAY_TIME = 1500;
    private final PieceColor stockfishColor;

    /**
     * Constructs a StockfishPlayer with the specified chess controller and color.
     *
     * @param chessController the controller managing the chess game
     * @param stockfishColor  the color of the player (White or Black)
     */
    public StockfishPlayer(ChessController chessController, PieceColor stockfishColor) {
        this.stockfishEngine = new Stockfish();
        this.stockfishEngine.start();
        this.chessController = chessController;
        this.stockfishColor = stockfishColor;
        logger.info("Stockfish player initialized with color: {}", stockfishColor.isWhite() ? "White" : "Black");
    }

    /**
     * Executes a move using the Stockfish engine after a short delay.
     */
    @Override
    public void makeMove() {
        executor.schedule(() -> {
            if (chessController.isGameEnded()) {
                return;
            }
            try {
                String bestMoveStr = stockfishEngine.getBestMove(ChessNotationUtils.getFEN(chessController.getCurrentBoardState()));
                if (bestMoveStr != null) {
                    String startPos = bestMoveStr.substring(0, 2);
                    String endPos = bestMoveStr.substring(2, 4);
                    ChessPosition start = ChessPosition.toChessPosition(startPos);
                    ChessPosition end = ChessPosition.toChessPosition(endPos);
                    ChessMove move = new ChessMove(start, end);

                    ChessTile startTile = chessController.getTile(start);
                    if (startTile != null && startTile.getPiece() != null) {
                        chessController.setCurrentLeftClickedTile(startTile);
                        logger.debug("Generated valid moves for AI piece at {}", startPos);
                    } else {
                        logger.warn("No piece found at start position: {}", startPos);
                        return;
                    }

                    if (bestMoveStr.length() > 4) {
                        char promotion = bestMoveStr.charAt(4);
                        logger.info("Promotion detected: {} to {} with promotion to {}", startPos, endPos, promotion);
                    } else if (isCastling(start, end)) {
                        logger.info("Castling move detected: {} to {}", startPos, endPos);
                    } else {
                        logger.info("Best move from Stockfish: {} to {}", startPos, endPos);
                    }

                    boolean success = chessController.movePiece(move);
                    if (!success) {
                        logger.warn("Failed to execute Stockfish move: {} to {}", startPos, endPos);
                    } else {
                        chessController.setCurrentLeftClickedTile(null);
                    }
                } else {
                    logger.warn("No best move returned by Stockfish");
                }
            } catch (Exception e) {
                logger.error("Error making move with Stockfish", e);
            }
        }, MOVE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the color of the Stockfish player.
     *
     * @return the PieceColor of the player
     */
    @Override
    public PieceColor getColor() {
        return stockfishColor;
    }

    /**
     * Cleans up resources by stopping the Stockfish engine and shutting down the executor.
     */
    @Override
    public void shutdown() {
        stockfishEngine.stopEngine();
        executor.shutdown();
        logger.info("StockfishPlayer shutdown");
    }

    private boolean isCastling(ChessPosition start, ChessPosition end) {
        if (start.row() == end.row() && Math.abs(start.col() - end.col()) == 2) {
            return start.toChessNotation().equals("e1") || start.toChessNotation().equals("e8");
        }
        return false;
    }
}