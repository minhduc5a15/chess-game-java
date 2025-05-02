package com.minhduc5a12.chess.core.model;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.pieces.ChessPieceMap;
import com.minhduc5a12.chess.utils.BoardUtils;
import com.minhduc5a12.chess.utils.ImageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;

public abstract class ChessPiece implements Comparable<ChessPiece> {

    protected static final Logger logger = LoggerFactory.getLogger(ChessPiece.class);
    private final PieceColor color;
    private Image image;
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
        return ImageLoader.getImage(path, size, size);
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

    public void setImage(Image image) {
        this.image = image;
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

    public abstract ChessPiece deepCopy();

    public boolean equals(ChessPiece other) {
        if (other == null) return false;
        return this.getClass() == other.getClass() && this.getColor() == other.getColor() && this.hasMoved == other.hasMoved;
    }
}