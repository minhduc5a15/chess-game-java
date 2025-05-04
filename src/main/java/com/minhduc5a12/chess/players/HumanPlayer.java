package com.minhduc5a12.chess.players;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.game.ChessController;

/**
 * Represents a human player who interacts with the chess game through the user interface.
 */
public class HumanPlayer implements Player {

    private static final Logger logger = LoggerFactory.getLogger(HumanPlayer.class);
    private final ChessController chessController;
    private final PieceColor playerColor;

    /**
     * Constructs a HumanPlayer with the specified chess controller and color.
     *
     * @param chessController the controller managing the chess game
     * @param playerColor     the color of the player (White or Black)
     */
    public HumanPlayer(ChessController chessController, PieceColor playerColor) {
        this.chessController = chessController;
        this.playerColor = playerColor;
        logger.info("HumanPlayer initialized with color: {}", playerColor.isWhite() ? "White" : "Black");
    }

    /**
     * Waits for user input to make a move via the user interface.
     * Does nothing as moves are handled through UI interactions.
     */
    @Override
    public void makeMove() {
        logger.debug("HumanPlayer waiting for user input for color: {}", playerColor);
    }

    /**
     * Returns the color of the human player.
     *
     * @return the PieceColor of the player
     */
    @Override
    public PieceColor getColor() {
        return playerColor;
    }

    /**
     * Cleans up resources for the human player.
     * No special cleanup is required.
     */
    @Override
    public void shutdown() {
        logger.info("HumanPlayer shutdown");
    }
}