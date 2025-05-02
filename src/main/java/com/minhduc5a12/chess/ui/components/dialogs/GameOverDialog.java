package com.minhduc5a12.chess.ui.components.dialogs;

import com.minhduc5a12.chess.game.ChessLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;

public class GameOverDialog extends JDialog {
    private static final Logger logger = LoggerFactory.getLogger(GameOverDialog.class);
    private final JFrame parentFrame;

    public GameOverDialog(Frame parent, String message) {
        super(parent, "Kết thúc ván cờ", true);
        this.parentFrame = (JFrame) parent;
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(30, 30, 30));
        setResizable(false);
        setUndecorated(true);
        JPanel roundedPanel = createRoundedPanel();
        roundedPanel.setLayout(new BorderLayout(10, 10));
        roundedPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        messageLabel.setForeground(Color.WHITE);
        roundedPanel.add(messageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        roundedPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(roundedPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(parent);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
    }

    private JPanel createRoundedPanel() {
        return new JPanel() {
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
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton restartButton = createStyledButton("Restart");
        restartButton.addActionListener(this::onRestart);
        buttonPanel.add(restartButton);

        JButton exitButton = createStyledButton("Exit");
        exitButton.addActionListener(this::onExit);
        buttonPanel.add(exitButton);

        return buttonPanel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getModel().isPressed() ? new Color(92, 51, 23) : getModel().isRollover() ? new Color(160, 82, 45) : new Color(139, 69, 19));
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2d.setColor(Color.WHITE);
                FontMetrics metrics = g2d.getFontMetrics();
                int x = (getWidth() - metrics.stringWidth(getText())) / 2;
                int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(120, 40);
            }
        };
        button.setFont(new Font("Georgia", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void onRestart(ActionEvent e) {
        logger.debug("onRestart called");
        dispose();
        if (parentFrame != null) {
            logger.debug("Disposing parentFrame");
            parentFrame.dispose();
        } else {
            logger.warn("parentFrame is null, skipping dispose");
        }
        logger.debug("Calling ChessLauncher.launch()");
        try {
            ChessLauncher.launch();
            logger.info("Game restarted successfully");
        } catch (Exception ex) {
            logger.error("Failed to restart game", ex);
        }
    }

    private void onExit(ActionEvent e) {
        logger.debug("onExit called");
        dispose();
        if (parentFrame != null) {
            logger.debug("Disposing parentFrame");
            parentFrame.dispose();
        } else {
            logger.warn("parentFrame is null, skipping dispose");
        }
        logger.info("Exiting game");
        System.exit(0);
    }
}