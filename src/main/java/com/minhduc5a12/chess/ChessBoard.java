package com.minhduc5a12.chess;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.minhduc5a12.chess.model.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChessBoard extends JPanel {
    private static final int BOARD_SIZE = 8;
    private static final int TILE_SIZE = 100;

    private final ChessTile[][] tiles = new ChessTile[BOARD_SIZE][BOARD_SIZE];
    private ChessTile selectedTile = null;
    private final GameEngine gameEngine;
    private BufferedImage chessboardImage;
    private static final Logger logger = LoggerFactory.getLogger(ChessBoard.class);

    public ChessBoard(GameEngine gameEngine) {
        if (gameEngine == null) {
            throw new IllegalArgumentException("gameEngine cannot be null");
        }
        this.gameEngine = gameEngine;
        setPreferredSize(new Dimension(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE));
        setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        initializeTiles();
        loadChessboardImage();
        addMouseListener(new ChessMouseListener());
        addMouseMotionListener(new ChessMouseMotionListener());
    }

    protected void initializeTiles() {
        removeAll();
        ChessTile[][] board = gameEngine.getBoard();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                tiles[row][col] = board[row][col];
                add(tiles[row][col]);
            }
        }
        revalidate();
    }

    private void loadChessboardImage() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            java.net.URL imageUrl = classLoader.getResource("images/chessboard.png");
            if (imageUrl == null) throw new IOException("Cannot find image: chessboard.png");
            BufferedImage originalImage = ImageIO.read(imageUrl);
            chessboardImage = new BufferedImage(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = chessboardImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, 800, 800, null);
            g2d.dispose();
        } catch (IOException e) {
            logger.error("Failed to load chessboard image", e);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (chessboardImage != null) {
            g.drawImage(chessboardImage, 0, 0, null);
        }
    }

    private class ChessMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (gameEngine.isGameEnded()) return;
            int col = e.getX() / TILE_SIZE;
            int row = e.getY() / TILE_SIZE;

            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                ChessTile clickedTile = tiles[row][col];

                if (selectedTile == null) {
                    if (clickedTile.getPiece() != null && clickedTile.getPiece().getColor() == gameEngine.getCurrentPlayerColor()) {
                        selectedTile = clickedTile;
                        selectedTile.setSelected(true);
                        List<Move> validMoves = selectedTile.getPiece().generateValidMoves(selectedTile.getCol(), selectedTile.getRow(), gameEngine.getBoard());
                        List<Move> filteredMoves = new ArrayList<>();
                        for (Move move : validMoves) {
                            if (gameEngine.isMoveValidUnderCheck(selectedTile.getCol(), selectedTile.getRow(), move.endX(), move.endY())) {
                                filteredMoves.add(move);
                            }
                        }
                        for (Move move : filteredMoves) {
                            int endX = move.endX();
                            int endY = move.endY();
                            ChessTile targetTile = tiles[endY][endX];
                            if (targetTile.getPiece() != null && targetTile.getPiece().getColor() != gameEngine.getCurrentPlayerColor()) {
                                targetTile.setEnemyHighlighted(true);
                            } else {
                                targetTile.setHighlighted(true);
                            }
                        }
                        repaint();
                    }
                } else {
                    ChessPiece selectedPiece = selectedTile.getPiece();
                    if (selectedPiece.isValidMove(selectedTile.getCol(), selectedTile.getRow(), col, row, gameEngine.getBoard())) {
                        if (gameEngine.isMoveValidUnderCheck(selectedTile.getCol(), selectedTile.getRow(), col, row)) {
                            gameEngine.makeMove(selectedTile.getCol(), selectedTile.getRow(), col, row);
                            repaint();
                        }
                    }
                    selectedTile.setSelected(false);
                    for (int y = 0; y < BOARD_SIZE; y++) {
                        for (int x = 0; x < BOARD_SIZE; x++) {
                            tiles[y][x].setHighlighted(false);
                            tiles[y][x].setEnemyHighlighted(false);
                        }
                    }
                    selectedTile = null;
                }
            }
        }
    }

    private class ChessMouseMotionListener extends MouseMotionAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            int col = e.getX() / TILE_SIZE;
            int row = e.getY() / TILE_SIZE;

            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                ChessTile tile = tiles[row][col];
                if (tile.getPiece() != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }
}