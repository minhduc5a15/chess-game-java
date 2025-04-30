package com.minhduc5a12.chess.model;

import com.minhduc5a12.chess.constants.GameConstants;

import java.util.HashMap;
import java.util.Map;

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

    public ChessPosition {
        if (col < 0 || col >= GameConstants.Board.BOARD_SIZE || row < 0 || row >= GameConstants.Board.BOARD_SIZE) {
            throw new IllegalArgumentException("Invalid position: (" + col + ", " + row + ")");
        }
    }

    public static ChessPosition get(String notation) {
        return POSITIONS.get(notation.toUpperCase());
    }

    public String toChessNotation() {
        return "" + (char) ('a' + col) + (row + 1);
    }

    public static ChessPosition toChessPosition(String notation) {
        if (notation == null || notation.length() != 2 || !Character.isLetter(notation.charAt(0)) || !Character.isDigit(notation.charAt(1)) || notation.charAt(0) < 'a' || notation.charAt(0) > 'h' || notation.charAt(1) < '1' || notation.charAt(1) > '8') {
            throw new IllegalArgumentException("Invalid chess notation: " + notation);
        }
        return new ChessPosition(notation.charAt(0) - 'a', notation.charAt(1) - '1');
    }

    public int matrixCol() {
        return col;
    }

    public int matrixRow() {
        return GameConstants.Board.BOARD_SIZE - row - 1;
    }
}
