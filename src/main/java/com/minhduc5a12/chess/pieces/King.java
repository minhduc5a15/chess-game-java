package com.minhduc5a12.chess.pieces;

import com.minhduc5a12.chess.ChessPiece;
import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.constants.PieceColor;

public class King extends ChessPiece {
    public King(PieceColor color, String imagePath) {
        super(color, imagePath);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, ChessTile[][] board) {
        // Kiểm tra xem ô đích có nằm trong bàn cờ không
        if (endX < 0 || endX >= 8 || endY < 0 || endY >= 8) {
            return false;
        }

        // Kiểm tra xem ô đích có cùng màu với King không
        ChessPiece destinationPiece = board[endY][endX].getPiece();
        if (destinationPiece != null && destinationPiece.getColor() == this.getColor()) {
            return false;
        }

        // Tính toán khoảng cách di chuyển
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);

        // Kiểm tra nước đi nhập thành (castling)
        if (dx == 2 && dy == 0 && !hasMoved()) {
            return isCastlingMove(startX, startY, endX, endY, board);
        }

        // Kiểm tra di chuyển thông thường (1 ô theo mọi hướng)
        if (dx > 1 || dy > 1) {
            return false;
        }

        // Kiểm tra xem ô đích có đang bị tấn công không
        return !isSquareUnderAttack(endX, endY, board);
    }

    /**
     * Kiểm tra xem nước đi có phải là nhập thành hợp lệ không.
     *
     * @param startX Vị trí cột ban đầu của Vua.
     * @param startY Vị trí hàng ban đầu của Vua.
     * @param endX   Vị trí cột đích của Vua.
     * @param endY   Vị trí hàng đích của Vua.
     * @param board  Bàn cờ hiện tại.
     * @return true nếu là nước nhập thành hợp lệ, false nếu không.
     */
    private boolean isCastlingMove(int startX, int startY, int endX, int endY, ChessTile[][] board) {
        int direction = (endX > startX) ? 1 : -1; // Hướng di chuyển (cánh Vua hoặc cánh Hậu)
        int rookX = (direction == 1) ? 7 : 0; // Vị trí Xe tương ứng

        // Kiểm tra Xe có ở vị trí đúng và chưa di chuyển
        ChessPiece rook = board[startY][rookX].getPiece();
        if (!(rook instanceof Rook) || rook.hasMoved()) {
            return false;
        }

        // Kiểm tra không có quân cờ nào nằm giữa Vua và Xe
        for (int x = startX + direction; x != rookX; x += direction) {
            if (board[startY][x].getPiece() != null) {
                return false;
            }
        }

        // Kiểm tra Vua không bị chiếu và các ô đi qua không bị tấn công
        for (int x = startX; x != endX + direction; x += direction) {
            if (isSquareUnderAttack(x, startY, board)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Kiểm tra xem một ô có đang bị tấn công bởi quân đối phương không.
     *
     * @param x     Tọa độ cột của ô cần kiểm tra.
     * @param y     Tọa độ hàng của ô cần kiểm tra.
     * @param board Bàn cờ hiện tại.
     * @return true nếu ô đang bị tấn công, false nếu không.
     */
    private boolean isSquareUnderAttack(int x, int y, ChessTile[][] board) {
        // Duyệt qua tất cả các quân cờ trên bàn cờ
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPiece piece = board[i][j].getPiece();
                if (piece != null && piece.getColor() != this.getColor()) {
                    // Kiểm tra xem quân cờ đối phương có thể tấn công ô (x, y) không
                    if (piece.isValidMove(j, i, x, y, board)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}