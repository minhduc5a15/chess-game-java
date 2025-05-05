package com.minhduc5a12.chess.ui.board;

import com.minhduc5a12.chess.constants.GameConstants;
import com.minhduc5a12.chess.constants.GameMode;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;
import com.minhduc5a12.chess.game.BoardManager;
import com.minhduc5a12.chess.game.ChessController;
import com.minhduc5a12.chess.utils.BoardUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ChessBoardUI {

    private static final Logger logger = LoggerFactory.getLogger(ChessBoardUI.class);
    private final ChessController controller;
    private final BoardManager boardManager;
    private final ChessTile[][] tiles;
    private ChessTile currentLeftClickedTile;
    private List<ChessMove> currentValidMoves;

    public ChessBoardUI(ChessController controller, BoardManager boardManager) {
        this.controller = controller;
        this.boardManager = boardManager;
        this.tiles = new ChessTile[GameConstants.Board.BOARD_SIZE][GameConstants.Board.BOARD_SIZE];
        this.currentValidMoves = new ArrayList<>();
        initializeTiles();
    }

    private void initializeTiles() {
        for (int row = 0; row < GameConstants.Board.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConstants.Board.BOARD_SIZE; col++) {
                tiles[row][col] = new ChessTile(new ChessPosition(col, GameConstants.Board.BOARD_SIZE - row - 1), controller);
            }
        }
        logger.debug("ChessBoardUI tiles initialized");
    }

    public ChessTile[][] getTiles() {
        return tiles;
    }

    public ChessTile getTile(ChessPosition position) {
        return tiles[position.matrixRow()][position.matrixCol()];
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

    public List<ChessMove> getCurrentValidMoves() {
        return currentValidMoves;
    }

    public void clearCurrentValidMoves() {
        if (!currentValidMoves.isEmpty()) {
            clearValidMoveHighlights();
            currentValidMoves.clear();
        }
    }

    public void repaintPieces() {
        for (int row = 0; row < GameConstants.Board.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConstants.Board.BOARD_SIZE; col++) {
                ChessPosition position = new ChessPosition(col, GameConstants.Board.BOARD_SIZE - row - 1);
                ChessPiece piece = boardManager.getPiece(position);
                tiles[row][col].setPiece(piece);
                tiles[row][col].repaint();
            }
        }
        logger.debug("Repainted all pieces");
    }

    public void repaintTiles(ChessTile... tiles) {
        for (ChessTile tile : tiles) {
            if (tile != null) {
                ChessPiece piece = boardManager.getPiece(tile.getPosition());
                tile.setPiece(piece);
                tile.repaint();
            }
        }
        logger.debug("Repainted tiles: {}", tiles.length);
    }

    public void clearLastMoveHighlights() {
        for (int row = 0; row < GameConstants.Board.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConstants.Board.BOARD_SIZE; col++) {
                ChessTile tile = tiles[row][col];
                if (tile.isInLastMove()) {
                    tile.setInLastMove(false);
                    tile.repaint();
                    logger.debug("Cleared last move highlight for tile: {}", tile.getPosition().toChessNotation());
                }
            }
        }
        logger.debug("Cleared all last move highlights");
    }

    public void highlightLastMove() {
        ChessMove lastMove = boardManager.getLastMove();
        if (lastMove != null) {
            ChessTile startTile = getTile(lastMove.start());
            ChessTile endTile = getTile(lastMove.end());
            if (startTile != null) {
                startTile.setInLastMove(true);
                startTile.repaint();
                logger.debug("Highlighted last move start tile: {}", startTile.getPosition().toChessNotation());
            }
            if (endTile != null) {
                endTile.setInLastMove(true);
                endTile.repaint();
                logger.debug("Highlighted last move end tile: {}", endTile.getPosition().toChessNotation());
            }
        }
    }

    private void generateAndHighlightValidMoves(ChessTile tile) {
        if (tile.getPiece() == null) return;
        currentValidMoves = tile.getPiece().generateValidMoves(tile.getPosition(), boardManager.getChessPieceMap());

        if (controller.getGameMode() == GameMode.AI_VS_AI) return;
        for (ChessMove move : currentValidMoves) {
            if (BoardUtils.isMoveValidUnderCheck(move, boardManager.getChessPieceMap())) {
                ChessTile endTile = getTile(move.end());
                if (endTile != null) {
                    endTile.setInValidMove(true);
                    endTile.repaint();
                }
            }
        }
        logger.debug("Generated and highlighted valid moves for tile: {}", tile.getPosition().toChessNotation());
    }

    private void clearValidMoveHighlights() {
        for (ChessMove move : currentValidMoves) {
            ChessTile endTile = getTile(move.end());
            if (endTile != null) {
                endTile.setInValidMove(false);
                endTile.repaint();
            }
        }
        logger.debug("Cleared valid move highlights");
    }

    public void flipBoard() {
        logger.debug("Flipping board UI, delegating to ChessBoard");
        controller.getChessBoard().flipBoard();
    }

    public void clear() {
        for (ChessTile[] row : tiles) {
            for (ChessTile tile : row) {
                tile.setPiece(null);
                tile.setInLastMove(false);
                tile.setInValidMove(false);
                tile.setHintHighlightedSquare(false);
                tile.repaint();
            }
        }
        currentLeftClickedTile = null;
        clearCurrentValidMoves();
        clearLastMoveHighlights();
        logger.debug("Cleared ChessBoardUI");
    }

    public void clearHintHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessTile tile = getTile(new ChessPosition(col, row));
                if (tile.isHintHighlightedSquare()) {
                    tile.setHintHighlightedSquare(false);
                }
            }
        }
        logger.debug("Hint highlights cleared");
    }

    public void updateBoardUI() {
        clearLastMoveHighlights();
        highlightLastMove();
        clearHintHighlights();
    }
}