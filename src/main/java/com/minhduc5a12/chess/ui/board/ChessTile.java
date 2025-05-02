package com.minhduc5a12.chess.ui.board;

import com.minhduc5a12.chess.constants.GameConstants;
import com.minhduc5a12.chess.constants.GameMode;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;
import com.minhduc5a12.chess.game.ChessController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class ChessTile extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ChessTile.class);
    private static final int CIRCLE_SIZE = 30;

    private final ChessPosition position;
    private ChessPiece piece;
    private final int tileSize;
    private final int pieceSize;
    private boolean isLeftClickSelected;
    private boolean isValidMove;
    private boolean isLastMove;
    private final ChessController chessController;

    public ChessTile(ChessPosition position, int tileSize, ChessController chessController) {
        this.position = position;
        this.tileSize = tileSize;
        this.pieceSize = 95;
        this.isLeftClickSelected = this.isValidMove = this.isLastMove = false;
        this.chessController = chessController;
        setPreferredSize(new Dimension(tileSize, tileSize));
        setOpaque(false);
        addMouseListener(new ChessTileMouseListener());
        addMouseMotionListener(new ChessTileMouseMotionListener());
    }

    public ChessTile(ChessPosition position, ChessController chessController) {
        this(position, GameConstants.Board.SQUARE_SIZE, chessController);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (isLeftClickSelected) {
            g2d.setColor(new Color(56, 72, 79, 160));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        if (isLastMove) {
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.fillRect(0, 0, GameConstants.Board.SQUARE_SIZE, GameConstants.Board.SQUARE_SIZE);
            g2d.setColor(chessController.getCurrentBoardState().getCurrentPlayerColor().isBlack() ? new Color(0, 211, 255) : new Color(255, 24, 62));
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRect(0, 0, GameConstants.Board.SQUARE_SIZE, GameConstants.Board.SQUARE_SIZE);
        }

        if (piece != null) {
            drawPiece(g);
        }
        if (isValidMove) {
            if (piece != null) {
                g2d.setColor(new Color(222, 47, 31, 150));
                g2d.setStroke(new BasicStroke(4));
                int circleX = (GameConstants.Board.SQUARE_SIZE - 90) / 2;
                int circleY = (GameConstants.Board.SQUARE_SIZE - 90) / 2;
                g2d.drawOval(circleX, circleY, 90, 90);
                g2d.setStroke(new BasicStroke(1));
            } else {
                g2d.setColor(new Color(255, 255, 255, 100));
                int circleX = (GameConstants.Board.SQUARE_SIZE - CIRCLE_SIZE) / 2;
                int circleY = (GameConstants.Board.SQUARE_SIZE - CIRCLE_SIZE) / 2;
                g2d.fillOval(circleX, circleY, CIRCLE_SIZE, CIRCLE_SIZE);
            }
        }
    }

    private void drawPiece(Graphics g) {
        Image image = piece.getImage();
        if (image != null) {
            int offsetX = (tileSize - pieceSize) / 2;
            int offsetY = (tileSize - pieceSize) / 2;
            g.drawImage(image, offsetX, offsetY, pieceSize, pieceSize, null);
        } else {
            logger.warn("No image for piece at position: {}", position.toChessNotation());
        }
    }

    public ChessPosition getPosition() {
        return position;
    }

    public ChessPiece getPiece() {
        return piece;
    }

    public void setPiece(ChessPiece piece) {
        this.piece = piece;
        repaint();
    }

    public void setLeftClickSelected(boolean leftClickSelected) {
        this.isLeftClickSelected = leftClickSelected;
        repaint();
    }

    public void setValidMove(boolean validMove) {
        this.isValidMove = validMove;
        repaint();
    }

    public int getCol() {
        return position.col();
    }

    public int getRow() {
        return position.row();
    }

    public void setLastMove(boolean lastMove) {
        isLastMove = lastMove;
        repaint();
    }

    private class ChessTileMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (chessController.isGameEnded() || chessController.getGameMode() == GameMode.AI_VS_AI) {
                return;
            }

            if (chessController.getGameMode() == GameMode.PLAYER_VS_AI) {
                if (chessController.getCurrentPlayerColor() != chessController.getHumanPlayerColor()) {
                    return;
                }
            }

            if (e.getButton() == MouseEvent.BUTTON1) {
                ChessTile selectedTile = chessController.getCurrentLeftClickedTile();
                if (selectedTile == null) {
                    if (piece != null && piece.getColor() == chessController.getCurrentPlayerColor()) {
                        chessController.setCurrentLeftClickedTile(ChessTile.this);
                        logger.debug("Selected piece at: {}", position.toChessNotation());
                    }
                } else {
                    if (piece != null && piece.getColor() == chessController.getCurrentPlayerColor()) {
                        if (selectedTile == ChessTile.this) {
                            chessController.setCurrentLeftClickedTile(null);
                            logger.debug("Deselected piece at: {}", position.toChessNotation());
                            return;
                        }
                        chessController.setCurrentLeftClickedTile(ChessTile.this);
                        logger.debug("Selected piece at: {}", position.toChessNotation());
                        return;
                    }
                    ChessMove move = new ChessMove(selectedTile.getPosition(), position);
                    if (chessController.movePiece(move)) {
                        logger.debug("Moved piece from {} to {}", move.start().toChessNotation(), move.end().toChessNotation());
                        chessController.setCurrentLeftClickedTile(null);
                    }
                }
            }
        }
    }

    private class ChessTileMouseMotionListener extends MouseMotionAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            if (piece != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}