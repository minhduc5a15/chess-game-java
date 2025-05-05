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

/**
 * Represents a single tile on the chessboard, handling piece rendering, user interactions,
 * and visual states such as selection, valid moves, and highlights.
 */
public class ChessTile extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(ChessTile.class);
    private static final int CIRCLE_SIZE = 30;
    private static final int PIECE_SIZE = 95;

    private final ChessPosition position;
    private final int tileSize;
    private final ChessController chessController;
    private ChessPiece piece;
    private boolean isLeftClickSelected;
    private boolean isInValidMove;
    private boolean isInLastMove;
    private boolean isHintHighlightedSquare;

    /**
     * Constructs a ChessTile with the specified position, tile size, and controller.
     *
     * @param position        The position of the tile on the chessboard.
     * @param tileSize        The size of the tile in pixels.
     * @param chessController The controller managing the chess game logic.
     */
    public ChessTile(ChessPosition position, int tileSize, ChessController chessController) {
        this.position = position;
        this.tileSize = tileSize;
        this.chessController = chessController;
        this.piece = null;
        this.isLeftClickSelected = false;
        this.isInValidMove = false;
        this.isInLastMove = false;
        this.isHintHighlightedSquare = false;

        initializeTile();
    }

    /**
     * Constructs a ChessTile with the default tile size.
     *
     * @param position        The position of the tile on the chessboard.
     * @param chessController The controller managing the chess game logic.
     */
    public ChessTile(ChessPosition position, ChessController chessController) {
        this(position, GameConstants.Board.SQUARE_SIZE, chessController);
    }

    /**
     * Initializes the tile's properties and event listeners.
     */
    private void initializeTile() {
        setPreferredSize(new Dimension(tileSize, tileSize));
        setOpaque(false);
        addMouseListener(new ChessTileMouseListener());
        addMouseMotionListener(new ChessTileMouseMotionListener());
    }

    /**
     * Paints the tile, including selection highlights, last move indicators,
     * valid move markers, and the chess piece if present.
     *
     * @param g The Graphics object to paint on.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        drawSelectionHighlight(g2d);
        drawLastMoveIndicator(g2d);
        drawPiece(g);
        drawValidMoveIndicator(g2d);
        drawHintHighlight(g2d);
    }

    /**
     * Draws the highlight for a selected tile.
     *
     * @param g2d The Graphics2D object for drawing.
     */
    private void drawSelectionHighlight(Graphics2D g2d) {
        if (isLeftClickSelected) {
            g2d.setColor(new Color(56, 72, 79, 160));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /**
     * Draws the indicator for the last move made.
     *
     * @param g2d The Graphics2D object for drawing.
     */
    private void drawLastMoveIndicator(Graphics2D g2d) {
        if (isInLastMove) {
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.fillRect(0, 0, tileSize, tileSize);
            g2d.setColor(chessController.getBoardManager().getCurrentBoardState().getCurrentPlayerColor().isBlack() ? new Color(0, 211, 255) : new Color(255, 24, 62));
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRect(0, 0, tileSize, tileSize);
        }
    }

    /**
     * Draws the chess piece on the tile if present.
     *
     * @param g The Graphics object for drawing.
     */
    private void drawPiece(Graphics g) {
        if (piece == null) {
            return;
        }

        Image image = piece.getImage();
        if (image != null) {
            int offset = (tileSize - PIECE_SIZE) / 2;
            g.drawImage(image, offset, offset, PIECE_SIZE, PIECE_SIZE, null);
        } else {
            logger.warn("No image for piece at position: {}", position.toChessNotation());
        }
    }

    /**
     * Draws the indicator for a valid move.
     *
     * @param g2d The Graphics2D object for drawing.
     */
    private void drawValidMoveIndicator(Graphics2D g2d) {
        if (!isInValidMove) {
            return;
        }

        if (piece != null) {
            g2d.setColor(new Color(222, 47, 31, 150));
            g2d.setStroke(new BasicStroke(4));
            int circleSize = 90;
            int circleX = (tileSize - circleSize) / 2;
            int circleY = (tileSize - circleSize) / 2;
            g2d.drawOval(circleX, circleY, circleSize, circleSize);
            g2d.setStroke(new BasicStroke(1));
        } else {
            g2d.setColor(new Color(255, 255, 255, 100));
            int circleX = (tileSize - CIRCLE_SIZE) / 2;
            int circleY = (tileSize - CIRCLE_SIZE) / 2;
            g2d.fillOval(circleX, circleY, CIRCLE_SIZE, CIRCLE_SIZE);
        }
    }

    /**
     * Draws the hint highlight for the tile.
     *
     * @param g2d The Graphics2D object for drawing.
     */
    private void drawHintHighlight(Graphics2D g2d) {
        if (isHintHighlightedSquare) {
            ChessPiece piece = getPiece();
            g2d.setColor(new Color(0, 196, 255, piece != null && piece.getColor() != chessController.getBoardManager().getCurrentPlayerColor() ? 100 : 50));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public ChessPosition getPosition() {
        return position;
    }

    public ChessPiece getPiece() {
        return piece;
    }

    public int getCol() {
        return position.col();
    }

    public int getRow() {
        return position.row();
    }

    public boolean isInLastMove() {
        return isInLastMove;
    }

    public boolean isHintHighlightedSquare() {
        return isHintHighlightedSquare;
    }

    public void setPiece(ChessPiece piece) {
        this.piece = piece;
        repaint();
    }

    public void setLeftClickSelected(boolean leftClickSelected) {
        this.isLeftClickSelected = leftClickSelected;
        repaint();
    }

    public void setInValidMove(boolean inValidMove) {
        this.isInValidMove = inValidMove;
        repaint();
    }

    public void setHintHighlightedSquare(boolean hintHighlightedSquare) {
        this.isHintHighlightedSquare = hintHighlightedSquare;
        repaint();
    }

    public void setInLastMove(boolean inLastMove) {
        this.isInLastMove = inLastMove;
        repaint();
    }

    /**
     * Handles mouse click events for selecting pieces and making moves.
     */
    private class ChessTileMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (isInteractionBlocked()) {
                return;
            }

            if (e.getButton() == MouseEvent.BUTTON1) {
                handleLeftClick();
            }
        }

        /**
         * Checks if user interaction should be blocked based on game state or mode.
         *
         * @return True if interaction is blocked, false otherwise.
         */
        private boolean isInteractionBlocked() {
            if (chessController.isGameEnded() || chessController.getGameMode() == GameMode.AI_VS_AI) {
                return true;
            }

            return chessController.getGameMode() == GameMode.PLAYER_VS_AI && chessController.getBoardManager().getCurrentPlayerColor() != chessController.getHumanPlayerColor();
        }

        /**
         * Handles left-click events for piece selection and movement.
         */
        private void handleLeftClick() {
            ChessTile selectedTile = chessController.getBoardUI().getCurrentLeftClickedTile();

            if (selectedTile == null) {
                selectPiece();
            } else {
                handleMoveOrReselect(selectedTile);
            }
        }

        /**
         * Selects a piece if it belongs to the current player.
         */
        private void selectPiece() {
            if (piece != null && piece.getColor() == chessController.getBoardManager().getCurrentPlayerColor()) {
                chessController.getBoardUI().setCurrentLeftClickedTile(ChessTile.this);
                logger.debug("Selected piece at: {}", position.toChessNotation());
            }
        }

        /**
         * Handles move execution or reselection of a piece.
         *
         * @param selectedTile The currently selected tile.
         */
        private void handleMoveOrReselect(ChessTile selectedTile) {
            if (piece != null && piece.getColor() == chessController.getBoardManager().getCurrentPlayerColor()) {
                if (selectedTile == ChessTile.this) {
                    chessController.getBoardUI().setCurrentLeftClickedTile(null);
                    logger.debug("Deselected piece at: {}", position.toChessNotation());
                } else {
                    chessController.getBoardUI().setCurrentLeftClickedTile(ChessTile.this);
                    logger.debug("Selected piece at: {}", position.toChessNotation());
                }
            } else {
                executeMove(selectedTile);
            }
        }

        /**
         * Executes a move if valid and clears the selection.
         *
         * @param selectedTile The source tile of the move.
         */
        private void executeMove(ChessTile selectedTile) {
            ChessMove move = new ChessMove(selectedTile.getPosition(), position);
            if (chessController.movePiece(move)) {
                logger.debug("Moved piece from {} to {}", move.start().toChessNotation(), move.end().toChessNotation());
                chessController.getBoardUI().setCurrentLeftClickedTile(null);
            }
        }
    }

    /**
     * Handles mouse motion events to update the cursor based on tile content.
     */
    private class ChessTileMouseMotionListener extends MouseMotionAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            setCursor(piece != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}