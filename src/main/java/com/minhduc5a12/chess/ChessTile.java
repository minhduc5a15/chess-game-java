package com.minhduc5a12.chess;

import com.minhduc5a12.chess.pieces.ChessPiece;

import javax.swing.*;
import java.awt.*;

public class ChessTile extends JPanel {
    private static final int TILE_SIZE = 100;
    private static final int PIECE_SIZE = 95;
    private static final int CIRCLE_SIZE = 30;

    private ChessPiece piece;
    private final int row;
    private final int col;
    private boolean isEnemyHighlighted = false;
    private boolean isHighlighted = false;
    private boolean isSelected = false;
    private boolean isStockfishSuggested = false;

    public ChessTile(int row, int col) {
        this.row = row;
        this.col = col;
        setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
        setOpaque(false);
    }

    public void setStockfishSuggested(boolean suggested) {
        this.isStockfishSuggested = suggested;
        repaint();
    }

    public ChessPiece getPiece() {
        return piece;
    }

    public void setPiece(ChessPiece piece) {
        this.piece = piece;
        repaint();
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        repaint();
    }

    public void setHighlighted(boolean highlighted) {
        this.isHighlighted = highlighted;
        repaint();
    }

    public void setEnemyHighlighted(boolean enemyHighlighted) {
        this.isEnemyHighlighted = enemyHighlighted;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (isSelected) {
            g2d.setColor(new Color(56, 72, 79, 160));
            g2d.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
        }

        if (isStockfishSuggested) { // Tô toàn bộ ô màu xanh nhạt
            g2d.setColor(new Color(173, 216, 230, 100)); // Light Blue với độ trong suốt
            g2d.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
        }

        if (piece != null) {
            drawPiece(g);
        }

        if (isHighlighted) {
            g2d.setColor(new Color(255, 255, 255, 100));
            int circleX = (TILE_SIZE - CIRCLE_SIZE) / 2;
            int circleY = (TILE_SIZE - CIRCLE_SIZE) / 2;
            g2d.fillOval(circleX, circleY, CIRCLE_SIZE, CIRCLE_SIZE);
        } else if (isEnemyHighlighted) {
            g2d.setColor(new Color(255, 0, 0, 150));
            g2d.setStroke(new BasicStroke(7));
            int circleX = (TILE_SIZE - 90) / 2;
            int circleY = (TILE_SIZE - 90) / 2;
            g2d.drawOval(circleX, circleY, 90, 90);
            g2d.setStroke(new BasicStroke(1));
        }
    }

    private void drawPiece(Graphics g) {
        Image image = piece.getImage();
        if (image != null) {
            int offsetX = (TILE_SIZE - PIECE_SIZE) / 2;
            int offsetY = (TILE_SIZE - PIECE_SIZE) / 2;
            g.drawImage(image, offsetX, offsetY, null);
        }
    }
}