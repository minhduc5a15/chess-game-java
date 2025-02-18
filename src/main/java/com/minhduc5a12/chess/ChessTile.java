package com.minhduc5a12.chess;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ChessTile extends JPanel {
    private static final int TILE_SIZE = 100;
    private static final int PIECE_SIZE = 95; // Kích thước quân cờ

    private ChessPiece piece; // Quân cờ trong ô (có thể là null)
    private final Color originalColor; // Màu gốc của ô cờ
    private final int row; // Hàng của ô cờ
    private final int col; // Cột của ô cờ

    public ChessTile(Color tileColor, int row, int col) {
        this.originalColor = tileColor; // Lưu màu gốc
        this.row = row;
        this.col = col;
        setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
        setBackground(tileColor); // Đặt màu nền ban đầu
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setPiece(ChessPiece piece) {
        this.piece = piece;
        repaint(); // Vẽ lại ô cờ khi có thay đổi
    }

    public ChessPiece getPiece() {
        return piece;
    }

    public void setSelected(boolean selected) {
        if (selected) {
            setBackground(new Color(0xabd73d)); // Màu vàng để đánh dấu ô được chọn
        } else {
            setBackground(originalColor); // Trả lại màu gốc khi không được chọn
        }
        repaint(); // Vẽ lại ô cờ
    }

    public Color getOriginalColor() {
        return originalColor;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Vẽ quân cờ nếu có
        if (piece != null) {
            drawPiece(g);
        }
    }

    private void drawPiece(Graphics g) {
        try {
            // Sử dụng ClassLoader để tải hình ảnh từ thư mục resources
            ClassLoader classLoader = getClass().getClassLoader();
            java.net.URL imageUrl = classLoader.getResource(piece.getImagePath());

            if (imageUrl == null) {
                throw new IOException("Cannot find image: " + piece.getImagePath());
            }

            // Tải hình ảnh từ URL
            BufferedImage image = ImageIO.read(imageUrl);

            // Thay đổi kích thước hình ảnh
            Image resizedImage = image.getScaledInstance(PIECE_SIZE, PIECE_SIZE, Image.SCALE_SMOOTH);

            // Tính toán vị trí để đặt quân cờ vào giữa ô
            int offsetX = (TILE_SIZE - PIECE_SIZE) / 2;
            int offsetY = (TILE_SIZE - PIECE_SIZE) / 2;

            // Vẽ hình ảnh quân cờ
            g.drawImage(resizedImage, offsetX, offsetY, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}