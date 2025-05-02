package com.minhduc5a12.chess.game;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.JPanel;

import com.minhduc5a12.chess.ui.board.ChessTile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minhduc5a12.chess.constants.GameConstants;
import com.minhduc5a12.chess.constants.GameMode;
import com.minhduc5a12.chess.utils.ImageLoader;

public class ChessBoard extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ChessBoard.class);
    private final ChessController chessController;
    private final Image boardImage;
    private boolean isFlipped;

    public ChessBoard(ChessController chessController) {
        this.chessController = chessController;
        this.isFlipped = chessController.getGameMode() == GameMode.PLAYER_VS_AI && chessController.getHumanPlayerColor().isBlack();
        this.boardImage = ImageLoader.getImage("images/chessboard.png", GameConstants.Board.BOARD_WIDTH, GameConstants.Board.BOARD_HEIGHT);
        setPreferredSize(new Dimension(GameConstants.Board.BOARD_WIDTH, GameConstants.Board.BOARD_HEIGHT));
        setOpaque(true);
        setLayout(new GridLayout(8, 8, 0, 0));
        initializeBoard();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawDefaultBoard(g);
    }

    private void initializeBoard() {
        removeAll();
        ChessTile[][] tiles = chessController.getTiles();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int displayRow = isFlipped ? 7 - row : row;
                int displayCol = isFlipped ? 7 - col : col;
                add(tiles[displayRow][displayCol]);
            }
        }
        revalidate();
        repaint();
    }

    private void drawDefaultBoard(Graphics g) {
        g.drawImage(this.boardImage, 0, 0, this.getWidth(), this.getHeight(), null);
    }

    public void flipBoard() {
        isFlipped = !isFlipped;
        initializeBoard();
        logger.info("Board flipped: {}", isFlipped);
    }
} 