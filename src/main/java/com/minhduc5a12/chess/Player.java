package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.utils.ImageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class Player {
    private static final int BOARD_WIDTH = 800;
    private static final int PLAYER_PANEL_HEIGHT = 100;
    private static final int AVATAR_SIZE = 50;
    private static final int TIME_PANEL_WIDTH = 150;
    private static final int TIME_PANEL_HEIGHT = 50;
    private static final Color DARK_BG = new Color(30, 30, 30);
    private static final Color TIME_BG = new Color(66, 66, 66);
    private static final Logger logger = LoggerFactory.getLogger(Player.class);

    private final String name;
    private final PieceColor color;
    private int timeSeconds = 600;
    private JLabel timeLabel;
    private final GameEngine gameEngine;
    private Timer timer;
    private final String avatarPath;

    public Player(String name, PieceColor color, GameEngine gameEngine, String avatarPath) {
        this.name = name;
        this.color = color;
        this.gameEngine = gameEngine;
        this.avatarPath = avatarPath;
        initializeLabels();
        gameEngine.addTurnChangeListener(this::onTurnChange);
    }

    private void initializeLabels() {
        timeLabel = new JLabel(formatTime());
        int FONT_SIZE = 20;
        timeLabel.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
    }

    private String formatTime() {
        return String.format("%d:%02d", timeSeconds / 60, timeSeconds % 60);
    }

    private void updateTimeLabel() {
        timeLabel.setText(formatTime());
        timeLabel.repaint();
    }

    void startTimer() {
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(1000, e -> {
            if (color == gameEngine.getCurrentPlayerColor()) {
                if (--timeSeconds <= 0) {
                    timer.stop();
                    JOptionPane.showMessageDialog(null, name + " hết giờ! " + (color == PieceColor.WHITE ? "Đen" : "Trắng") + " thắng!", "Hết giờ", JOptionPane.INFORMATION_MESSAGE);
                }
                updateTimeLabel();
            }
        });
        timer.start();
        logger.info("Timer started for {}", name);
    }

    public void pauseTimer() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
            logger.info("Timer paused for {}", name);
        }
    }

    public void resumeTimer() {
        if (timer != null && !timer.isRunning()) {
            timer.start();
            logger.info("Timer resumed for {}", name);
        }
    }

    private void onTurnChange(PieceColor newTurn) {
        if (color == newTurn) {
            startTimer();
            timeLabel.setForeground(Color.WHITE);
            logger.info("Turn switched to {} - Timer started", name);
        } else {
            pauseTimer();
            timeLabel.setForeground(Color.BLACK);
            logger.info("Turn switched away from {} - Timer paused", name);
        }
        timeLabel.repaint();
    }

    public JPanel createPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(DARK_BG);
        panel.setPreferredSize(new Dimension(BOARD_WIDTH, PLAYER_PANEL_HEIGHT));

        panel.add(createLeftSection(), BorderLayout.WEST);
        panel.add(createRightSection(), BorderLayout.EAST);

        timeLabel.setForeground(color == gameEngine.getCurrentPlayerColor() ? Color.WHITE : Color.BLACK);

        return panel;
    }

    private JPanel createLeftSection() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(BOARD_WIDTH / 2, PLAYER_PANEL_HEIGHT));
        left.setBackground(DARK_BG);

        left.add(Box.createVerticalGlue());

        JPanel avatarName = new JPanel(new BorderLayout());
        avatarName.setPreferredSize(new Dimension(BOARD_WIDTH / 2, AVATAR_SIZE));
        avatarName.setBackground(DARK_BG);
        avatarName.add(createAvatarPanel(), BorderLayout.WEST);
        avatarName.add(createNamePanel(), BorderLayout.CENTER);
        left.add(avatarName);

        left.add(Box.createVerticalGlue());

        return left;
    }

    private JPanel createAvatarPanel() {
        JPanel avatarPanel = new JPanel();
        avatarPanel.setPreferredSize(new Dimension(BOARD_WIDTH / 8, AVATAR_SIZE));
        avatarPanel.setBackground(DARK_BG);
        ImageIcon avatarIcon = new ImageIcon(ImageLoader.getImage(avatarPath).getScaledInstance(AVATAR_SIZE, AVATAR_SIZE, Image.SCALE_SMOOTH));
        avatarPanel.add(new JLabel(avatarIcon));
        return avatarPanel;
    }

    private JPanel createNamePanel() {
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.setPreferredSize(new Dimension((BOARD_WIDTH / 2) - (BOARD_WIDTH / 8), AVATAR_SIZE));
        namePanel.setBackground(DARK_BG);
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        nameLabel.setForeground(Color.WHITE);
        namePanel.add(nameLabel);
        return namePanel;
    }

    private JPanel createRightSection() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(BOARD_WIDTH / 2, PLAYER_PANEL_HEIGHT));
        right.setBackground(DARK_BG);

        right.add(Box.createVerticalGlue());

        JPanel timePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TIME_BG);
                g2d.fillRoundRect(0, 0, TIME_PANEL_WIDTH, TIME_PANEL_HEIGHT, 15, 15);
            }
        };
        timePanel.setPreferredSize(new Dimension(TIME_PANEL_WIDTH, TIME_PANEL_HEIGHT));
        timePanel.setOpaque(false);
        timePanel.setLayout(new GridBagLayout());
        timePanel.add(timeLabel, new GridBagConstraints());

        JPanel timeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        timeWrapper.setPreferredSize(new Dimension(BOARD_WIDTH / 2, TIME_PANEL_HEIGHT));
        timeWrapper.setBackground(DARK_BG);
        timeWrapper.add(timePanel);

        right.add(timeWrapper);
        right.add(Box.createVerticalGlue());

        return right;
    }

    public PieceColor getColor() {
        return color;
    }
}