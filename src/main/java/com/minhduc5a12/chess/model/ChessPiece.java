package com.minhduc5a12.chess.model;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.pieces.ChessPieceMap;
import com.minhduc5a12.chess.utils.BoardUtils;
import com.minhduc5a12.chess.utils.ImageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;

public abstract class ChessPiece implements Comparable<ChessPiece> {
    protected static final Logger logger = LoggerFactory.getLogger(ChessPiece.class);
    private final PieceColor color;
    private final Image image;
    protected int pieceValue = 0;
    private boolean hasMoved = false;

    public ChessPiece(PieceColor color, String imageFileName) {
        this(color, imageFileName, 95);
    }

    public ChessPiece(PieceColor color, String imageFileName, int size) {
        this.color = color;
        String imagePath = "images/pieces/" + imageFileName;
        this.image = loadImage(imagePath, size);
    }

    private Image loadImage(String path, int size) {
        logger.debug("Loading image for piece: {}", path);
        final Image loadedImage = ImageLoader.getImage(path, size, size);
        if (loadedImage == null) {
            logger.error("Failed to load image for piece: {}", path);
        } else {
            logger.info("Image loaded successfully for piece: {}", path);
        }
        return loadedImage;
    }

    public PieceColor getColor() {
        return color;
    }

    public Image getImage() {
        return image;
    }

    public int getPieceValue() {
        return pieceValue;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public boolean isValidMove(ChessMove move, ChessPieceMap pieceMap) {
        final List<ChessMove> moves = generateValidMoves(move.start(), pieceMap);

        for (ChessMove chessMove : moves) {
            ChessPieceMap tempMap = BoardUtils.simulateMove(chessMove, pieceMap);
            if (!BoardUtils.isKingInCheck(this.color, tempMap) && chessMove.equals(move)) {
                return true;
            }
        }

        return false;
    }

    public abstract List<ChessMove> generateValidMoves(ChessPosition start, ChessPieceMap pieceMap);

    public abstract String getPieceNotation();

    @Override
    public int compareTo(ChessPiece other) {
        return Integer.compare(this.pieceValue, other.pieceValue);
    }
}
