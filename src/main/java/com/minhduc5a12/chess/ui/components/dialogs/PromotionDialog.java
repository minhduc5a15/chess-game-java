package com.minhduc5a12.chess.ui.components.dialogs;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.utils.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class PromotionDialog extends JDialog {
    private String selectedPiece = "Queen";

    public PromotionDialog(Frame parent, PieceColor color) {
        super(parent, "Pawn Promotion", true);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(30, 30, 30));
        setResizable(false);
        setUndecorated(true);

        JPanel roundedPanel = getJPanel();

        JLabel messageLabel = new JLabel("Promote pawn to:", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        messageLabel.setForeground(Color.WHITE);
        roundedPanel.add(messageLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton queenButton = createStyledButton("images/pieces/" + (color.isWhite() ? "white_queen.png" : "black_queen.png"));
        queenButton.addActionListener(e -> onPieceSelected("Queen"));
        buttonPanel.add(queenButton);

        JButton rookButton = createStyledButton("images/pieces/" + (color.isWhite() ? "white_rook.png" : "black_rook.png"));
        rookButton.addActionListener(e -> onPieceSelected("Rook"));
        buttonPanel.add(rookButton);

        JButton bishopButton = createStyledButton("images/pieces/" + (color.isWhite() ? "white_bishop.png" : "black_bishop.png"));
        bishopButton.addActionListener(e -> onPieceSelected("Bishop"));
        buttonPanel.add(bishopButton);

        JButton knightButton = createStyledButton("images/pieces/" + (color.isWhite() ? "white_knight.png" : "black_knight.png"));
        knightButton.addActionListener(e -> onPieceSelected("Knight"));
        buttonPanel.add(knightButton);

        roundedPanel.add(buttonPanel, BorderLayout.CENTER);
        add(roundedPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(parent);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
    }

    private static JPanel getJPanel() {
        JPanel roundedPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(139, 69, 19), getWidth(), getHeight(), new Color(92, 51, 23));
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g2d.setColor(new Color(80, 40, 20));
                g2d.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
                g2d.dispose();
            }
        };
        roundedPanel.setLayout(new BorderLayout(10, 10));
        roundedPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return roundedPanel;
    }

    private JButton createStyledButton(String iconPath) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getModel().isPressed() ? new Color(92, 51, 23) : getModel().isRollover() ? new Color(160, 82, 45) : new Color(139, 69, 19)); // Màu gỗ mặc định
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2d.dispose();
                super.paintComponent(g);
            }
        };

        button.setIcon(new ImageIcon(ImageLoader.getImage(iconPath, 95, 95)));
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void onPieceSelected(String piece) {
        this.selectedPiece = piece;
        dispose();
    }

    public String getSelectedPiece() {
        return selectedPiece;
    }
}