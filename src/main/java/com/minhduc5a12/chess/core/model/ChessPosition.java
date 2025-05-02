package com.minhduc5a12.chess.core.model;

import java.util.HashMap;
import java.util.Map;

import com.minhduc5a12.chess.constants.GameConstants;
import com.minhduc5a12.chess.exception.InvalidPositionException;

/**
 * A record representing a position on a chessboard using Cartesian coordinates (col, row).
 * The column (col) corresponds to the file (a-h), and the row corresponds to the rank (1-8).
 * The coordinates are validated to ensure they fall within the chessboard boundaries (0 to 7).
 * If a position is outside the board, an {@link InvalidPositionException} is thrown.
 *
 * @param col the column index (0 to 7, corresponding to files a to h)
 * @param row the row index (0 to 7, corresponding to ranks 1 to 8)
 */
public record ChessPosition(int col, int row) {

    private static final Map<String, ChessPosition> POSITIONS = new HashMap<>();

    static {
        for (char file = 'a'; file <= 'h'; file++) {
            for (int rank = 1; rank <= 8; rank++) {
                String notation = "" + file + rank;
                POSITIONS.put(notation.toUpperCase(), new ChessPosition(file - 'a', rank - 1));
            }
        }
    }

    /**
     * Validates the chess position to ensure it lies within the board boundaries.
     * Throws an exception if the position is invalid.
     *
     * @throws IllegalArgumentException if the col or row is outside the board (0 to 7)
     */
    public ChessPosition {
        if (col < 0 || col >= GameConstants.Board.BOARD_SIZE || row < 0 || row >= GameConstants.Board.BOARD_SIZE) {
            throw new InvalidPositionException("Invalid position: (" + col + ", " + row + ")");
        }
    }

    /**
     * Retrieves a chess position from standard algebraic notation (e.g., "a1").
     *
     * @param notation the algebraic notation of the position (e.g., "a1", "h8")
     * @return the corresponding {@link ChessPosition}
     * @throws IllegalArgumentException if the notation is invalid
     */
    public static ChessPosition get(String notation) {
        return POSITIONS.get(notation.toUpperCase());
    }

    /**
     * Converts the position to standard algebraic chess notation (e.g., "a1").
     *
     * @return the algebraic notation of the position
     */
    public String toChessNotation() {
        return "" + (char) ('a' + col) + (row + 1);
    }

    /**
     * Converts a standard algebraic notation to a {@link ChessPosition}.
     *
     * @param notation the algebraic notation (e.g., "a1", "h8")
     * @return the corresponding {@link ChessPosition}
     * @throws IllegalArgumentException if the notation is invalid (e.g., null, wrong format, or out of bounds)
     */
    public static ChessPosition toChessPosition(String notation) {
        if (notation == null || notation.length() != 2 || !Character.isLetter(notation.charAt(0)) || !Character.isDigit(notation.charAt(1)) || notation.charAt(0) < 'a' || notation.charAt(0) > 'h' || notation.charAt(1) < '1' || notation.charAt(1) > '8') {
            throw new IllegalArgumentException("Invalid chess notation: " + notation);
        }
        return new ChessPosition(notation.charAt(0) - 'a', notation.charAt(1) - '1');
    }

    /**
     * Returns the column index in the Cartesian coordinate system (0 to 7).
     *
     * @return the column index
     */
    public int matrixCol() {
        return col;
    }

    /**
     * Returns the row index in the matrix representation, adjusted for the board's orientation.
     * The row is inverted to match the matrix representation (rank 8 at row 0, rank 1 at row 7).
     *
     * @return the adjusted row index
     */
    public int matrixRow() {
        return GameConstants.Board.BOARD_SIZE - row - 1;
    }

    public ChessPosition deepCopy() {
        return new ChessPosition(col, row);
    }
}