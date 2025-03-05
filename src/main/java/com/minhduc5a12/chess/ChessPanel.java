package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;
import javax.swing.*;
import java.awt.*;

public class ChessPanel {
    private static final int BOARD_WIDTH = 800;
    private static final int BOARD_HEIGHT = 800;
    private static final int PLAYER_PANEL_HEIGHT = 100;
    private static final int FRAME_WIDTH = 800;
    private static final Color DARK_BG = new Color(30, 30, 30); // #1E1E1E

    public ChessPanel() {
    SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("Chess Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setBackground(DARK_BG);

        GameEngine gameEngine = new GameEngine();
        ChessBoard chessBoard = new ChessBoard(gameEngine);

        Player blackPlayer = new Player("Naruto", PieceColor.BLACK, gameEngine, "images/avatar1.png");
        Player whitePlayer = new Player("Sasuke", PieceColor.WHITE, gameEngine, "images/avatar2.png");
        JPanel whitePanel = blackPlayer.createPanel();
        JPanel blackPanel = whitePlayer.createPanel();

        whitePlayer.startTimer();

        frame.add(whitePanel, BorderLayout.NORTH);
        frame.add(chessBoard, BorderLayout.CENTER);
        frame.add(blackPanel, BorderLayout.SOUTH);

        frame.pack();
        Insets insets = frame.getInsets();
        int frameHeight = BOARD_HEIGHT + 2 * PLAYER_PANEL_HEIGHT + insets.top + insets.bottom;
        frame.setSize(FRAME_WIDTH, frameHeight);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    });
}

    private JPanel createPlayerPanel(String name, PieceColor color, GameEngine gameEngine, String avatarPath) {
        Player player = new Player(name, color, gameEngine, avatarPath);
        JPanel panel = player.createPanel();
        panel.setPreferredSize(new Dimension(BOARD_WIDTH, PLAYER_PANEL_HEIGHT));
        panel.setBackground(ChessPanel.DARK_BG);
        return panel;
    }
}