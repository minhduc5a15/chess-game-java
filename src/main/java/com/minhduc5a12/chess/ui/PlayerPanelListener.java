package com.minhduc5a12.chess.ui;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.model.ChessPiece;

public interface PlayerPanelListener {
    void onScoreUpdated(PieceColor color, int score);

    void onPieceCaptured(PieceColor capturerColor, ChessPiece capturedPiece);

    void onTurnChanged(PieceColor currentPlayerColor);
}
