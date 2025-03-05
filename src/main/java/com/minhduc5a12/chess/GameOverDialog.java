package com.minhduc5a12.chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

public class GameOverDialog extends JDialog {
    private final GameEngine gameEngine;
    private final Consumer<Void> restartCallback;

    public GameOverDialog(Frame parent, String message, GameEngine gameEngine, Consumer<Void> restartCallback) {
        super(parent, "Kết thúc ván cờ", true);
        this.gameEngine = gameEngine;
        this.restartCallback = restartCallback;

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(30, 30, 30)); // Dark background
        setResizable(false);
        setUndecorated(true);

        JPanel roundedPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, new Color(40, 40, 40), getWidth(), getHeight(), new Color(20, 20, 20));
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

                g2d.setColor(new Color(80, 80, 80));
                g2d.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));

                g2d.dispose();
            }
        };
        roundedPanel.setLayout(new BorderLayout(10, 10));
        roundedPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        messageLabel.setForeground(Color.WHITE);
        roundedPanel.add(messageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton restartButton = createStyledButton("Chơi lại");
        restartButton.addActionListener(this::onRestart);
        buttonPanel.add(restartButton);

        JButton exitButton = createStyledButton("Thoát");
        exitButton.addActionListener(this::onExit);
        buttonPanel.add(exitButton);

        roundedPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(roundedPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(parent);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20)); // Rounded corners
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(new Color(50, 50, 50));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(70, 70, 70));
                } else {
                    g2d.setColor(new Color(60, 60, 60));
                }
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));

                g2d.setColor(Color.WHITE);
                FontMetrics metrics = g2d.getFontMetrics();
                int x = (getWidth() - metrics.stringWidth(getText())) / 2;
                int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(120, 40);
            }
        };
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void onRestart(ActionEvent e) {
        restartCallback.accept(null);
        dispose();
    }

    private void onExit(ActionEvent e) {
        System.exit(0);
    }
}