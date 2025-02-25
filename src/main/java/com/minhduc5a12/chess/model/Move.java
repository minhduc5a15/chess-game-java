package com.minhduc5a12.chess.model;

public class Move {
    private final int startX;
    private final int startY;
    private final int endX;
    private final int endY;

    public Move(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move move = (Move) obj;
        return startX == move.startX && startY == move.startY && endX == move.endX && endY == move.endY;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(startX, startY, endX, endY);
    }

    @Override
    public String toString() {
        return "Move{" + "startX=" + startX + ", startY=" + startY + ", endX=" + endX + ", endY=" + endY + '}';
    }
}