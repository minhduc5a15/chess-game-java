package com.minhduc5a12.chess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minhduc5a12.chess.constants.GameConstants;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.model.BoardState;
import com.minhduc5a12.chess.model.ChessMove;
import com.minhduc5a12.chess.model.ChessPiece;
import com.minhduc5a12.chess.model.ChessPosition;
import com.minhduc5a12.chess.pieces.Bishop;
import com.minhduc5a12.chess.pieces.ChessPieceMap;
import com.minhduc5a12.chess.pieces.King;
import com.minhduc5a12.chess.pieces.Knight;
import com.minhduc5a12.chess.pieces.Pawn;
import com.minhduc5a12.chess.pieces.Queen;
import com.minhduc5a12.chess.pieces.Rook;
import com.minhduc5a12.chess.utils.BoardUtils;
import com.minhduc5a12.chess.utils.ChessNotationUtils;

public class BoardManager {

    protected static final Logger logger = LoggerFactory.getLogger(BoardManager.class);

    // Fields
    private final ChessTile[][] tiles = new ChessTile[GameConstants.Board.BOARD_SIZE][GameConstants.Board.BOARD_SIZE];
    // private final ChessPieceMap chessPieceMap;
    private final Map<BoardState, Integer> boardStateHistory;
    private final ChessNotationUtils notationUtils;
    private ChessTile currentLeftClickedTile;
    private List<ChessMove> currentValidMoves;
    private final BoardState currentBoardState;

    // Constructor
    public BoardManager() {
        this.boardStateHistory = new HashMap<>();
        this.notationUtils = new ChessNotationUtils();
        this.currentLeftClickedTile = null;
        this.currentBoardState = new BoardState(new ChessPieceMap());
        this.currentValidMoves = new ArrayList<>();
        initializeTiles();
    }

    // Initialize tiles array
    private void initializeTiles() {
        for (int row = 0; row < GameConstants.Board.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConstants.Board.BOARD_SIZE; col++) {
                tiles[row][col] = new ChessTile(new ChessPosition(col, GameConstants.Board.BOARD_SIZE - row - 1), (ChessController) this);
            }
        }
    }

    // Getters
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

    public int getHalfmoveClock() {
        return currentBoardState.getHalfmoveClock();
    }

    public int getFullmoveNumber() {
        return currentBoardState.getFullmoveNumber();
    }

    public Map<BoardState, Integer> getBoardStateHistory() {
        return boardStateHistory;
    }

    public ChessNotationUtils getNotationUtils() {
        return notationUtils;
    }

    public List<ChessMove> getCurrentValidMoves() {
        return currentValidMoves;
    }

    // Setters
    public void setPiece(ChessPosition position, ChessPiece piece) {
        currentBoardState.getChessPieceMap().setPiece(position, piece);
        getTile(position).setPiece(piece);
    }

    public void setPiece(int x, int y, ChessPiece piece) {
        setPiece(new ChessPosition(x, y), piece);
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

    public void setLastMove(ChessMove lastMove) {
        clearLastMoveHighlights();
        currentBoardState.setLastMove(lastMove);
        if (lastMove != null) {
            highlightLastMove();
            logger.info("Last move: {}", lastMove);
        }
    }


    // Core methods
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
                getTile(position).repaint();
            }
        }
    }

    public void switchTurn() {
        // currentPlayerColor = currentPlayerColor.getOpponent();
        currentBoardState.setCurrentPlayerColor(currentBoardState.getCurrentPlayerColor().getOpponent());
        clearCurrentValidMoves();
        setCurrentLeftClickedTile(null);
        // fullmoveNumber++;
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
        }
    }

    public void updateBoardStateHistory() {
        String FEN = notationUtils.getFEN(currentBoardState);
        logger.debug("Updated board state (FEN): {}, occurrences: {}", FEN, boardStateHistory.get(currentBoardState));
    }

    public void repaintTiles(ChessTile... tiles) {
        for (ChessTile tile : tiles) {
            if (tile != null) {
                tile.repaint();
            }
        }
    }

    // Helper methods
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
            setPiece(col, pawnRow, new Pawn(color, this.currentBoardState));
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

    private void clearLastMoveHighlights() {
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
