package com.minhduc5a12.chess;

import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChessTile extends JPanel {
    private static final int TILE_SIZE = 100;
    private static final int PIECE_SIZE = 95; // Kích thước quân cờ
    private static final int CIRCLE_SIZE = 30; // Kích thước hình tròn

    private ChessPiece piece; // Quân cờ trong ô (có thể là null)
    private final int row; // Hàng của ô cờ
    private final int col; // Cột của ô cờ
    private boolean isEnemyHighlighted = false;
    private boolean isHighlighted = false; // Trạng thái highlight
    private boolean isSelected = false; // Trạng thái được chọn
    private static final Logger logger = LoggerFactory.getLogger(ChessTile.class);

    public ChessTile(int row, int col) {
        this.row = row;
        this.col = col;
        setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
        setOpaque(false); // Đặt ô cờ trong suốt để hiển thị ảnh bàn cờ
    }

    public ChessPiece getPiece() {
        return piece;
    }

    public void setPiece(ChessPiece piece) {
        this.piece = piece;
        repaint(); // Vẽ lại ô cờ khi có thay đổi
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        repaint(); // Vẽ lại ô cờ khi trạng thái chọn thay đổi
    }

    public void setHighlighted(boolean highlighted) {
        this.isHighlighted = highlighted;
        repaint(); // Vẽ lại ô cờ khi trạng thái highlight thay đổi
    }

    public void setEnemyHighlighted(boolean enemyHighlighted) {
        this.isEnemyHighlighted = enemyHighlighted;
        repaint(); // Vẽ lại ô cờ khi trạng thái thay đổi
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        if (isSelected) {
            g2d.setColor(new Color(56, 72, 79, 160));
            g2d.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
        }

        if (piece != null) {
            drawPiece(g);
        }


        if (isHighlighted) {
            g2d.setColor(new Color(0, 0, 0, 100));
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
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            java.net.URL imageUrl = classLoader.getResource(piece.getImagePath());

            if (imageUrl == null) {
                throw new IOException("Cannot find image: " + piece.getImagePath());
            }
            BufferedImage image = ImageIO.read(imageUrl);

            Image resizedImage = image.getScaledInstance(PIECE_SIZE, PIECE_SIZE, Image.SCALE_SMOOTH);

            int offsetX = (TILE_SIZE - PIECE_SIZE) / 2;
            int offsetY = (TILE_SIZE - PIECE_SIZE) / 2;

            // Vẽ hình ảnh quân cờ
            g.drawImage(resizedImage, offsetX, offsetY, null);
        } catch (IOException e) {
            logger.error("Failed to load piece image", e);
        }
    }
}