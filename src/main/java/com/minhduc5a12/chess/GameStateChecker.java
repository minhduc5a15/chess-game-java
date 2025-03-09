package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.pieces.ChessPiece;
import com.minhduc5a12.chess.utils.BoardUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GameStateChecker {
    private static final Logger logger = LoggerFactory.getLogger(GameStateChecker.class);
    private final BoardManager boardManager;

    public GameStateChecker(BoardManager boardManager) {
        this.boardManager = boardManager;
    }

    public boolean isKingInCheck(PieceColor color) {
        return BoardUtils.isKingInCheck(boardManager.getBoard(), color, boardManager.getPiecePositions());
    }

    public boolean isCheckmate(PieceColor color) {
        return BoardUtils.isCheckmate(boardManager.getBoard(), color, boardManager.getPiecePositions());
    }

    public boolean isDeadPosition(PieceColor color) {
        return BoardUtils.isDeadPosition(boardManager.getBoard(), color, boardManager.getPiecePositions());
    }

    public boolean isThreefoldRepetition(List<String> boardStates) {
        return BoardUtils.isThreefoldRepetition(boardStates);
    }

    public boolean isFiftyMoveRule(int movesWithoutCaptureOrPawn) {
        return BoardUtils.isFiftyMoveRule(movesWithoutCaptureOrPawn);
    }

    public boolean isValidMove(int startX, int startY, int endX, int endY, PieceColor currentPlayerColor) {
        ChessPiece piece = boardManager.getBoard()[startY][startX].getPiece();
        if (piece == null || piece.getColor() != currentPlayerColor) {
            return false;
        }
        return piece.isValidMove(startX, startY, endX, endY, boardManager.getBoard());
    }

    public boolean isMoveValidUnderCheck(int startX, int startY, int endX, int endY, PieceColor currentPlayerColor) {
        return BoardUtils.isMoveValidUnderCheck(startX, startY, endX, endY, boardManager.getBoard(), currentPlayerColor, boardManager.getPiecePositions());
    }
}