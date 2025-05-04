package com.minhduc5a12.chess.core.model;

public record ChessMove(ChessPosition start, ChessPosition end) {

    @Override
    public String toString() {
        return "Move{" + start.toString() + ", " + end.toString() + "}";
    }

    public ChessMove(int startX, int startY, int endX, int endY) {
        this(new ChessPosition(startX, startY), new ChessPosition(endX, endY));
    }

    public ChessMove deepCopy() {
        return new ChessMove(start.deepCopy(), end.deepCopy());
    }

    public String moveNotation() {
        return start.toChessNotation() + end.toChessNotation();
    }
}
