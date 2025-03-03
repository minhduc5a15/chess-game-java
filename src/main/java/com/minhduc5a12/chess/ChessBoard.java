package com.minhduc5a12.chess;

import com.minhduc5a12.chess.model.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class ChessBoard extends JPanel {
    private static final int BOARD_SIZE = 8;
    private static final int TILE_SIZE = 100;
    private static final Logger logger = LoggerFactory.getLogger(ChessBoard.class);
    private final ChessTile[][] tiles = new ChessTile[BOARD_SIZE][BOARD_SIZE];
    private ChessTile selectedTile = null;
    private final GameEngine gameEngine;
    private BufferedImage chessboardImage;


    public ChessBoard(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        int boardWidth = BOARD_SIZE * TILE_SIZE;
        int boardHeight = BOARD_SIZE * TILE_SIZE;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE, 0, 0));
        loadChessboardImage();
        initializeTiles();
        addMouseListener(new ChessMouseListener());
        addMouseMotionListener(new ChessMouseMotionListener());
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

    private void initializeTiles() {
        ChessTile[][] board = gameEngine.getBoard();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                tiles[row][col] = board[row][col];
                tiles[row][col].setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
                tiles[row][col].setOpaque(false);
                add(tiles[row][col]);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (chessboardImage != null) {
            g.drawImage(chessboardImage, 0, 0, BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE, this);
        }
    }

    private class ChessMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int col = e.getX() / TILE_SIZE;
            int row = e.getY() / TILE_SIZE;

            if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
                return;
            }

            ChessTile clickedTile = tiles[row][col];
            boolean needsRepaint = false;

            if (e.getButton() == MouseEvent.BUTTON1) {
                logger.debug("Left-click detected at ({}, {})", row, col);

                for (int y = 0; y < BOARD_SIZE; y++) {
                    for (int x = 0; x < BOARD_SIZE; x++) {
                        if (tiles[y][x].isRightClickHighlighted()) {
                            tiles[y][x].setRightClickHighlighted(false);
                            needsRepaint = true;
                            logger.debug("Cleared right-click highlight at ({}, {})", y, x);
                        }
                    }
                }

                if (selectedTile == null) {
                    if (clickedTile.getPiece() != null && clickedTile.getPiece().getColor() == gameEngine.getCurrentPlayerColor()) {
                        logger.debug("Selecting piece at ({}, {}) with color {}, current player color: {}", row, col, clickedTile.getPiece().getColor(), gameEngine.getCurrentPlayerColor());
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
                            needsRepaint = true;
                        }
                    }
                } else {
                    ChessPiece selectedPiece = selectedTile.getPiece();
                    if (clickedTile.getPiece() != null && clickedTile.getPiece().getColor() == selectedPiece.getColor()) {
                        if (selectedPiece.getColor() == gameEngine.getCurrentPlayerColor()) {
                            logger.debug("Switching selection to piece at ({}, {}) with same color", row, col);
                            selectedTile.setSelected(false);
                            for (int y = 0; y < BOARD_SIZE; y++) {
                                for (int x = 0; x < BOARD_SIZE; x++) {
                                    if (tiles[y][x].isHighlighted() || tiles[y][x].isEnemyHighlighted()) {
                                        tiles[y][x].setHighlighted(false);
                                        tiles[y][x].setEnemyHighlighted(false);
                                        needsRepaint = true;
                                    }
                                }
                            }
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
                                needsRepaint = true;
                            }
                        } else {
                            logger.debug("Cannot switch selection - not current player's turn");
                        }
                    } else if (selectedPiece.isValidMove(selectedTile.getCol(), selectedTile.getRow(), col, row, gameEngine.getBoard())) {
                        if (selectedPiece.getColor() == gameEngine.getCurrentPlayerColor()) {
                            if (gameEngine.isMoveValidUnderCheck(selectedTile.getCol(), selectedTile.getRow(), col, row)) {
                                logger.debug("Making move from ({}, {}) to ({}, {})", selectedTile.getCol(), selectedTile.getRow(), col, row);
                                gameEngine.makeMove(selectedTile.getCol(), selectedTile.getRow(), col, row);
                                needsRepaint = true;
                            }
                        } else {
                            logger.debug("Cannot make move - not current player's turn");
                        }
                    }

                    if (selectedTile != null) {
                        selectedTile.setSelected(false);
                        needsRepaint = true;
                    }
                    for (int y = 0; y < BOARD_SIZE; y++) {
                        for (int x = 0; x < BOARD_SIZE; x++) {
                            if (tiles[y][x].isHighlighted() || tiles[y][x].isEnemyHighlighted()) {
                                tiles[y][x].setHighlighted(false);
                                tiles[y][x].setEnemyHighlighted(false);
                                needsRepaint = true;
                            }
                        }
                    }
                    selectedTile = null;
                }

                if (needsRepaint) {
                    repaint();
                    logger.debug("Repainted board after left-click changes");
                }
            }
            else if (e.getButton() == MouseEvent.BUTTON3) {
                if (clickedTile.getPiece() != null) {
                    if (!clickedTile.isRightClickHighlighted()) {
                        logger.info("Right-clicked on tile ({}, {}) with piece {}", row, col, clickedTile.getPiece());
                        clickedTile.setRightClickHighlighted(true);
                        logger.debug("Highlighted tile ({}, {}) with right-click", row, col);
                    } else {
                        logger.debug("Tile ({}, {}) already highlighted, skipping", row, col);
                        clickedTile.setRightClickHighlighted(false);
                    }
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

    public static int getBoardWidth() {
        return BOARD_SIZE * TILE_SIZE;
    }

    public static int getBoardHeight() {
        return BOARD_SIZE * TILE_SIZE;
    }
}