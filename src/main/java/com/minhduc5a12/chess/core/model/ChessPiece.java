package com.minhduc5a12.chess.core.model;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.pieces.ChessPieceMap;
import com.minhduc5a12.chess.utils.BoardUtils;
import com.minhduc5a12.chess.utils.ImageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;

/**
 * Abstract base class for chess pieces, defining common properties and behaviors.
 * Provides methods for move validation, image handling, and piece comparison.
 */
public abstract class ChessPiece implements Comparable<ChessPiece> {

    protected static final Logger logger = LoggerFactory.getLogger(ChessPiece.class);
    private final PieceColor color;
    private Image image;
    protected int pieceValue = 0;
    private boolean hasMoved = false;

    /**
     * Constructs a ChessPiece with the specified color and image file.
     * Uses default image size of 95 pixels.
     *
     * @param color         the color of the piece (white or black)
     * @param imageFileName the file name of the piece's image
     */
    public ChessPiece(PieceColor color, String imageFileName) {
        this(color, imageFileName, 95);
    }

    /**
     * Constructs a ChessPiece with the specified color, image file, and image size.
     *
     * @param color         the color of the piece (white or black)
     * @param imageFileName the file name of the piece's image
     * @param size          the size of the image in pixels
     */
    public ChessPiece(PieceColor color, String imageFileName, int size) {
        this.color = color;
        String imagePath = "images/pieces/" + imageFileName;
        this.image = loadImage(imagePath, size);
    }

    /**
     * Loads an image for the piece from the specified path with the given size.
     *
     * @param path the file path of the image
     * @param size the size of the image in pixels
     * @return the loaded image
     */
    private Image loadImage(String path, int size) {
        return ImageLoader.getImage(path, size, size);
    }

    // --- Getters and Setters ---

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

    // --- Move Validation ---

    /**
     * Checks if a move is valid for this piece, ensuring it does not put the player's king in check.
     *
     * @param move     the move to validate
     * @param pieceMap the current state of the chess board
     * @return true if the move is valid and legal, false otherwise
     */
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

    /**
     * Generates a list of valid moves for this piece from the starting position.
     *
     * @param start    the starting position of the piece
     * @param pieceMap the current state of the chess board
     * @return a list of valid moves
     */
    public abstract List<ChessMove> generateValidMoves(ChessPosition start, ChessPieceMap pieceMap);

    // --- Piece Identification ---

    /**
     * Returns the standard algebraic notation for this piece (e.g., "K" for King).
     *
     * @return the piece's notation
     */
    public abstract String getPieceNotation();

    // --- Comparison and Copying ---

    /**
     * Compares this piece to another based on their piece values.
     *
     * @param other the other piece to compare to
     * @return a negative integer, zero, or a positive integer if this piece's value is less than, equal to, or greater than the other
     */
    @Override
    public int compareTo(ChessPiece other) {
        return Integer.compare(this.pieceValue, other.pieceValue);
    }

    /**
     * Creates a deep copy of this piece.
     *
     * @return a new instance of the piece with the same properties
     */
    public abstract ChessPiece deepCopy();
}