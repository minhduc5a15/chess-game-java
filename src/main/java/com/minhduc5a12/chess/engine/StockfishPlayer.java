package com.minhduc5a12.chess.engine;

import com.minhduc5a12.chess.GameController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockfishPlayer {
    private static final Logger logger = LoggerFactory.getLogger(StockfishPlayer.class);
    private final Stockfish stockfish;
    private final GameController gameController;

    public StockfishPlayer(GameController gameController) {
        this.stockfish = new Stockfish();
        this.gameController = gameController;
    }

    public void makeMove() {
        logger.info("StockfishPlayer initiating move for Black");
        stockfish.makeMove(gameController);
    }

    public void close() {
        stockfish.close();
    }
}