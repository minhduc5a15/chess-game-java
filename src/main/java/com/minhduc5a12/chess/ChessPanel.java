package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.utils.ImageLoader;

import javax.swing.*;
import java.awt.*;

public class ChessPanel {
    private static final int BOARD_HEIGHT = 800;
    private static final int PLAYER_PANEL_HEIGHT = 100;
    private static final int FRAME_WIDTH = 800;
    private static final Color DARK_BG = new Color(30, 30, 30);

    private Player blackPlayer;
    private Player whitePlayer;

    public ChessPanel() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chess Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.setBackground(DARK_BG);

            GameEngine gameEngine = new GameEngine(frame, null);
            ChessBoard chessBoard = new ChessBoard(gameEngine);

            gameEngine.setChessBoard(chessBoard);

            blackPlayer = new Player("Naruto", PieceColor.BLACK, gameEngine, "images/avatar1.png");
            whitePlayer = new Player("Sasuke", PieceColor.WHITE, gameEngine, "images/avatar2.png");
            JPanel blackPlayerPanel = blackPlayer.createPanel();
            JPanel whitePlayerPanel = whitePlayer.createPanel();

            // Không gọi whitePlayer.startTimer() ở đây nữa
            gameEngine.setPlayers(whitePlayer, blackPlayer);

            // Bắt đầu timer trắng từ GameEngine
            gameEngine.startInitialTimer();

            frame.add(blackPlayerPanel, BorderLayout.NORTH);
            frame.add(chessBoard, BorderLayout.CENTER);
            frame.add(whitePlayerPanel, BorderLayout.SOUTH);

            frame.pack();
            Insets insets = frame.getInsets();
            int frameHeight = BOARD_HEIGHT + 2 * PLAYER_PANEL_HEIGHT + insets.top + insets.bottom;
            frame.setSize(FRAME_WIDTH, frameHeight);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}