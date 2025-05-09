package com.minhduc5a12.chess.ui;

import com.minhduc5a12.chess.constants.GameMode;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.game.ChessBoard;
import com.minhduc5a12.chess.game.ChessController;
import com.minhduc5a12.chess.ui.components.panels.ChessToolbar;
import com.minhduc5a12.chess.ui.components.panels.MoveHistoryPanel;
import com.minhduc5a12.chess.ui.components.panels.PlayerPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Manages the user interface for the chess game.
 */
public class ChessUI {
    private final ChessController chessController;
    private final JFrame frame;
    private static final Logger logger = LoggerFactory.getLogger(ChessUI.class);

    /**
     * Constructs a ChessUI with the specified game mode and player color.
     *
     * @param gameMode      the game mode (e.g., PLAYER_VS_PLAYER, PLAYER_VS_AI, AI_VS_AI)
     * @param selectedColor the color chosen for the human player in PLAYER_VS_AI mode
     */
    public ChessUI(int gameMode, PieceColor selectedColor) {
        this.chessController = new ChessController();
        configureGame(this.chessController, gameMode, selectedColor);
        this.frame = new JFrame("Chess Game");
        chessController.setFrame(frame);
        setupUI();
    }

    private void setupUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 30));

        ChessBoard chessBoard = new ChessBoard(chessController);
        chessController.setChessBoard(chessBoard); // Đặt chessBoard vào ChessController
        mainPanel.add(chessBoard, BorderLayout.CENTER);

        logger.info("Game mode selected {}", chessController.getGameMode());

        PlayerPanel whitePlayerPanel;
        PlayerPanel blackPlayerPanel;
        switch (chessController.getGameMode()) {
            case GameMode.AI_VS_AI -> {
                whitePlayerPanel = new PlayerPanel("AI", PieceColor.WHITE, "images/stockfish.png", chessController);
                mainPanel.add(whitePlayerPanel, BorderLayout.WEST);
                blackPlayerPanel = new PlayerPanel("AI", PieceColor.BLACK, "images/stockfish.png", chessController);
                mainPanel.add(blackPlayerPanel, BorderLayout.EAST);
            }
            case GameMode.PLAYER_VS_AI -> {
                PieceColor playerColor = chessController.getHumanPlayerColor();
                whitePlayerPanel = new PlayerPanel("You", playerColor, "images/player.png", chessController);
                mainPanel.add(whitePlayerPanel, playerColor.isWhite() ? BorderLayout.WEST : BorderLayout.EAST);
                blackPlayerPanel = new PlayerPanel("AI", playerColor.isWhite() ? PieceColor.BLACK : PieceColor.WHITE, "images/stockfish.png", chessController);
                mainPanel.add(blackPlayerPanel, playerColor.isWhite() ? BorderLayout.EAST : BorderLayout.WEST);
            }
            case GameMode.PLAYER_VS_PLAYER -> {
                whitePlayerPanel = new PlayerPanel("Player 1", PieceColor.WHITE, "images/player.png", chessController);
                mainPanel.add(whitePlayerPanel, BorderLayout.WEST);
                blackPlayerPanel = new PlayerPanel("Player 2", PieceColor.BLACK, "images/player.png", chessController);
                mainPanel.add(blackPlayerPanel, BorderLayout.EAST);
            }
            default -> {
                logger.info("No game mode were selected!");
                return;
            }
        }

        MoveHistoryPanel moveHistoryPanel = new MoveHistoryPanel(chessController);
        chessController.addHistoryChangeListener(moveHistoryPanel);
        mainPanel.add(moveHistoryPanel, BorderLayout.NORTH);

        ChessToolbar toolbar = new ChessToolbar(chessController, chessController.getBoardUI());
        mainPanel.add(toolbar, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                chessController.shutdown();
            }
        });
    }

    /**
     * Displays the chess game UI.
     */
    public void show() {
        frame.setVisible(true);
    }

    private void configureGame(ChessController controller, int mode, PieceColor playerColor) {
        switch (mode) {
            case GameMode.PLAYER_VS_PLAYER -> controller.setPlayerVsPlayer();
            case GameMode.PLAYER_VS_AI -> controller.setPlayerVsAI(playerColor);
            case GameMode.AI_VS_AI -> controller.setAIVsAI();
        }
    }
}