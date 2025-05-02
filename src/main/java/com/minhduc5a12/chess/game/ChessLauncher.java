package com.minhduc5a12.chess.game;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.ui.ChessUI;
import com.minhduc5a12.chess.ui.components.dialogs.GameModeSelectionDialog;

public class ChessLauncher {
    public static void launch() {
        GameModeSelectionDialog dialog = new GameModeSelectionDialog(null);
        dialog.setVisible(true);

        int selectedMode = dialog.getSelectedMode();
        PieceColor selectedColor = dialog.getSelectedColor();

        ChessUI chessUI = new ChessUI(selectedMode, selectedColor);
        chessUI.show();
    }
}