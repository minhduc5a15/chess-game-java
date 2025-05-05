package com.minhduc5a12.chess.game;

import com.minhduc5a12.chess.constants.GameConstants;
import com.minhduc5a12.chess.constants.GameMode;
import com.minhduc5a12.chess.ui.board.ChessTile;
import com.minhduc5a12.chess.utils.ImageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Represents the chess board UI, handling tile layout and board rendering.
 */
public class ChessBoard extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ChessBoard.class);
    private final ChessController chessController;
    private final Image boardImage;
    private boolean isFlipped;

    /**
     * Constructs a new ChessBoard with the specified controller.
     *
     * @param chessController The ChessController managing the game.
     */
    public ChessBoard(ChessController chessController) {
        this.chessController = chessController;
        this.isFlipped = chessController.getGameMode() == GameMode.PLAYER_VS_AI && Objects.requireNonNull(chessController.getHumanPlayerColor()).isBlack();
        this.boardImage = ImageLoader.getImage("images/chessboard.png", GameConstants.Board.BOARD_WIDTH, GameConstants.Board.BOARD_HEIGHT);
        setPreferredSize(new Dimension(GameConstants.Board.BOARD_WIDTH, GameConstants.Board.BOARD_HEIGHT));
        setOpaque(true);
        setLayout(new GridLayout(8, 8, 0, 0));
        initializeBoard();
    }

    /**
     * Paints the board by drawing the background image.
     *
     * @param g The Graphics context to paint on.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawDefaultBoard(g);
    }

    /**
     * Initializes the board by adding tiles in the correct order.
     */
    private void initializeBoard() {
        removeAll();
        addTilesToBoard();
        chessController.getBoardUI().repaintPieces();
        revalidate();
        repaint();
        logger.debug("Initialized board, isFlipped: {}", isFlipped);
    }

    /**
     * Draws the default board image.
     *
     * @param g The Graphics context to draw on.
     */
    private void drawDefaultBoard(Graphics g) {
        g.drawImage(boardImage, 0, 0, getWidth(), getHeight(), null);
    }

    /**
     * Adds tiles to the board, respecting the flipped state.
     */
    private void addTilesToBoard() {
        ChessTile[][] tiles = chessController.getBoardUI().getTiles();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int displayRow = isFlipped ? 7 - row : row;
                int displayCol = isFlipped ? 7 - col : col;
                add(tiles[displayRow][displayCol]);
            }
        }
    }

    /**
     * Flips the board orientation and reinitializes the layout.
     */
    public void flipBoard() {
        isFlipped = !isFlipped;
        initializeBoard();
        logger.info("Board flipped: {}", isFlipped);
    }
}