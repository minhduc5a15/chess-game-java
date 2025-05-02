package com.minhduc5a12.chess.game;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;

public interface MoveExecutor {
    boolean executeMove(ChessMove move);

    boolean performCastling(boolean isKingside, PieceColor color);

    boolean performEnPassant(ChessMove move);

    ChessPiece promotePawn(ChessPosition position, PieceColor color);
}