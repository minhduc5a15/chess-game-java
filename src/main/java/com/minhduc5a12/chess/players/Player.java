package com.minhduc5a12.chess.players;

import com.minhduc5a12.chess.constants.PieceColor;

/**
 * Defines the behavior of a player in the chess game.
 */
public interface Player {
    /**
     * Executes a move on the chess board.
     */
    void makeMove();

    /**
     * Returns the color of the player (White or Black).
     *
     * @return the PieceColor of the player
     */
    PieceColor getColor();

    /**
     * Cleans up resources when the player is terminated.
     */
    void shutdown();
}