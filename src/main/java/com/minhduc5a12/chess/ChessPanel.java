package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;

import javax.swing.*;
import java.awt.*;

public class ChessPanel {
    public ChessPanel() {
        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("Chess Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GameEngine gameEngine = new GameEngine();
            ChessBoard chessBoard = new ChessBoard(gameEngine);

            JLabel turnLabel = new JLabel("Lượt đi: Trắng", SwingConstants.CENTER);
            turnLabel.setFont(new Font("Arial", Font.BOLD, 16));

            // Thiết lập callback để cập nhật lượt chơi
            gameEngine.setOnTurnChange(newTurn -> {
                turnLabel.setText("Lượt đi: " + (newTurn == PieceColor.WHITE ? "Trắng" : "Đen"));
            });

            frame.add(chessBoard, BorderLayout.CENTER);
            frame.add(turnLabel, BorderLayout.SOUTH);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}