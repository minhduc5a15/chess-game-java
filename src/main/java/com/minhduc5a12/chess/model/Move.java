package com.minhduc5a12.chess.model;

public record Move(int startX, int startY, int endX, int endY) {

    @Override
    public String toString() {
        return "Move{" + "startX=" + startX + ", startY=" + startY + ", endX=" + endX + ", endY=" + endY + '}';
    }
}