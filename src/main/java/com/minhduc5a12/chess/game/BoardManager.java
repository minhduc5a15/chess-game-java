package com.minhduc5a12.chess.game;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.model.BoardState;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;
import com.minhduc5a12.chess.core.pieces.*;
import com.minhduc5a12.chess.utils.ChessNotationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the chess board state, including pieces, moves, and history.
 */
public class BoardManager {

    protected static final Logger logger = LoggerFactory.getLogger(BoardManager.class);
    private final Map<BoardState, Integer> boardStateHistory;
    private final BoardState currentBoardState;

    /**
     * Constructs a new BoardManager with an empty board state.
     */
    public BoardManager() {
        this.boardStateHistory = new HashMap<>();
        this.currentBoardState = new BoardState(new ChessPieceMap());
    }

    /**
     * Sets a piece at the specified position.
     *
     * @param position The position to place the piece.
     * @param piece    The piece to place.
     */
    public void setPiece(ChessPosition position, ChessPiece piece) {
        currentBoardState.getChessPieceMap().setPiece(position, piece);
    }

    /**
     * Sets a piece at the specified coordinates.
     *
     * @param x     The column index (0-7).
     * @param y     The row index (0-7).
     * @param piece The piece to place.
     */
    public void setPiece(int x, int y, ChessPiece piece) {
        setPiece(new ChessPosition(x, y), piece);
    }

    /**
     * Gets the piece at the specified position.
     *
     * @param position The position to check.
     * @return The piece at the position, or null if none.
     */
    public ChessPiece getPiece(ChessPosition position) {
        return currentBoardState.getChessPieceMap().getPiece(position);
    }

    /**
     * Gets the piece at the specified chess notation (e.g., "e4").
     *
     * @param chessNotation The chess notation of the position.
     * @return The piece at the position, or null if none.
     * @ noon, at 6:00 PM
     * Gets the piece at the specified chess notation (e.g., "e4").
     */
    public ChessPiece getPiece(String chessNotation) {
        return currentBoardState.getChessPieceMap().getPiece(chessNotation);
    }

    /**
     * Removes a piece from the specified position.
     *
     * @param position The position to clear.
     */
    public void removePiece(ChessPosition position) {
        currentBoardState.getChessPieceMap().removePiece(position);
    }

    /**
     * Updates the movement status of a piece after a move.
     *
     * @param move The move performed.
     */
    public void updatePieceMovement(ChessMove move) {
        ChessPiece piece = getPiece(move.end());
        if (piece != null) {
            piece.setHasMoved(true);
            logger.info("Updated piece movement: {}, hasMoved={}", piece, piece.hasMoved());
        }
    }

    /**
     * Updates the board state history with the current state.
     */
    public void updateBoardStateHistory() {
        String fenFourParts = ChessNotationUtils.getFenFourParts(currentBoardState);
        String FEN = ChessNotationUtils.getFEN(currentBoardState);
        boardStateHistory.merge(currentBoardState, 1, Integer::sum);
        logger.debug("Updated board state (FEN four parts): {}, occurrences: {}", fenFourParts, boardStateHistory.get(currentBoardState));

        logger.debug("Current board state (FEN): {}", FEN);
    }

    /**
     * Sets up the initial chess position with standard piece placement.
     */
    public void setupInitialPosition() {
        clear();
        placeInitialPieces(PieceColor.WHITE, 0, 1);
        placeInitialPieces(PieceColor.BLACK, 7, 6);
    }

    /**
     * Clears all pieces from the board.
     */
    public void clear() {
        currentBoardState.getChessPieceMap().clear();
    }

    /**
     * Places initial pieces for a player on the specified rows.
     *
     * @param color   The color of the pieces.
     * @param backRow The row for major pieces (0 or 7).
     * @param pawnRow The row for pawns (1 or 6).
     */
    private void placeInitialPieces(PieceColor color, int backRow, int pawnRow) {
        ChessPiece[] backRowPieces = {new Rook(color), new Knight(color), new Bishop(color), new Queen(color), new King(color), new Bishop(color), new Knight(color), new Rook(color)};
        for (int col = 0; col < 8; col++) {
            setPiece(col, backRow, backRowPieces[col]);
            setPiece(col, pawnRow, new Pawn(color, this.currentBoardState));
        }
    }

    // Getters and Setters

    public BoardState getCurrentBoardState() {
        return currentBoardState;
    }

    public PieceColor getCurrentPlayerColor() {
        return currentBoardState.getCurrentPlayerColor();
    }

    public ChessPieceMap getChessPieceMap() {
        return currentBoardState.getChessPieceMap();
    }

    public ChessMove getLastMove() {
        return currentBoardState.getLastMove();
    }

    public void setLastMove(ChessMove lastMove) {
        currentBoardState.setLastMove(lastMove);
        logger.info("Last move: {}", lastMove);
    }

    public Map<BoardState, Integer> getBoardStateHistory() {
        return boardStateHistory;
    }
}