package com.minhduc5a12.chess.game;

import com.minhduc5a12.chess.constants.GameConstants;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.model.BoardState;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;
import com.minhduc5a12.chess.core.pieces.*;
import com.minhduc5a12.chess.ui.board.ChessTile;
import com.minhduc5a12.chess.utils.BoardUtils;
import com.minhduc5a12.chess.utils.ChessNotationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardManager {

    protected static final Logger logger = LoggerFactory.getLogger(BoardManager.class);

    // Fields
    private final ChessTile[][] tiles = new ChessTile[GameConstants.Board.BOARD_SIZE][GameConstants.Board.BOARD_SIZE];
    private final Map<BoardState, Integer> boardStateHistory;
    private ChessTile currentLeftClickedTile;
    private List<ChessMove> currentValidMoves;
    private final BoardState currentBoardState;

    // Constructor
    public BoardManager() {
        this.boardStateHistory = new HashMap<>();
        this.currentLeftClickedTile = null;
        this.currentBoardState = new BoardState(new ChessPieceMap());
        this.currentValidMoves = new ArrayList<>();
        initializeTiles();
    }

    // --- Getters and Setters ---

    public BoardState getCurrentBoardState() {
        return currentBoardState;
    }

    public ChessPiece getPiece(ChessPosition position) {
        return currentBoardState.getChessPieceMap().getPiece(position);
    }

    public ChessPiece getPiece(String chessNotation) {
        return currentBoardState.getChessPieceMap().getPiece(chessNotation);
    }

    public PieceColor getCurrentPlayerColor() {
        return currentBoardState.getCurrentPlayerColor();
    }

    public ChessTile getCurrentLeftClickedTile() {
        return currentLeftClickedTile;
    }

    public void setCurrentLeftClickedTile(ChessTile tile) {
        if (currentLeftClickedTile != null) {
            currentLeftClickedTile.setLeftClickSelected(false);
            clearValidMoveHighlights();
        }
        currentLeftClickedTile = tile;
        if (tile != null) {
            tile.setLeftClickSelected(true);
            generateAndHighlightValidMoves(tile);
        }
    }

    public ChessPieceMap getChessPieceMap() {
        return currentBoardState.getChessPieceMap();
    }

    public ChessTile[][] getTiles() {
        return tiles;
    }

    public ChessTile getTile(ChessPosition position) {
        return tiles[position.matrixRow()][position.matrixCol()];
    }

    public ChessMove getLastMove() {
        return currentBoardState.getLastMove();
    }

    public void setLastMove(ChessMove lastMove) {
        clearLastMoveHighlights();
        currentBoardState.setLastMove(lastMove);
        if (lastMove != null) {
            highlightLastMove();
            logger.info("Last move: {}", lastMove);
        }
    }

    public Map<BoardState, Integer> getBoardStateHistory() {
        return boardStateHistory;
    }

    public List<ChessMove> getCurrentValidMoves() {
        return currentValidMoves;
    }

    public void setPiece(ChessPosition position, ChessPiece piece) {
        currentBoardState.getChessPieceMap().setPiece(position, piece);
        getTile(position).setPiece(piece);
    }

    public void setPiece(int x, int y, ChessPiece piece) {
        setPiece(new ChessPosition(x, y), piece);
    }

    // --- Core methods ---

    private void initializeTiles() {
        for (int row = 0; row < GameConstants.Board.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConstants.Board.BOARD_SIZE; col++) {
                tiles[row][col] = new ChessTile(new ChessPosition(col, GameConstants.Board.BOARD_SIZE - row - 1), (ChessController) this);
            }
        }
    }

    public void setupInitialPosition() {
        clear();
        placeInitialPieces(PieceColor.WHITE, 0, 1);
        placeInitialPieces(PieceColor.BLACK, 7, 6);
    }

    public void repaintPieces() {
        for (Map.Entry<ChessPosition, ChessPiece> entry : currentBoardState.getChessPieceMap().getPieceMap().entrySet()) {
            ChessPosition position = entry.getKey();
            ChessPiece piece = entry.getValue();
            if (piece != null) {
                ChessTile tile = getTile(position);
                if (!tile.getPiece().equals(piece)) {
                    tile.setPiece(piece);
                }
            }
        }
    }

    public void switchTurn() {
        currentBoardState.setCurrentPlayerColor(currentBoardState.getCurrentPlayerColor().getOpponent());
        clearCurrentValidMoves();
        setCurrentLeftClickedTile(null);

        currentBoardState.incrementFullmoveNumber();

        logger.debug("Switched turn to: {}", currentBoardState.getCurrentPlayerColor());
    }

    public void clear() {
        currentBoardState.getChessPieceMap().clear();
        for (ChessTile[] row : tiles) {
            for (ChessTile tile : row) {
                tile.setPiece(null);
            }
        }
        currentLeftClickedTile = null;
    }

    public void removePiece(ChessPosition position) {
        currentBoardState.getChessPieceMap().removePiece(position);
        getTile(position).setPiece(null);
    }

    public void updatePieceMovement(ChessMove move) {
        ChessPiece piece = getPiece(move.end());
        if (piece != null) {
            piece.setHasMoved(true);
            logger.info("Updated piece movement: {}, hasMoved={}", piece, piece.hasMoved());
        }
    }

    public void updateBoardStateHistory() {
        String FEN = ChessNotationUtils.getFEN(currentBoardState);
        boardStateHistory.merge(currentBoardState, 1, Integer::sum);
        logger.debug("Updated board state (FEN): {}, occurrences: {}", FEN, boardStateHistory.get(currentBoardState));
    }

    public void repaintTiles(ChessTile... tiles) {
        for (ChessTile tile : tiles) {
            if (tile != null) {
                tile.repaint();
            }
        }
    }

    // --- Helper methods ---

    private void placeInitialPieces(PieceColor color, int backRow, int pawnRow) {
        setPiece(0, backRow, new Rook(color));
        setPiece(1, backRow, new Knight(color));
        setPiece(2, backRow, new Bishop(color));
        setPiece(3, backRow, new Queen(color));
        setPiece(4, backRow, new King(color));
        setPiece(5, backRow, new Bishop(color));
        setPiece(6, backRow, new Knight(color));
        setPiece(7, backRow, new Rook(color));
        for (int col = 0; col < 8; col++) {
            setPiece(col, pawnRow, color.isWhite() && col < 1 ? new Queen(color) : new Pawn(color, this.currentBoardState));
        }
    }

    private void generateAndHighlightValidMoves(ChessTile tile) {
        currentValidMoves = tile.getPiece().generateValidMoves(tile.getPosition(), currentBoardState.getChessPieceMap());
        for (ChessMove move : currentValidMoves) {
            if (BoardUtils.isMoveValidUnderCheck(move, currentBoardState.getChessPieceMap())) {
                ChessTile endTile = getTile(move.end());
                if (endTile != null) {
                    endTile.setValidMove(true);
                }
            }
        }
    }

    private void clearValidMoveHighlights() {
        for (ChessMove move : currentValidMoves) {
            ChessTile endTile = getTile(move.end());
            if (endTile != null) {
                endTile.setValidMove(false);
            }
        }
    }

    public void clearCurrentValidMoves() {
        if (!currentValidMoves.isEmpty()) {
            clearValidMoveHighlights();
            currentValidMoves.clear();
        }
    }

    public void clearLastMoveHighlights() {
        if (currentBoardState.getLastMove() != null) {
            ChessTile startTile = getTile(currentBoardState.getLastMove().start());
            ChessTile endTile = getTile(currentBoardState.getLastMove().end());
            if (startTile != null) {
                startTile.setLastMove(false);
            }
            if (endTile != null) {
                endTile.setLastMove(false);
            }
        }
    }

    private void highlightLastMove() {
        ChessTile startTile = getTile(currentBoardState.getLastMove().start());
        ChessTile endTile = getTile(currentBoardState.getLastMove().end());
        if (startTile != null) {
            startTile.setLastMove(true);
        }
        if (endTile != null) {
            endTile.setLastMove(true);
        }
    }
}
