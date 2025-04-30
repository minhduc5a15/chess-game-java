package com.minhduc5a12.chess.ui;

import com.minhduc5a12.chess.ChessBoard;
import com.minhduc5a12.chess.ChessController;
import com.minhduc5a12.chess.constants.GameMode;
import com.minhduc5a12.chess.utils.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ChessToolbar extends JPanel {
    private final ChessController chessController;
    private final ChessBoard chessBoard;
    private static final int ICON_SIZE = 24;
    private static final int TOOLBAR_HEIGHT = 50;

    public ChessToolbar(ChessController controller, ChessBoard chessBoard) {
        this.chessController = controller;
        this.chessBoard = chessBoard;
        setBackground(new Color(40, 40, 40));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        setLayout(new GridLayout(1, 0, 10, 0));
        setPreferredSize(new Dimension(0, TOOLBAR_HEIGHT));

        initializeButtons();
    }

    private void initializeButtons() {
        add(createButton("Flip Board", "images/flip-board.png", e -> {
            chessBoard.flipBoard();
        }));

        if (chessController.getGameMode() != GameMode.AI_VS_AI) {
            add(createButton("Resign", "images/resign.png", e -> {
                ResignDialog dialog = new ResignDialog((Frame) SwingUtilities.getWindowAncestor(this), "Are you sure you want to resign?");
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    chessController.resignGame( );
                }
            }));
        }

        add(createButton("Move Back", "images/back.png", e -> {
//            chessController.undoMove();
        }));

        add(createButton("Move Forward", "images/forward.png", e -> {
//            chessController.redoMove();
        }));
    }

    private JButton createButton(String tooltip, String imgPath, ActionListener action) {
        Image iconImage = ImageLoader.getImage(imgPath, ICON_SIZE, ICON_SIZE);
        JButton button = new JButton(new ImageIcon(iconImage));
        button.setToolTipText(tooltip);
        button.setBackground(new Color(40, 40, 40));
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        button.setFocusPainted(false);
        button.addActionListener(action);
        return button;
    }
}