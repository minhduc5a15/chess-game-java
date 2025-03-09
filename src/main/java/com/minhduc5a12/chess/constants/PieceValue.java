package com.minhduc5a12.chess.constants;

import com.minhduc5a12.chess.pieces.Bishop;
import com.minhduc5a12.chess.pieces.ChessPiece;
import com.minhduc5a12.chess.pieces.King;
import com.minhduc5a12.chess.pieces.Knight;
import com.minhduc5a12.chess.pieces.Pawn;
import com.minhduc5a12.chess.pieces.Queen;
import com.minhduc5a12.chess.pieces.Rook;

/**
 * Class chứa các giá trị điểm số cố định cho các quân cờ trong cờ vua.
 */
public final class PieceValue {
    public static final int PAWN = 1;
    public static final int KNIGHT = 3;
    public static final int BISHOP = 3;
    public static final int ROOK = 5;
    public static final int QUEEN = 9;
    public static final int KING = 1000;

    private PieceValue() {
        throw new AssertionError("Cannot instantiate PieceValue class");
    }

    /**
     * Lấy giá trị điểm số của một quân cờ.
     *
     * @param piece Quân cờ cần lấy giá trị.
     * @return Giá trị điểm số tương ứng.
     * @throws IllegalArgumentException nếu piece là null hoặc không thuộc loại quân cờ hợp lệ.
     */
    public static int getValue(ChessPiece piece) {
        if (piece == null) {
            throw new IllegalArgumentException("ChessPiece cannot be null");
        }
        if (piece instanceof Pawn) return PAWN;
        if (piece instanceof Knight) return KNIGHT;
        if (piece instanceof Bishop) return BISHOP;
        if (piece instanceof Rook) return ROOK;
        if (piece instanceof Queen) return QUEEN;
        if (piece instanceof King) return KING;
        throw new IllegalArgumentException("Unknown ChessPiece type: " + piece.getClass().getSimpleName());
    }
}