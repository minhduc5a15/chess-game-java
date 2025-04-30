package com.minhduc5a12.chess.players;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minhduc5a12.chess.ChessController;
import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.engine.Stockfish;
import com.minhduc5a12.chess.model.ChessMove;
import com.minhduc5a12.chess.model.ChessPosition;
import com.minhduc5a12.chess.utils.ChessNotationUtils;

public class StockfishPlayer {
    private static final Logger logger = LoggerFactory.getLogger(StockfishPlayer.class);
    private final Stockfish stockfishEngine;
    private final ChessController chessController;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ChessNotationUtils chessNotationUtils = new ChessNotationUtils();
    private static final int MOVE_DELAY_TIME = 1500;
    private final PieceColor stockfishColor;

    public StockfishPlayer(ChessController chessController, PieceColor stockfishColor) {
        this.stockfishEngine = new Stockfish();
        this.stockfishEngine.start();
        this.chessController = chessController;
        this.stockfishColor = stockfishColor;
        logger.info("Stockfish player initialized with color: {}", stockfishColor.isWhite() ? "White" : "Black");
    }

    public void makeMove() {
        executor.schedule(() -> {
            if (chessController.isGameEnded()) return;
            try {
                String bestMoveStr = stockfishEngine.getBestMove(chessNotationUtils.getFEN(chessController.getCurrentBoardState()));
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

    private boolean isCastling(ChessPosition start, ChessPosition end) {
        if (start.row() == end.row() && Math.abs(start.col() - end.col()) == 2) {
            return start.toChessNotation().equals("e1") || start.toChessNotation().equals("e8");
        }
        return false;
    }

    public void shutdown() {
        stockfishEngine.stopEngine();
        executor.shutdown();
    }
}