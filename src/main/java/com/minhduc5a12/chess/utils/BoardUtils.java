package com.minhduc5a12.chess.utils;

public class BoardUtils {
    // Kích thước bàn cờ
    public static final int BOARD_SIZE = 8;

    /**
     * Kiểm tra xem tọa độ (x, y) có nằm trong phạm vi bàn cờ không.
     *
     * @param x Tọa độ cột (0 đến 7).
     * @param y Tọa độ hàng (0 đến 7).
     * @return true nếu tọa độ hợp lệ, false nếu không.
     */
    public static boolean isWithinBoard(int x, int y) {
        return x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE;
    }
}