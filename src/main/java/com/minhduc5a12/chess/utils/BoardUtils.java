package com.minhduc5a12.chess.utils;

import com.minhduc5a12.chess.ChessPiece;
import com.minhduc5a12.chess.ChessTile;
import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.pieces.*;

public class BoardUtils {
    // Kích thước bàn cờ
    public static final int BOARD_SIZE = 8;

    /**
     * In ra trạng thái hiện tại của bàn cờ.
     *
     * @param board Bàn cờ cần in.
     */
    public static void printBoard(ChessTile[][] board) {
        System.out.println("Current Board State:");
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                ChessPiece piece = board[y][x].getPiece();
                if (piece != null) {
                    // In ký hiệu quân cờ (ví dụ: P = Tốt, R = Xe, N = Mã, B = Tượng, Q = Hậu, K = Vua)
                    String pieceSymbol = getPieceSymbol(piece);
                    System.out.print(pieceSymbol + " ");
                } else {
                    System.out.print(". "); // Ô trống
                }
            }
            System.out.println(); // Xuống dòng sau mỗi hàng
        }
        System.out.println(); // Dòng trống để phân cách
    }

    /**
     * Trả về ký hiệu của quân cờ.
     *
     * @param piece Quân cờ cần lấy ký hiệu.
     * @return Ký hiệu của quân cờ.
     */
    private static String getPieceSymbol(ChessPiece piece) {
        if (piece instanceof Pawn) return piece.getColor() == PieceColor.WHITE ? "P" : "p";
        if (piece instanceof Rook) return piece.getColor() == PieceColor.WHITE ? "R" : "r";
        if (piece instanceof Knight) return piece.getColor() == PieceColor.WHITE ? "N" : "n";
        if (piece instanceof Bishop) return piece.getColor() == PieceColor.WHITE ? "B" : "b";
        if (piece instanceof Queen) return piece.getColor() == PieceColor.WHITE ? "Q" : "q";
        if (piece instanceof King) return piece.getColor() == PieceColor.WHITE ? "K" : "k";

        return "?";
    }


    /**
     * Kiểm tra xem tọa độ (x, y) có nằm trong phạm vi bàn cờ không.
     *
     * @param x Tọa độ cột (0 đến 7).
     * @param y Tọa độ hàng (0 đến 7).
     * @return true nếu tọa độ hợp lệ, false nếu không.
     */
    public static boolean isWithinBoard(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }

    /**
     * Chuyển đổi nước đi thành ký hiệu cờ vua tiêu chuẩn (algebraic notation).
     *
     * @param piece       Quân cờ đang di chuyển.
     * @param startX      Tọa độ cột ban đầu.
     * @param startY      Tọa độ hàng ban đầu.
     * @param endX        Tọa độ cột đích.
     * @param endY        Tọa độ hàng đích.
     * @param targetPiece Quân cờ ở ô đích (nếu có).
     * @param board       Bàn cờ hiện tại.
     * @return Chuỗi ký hiệu cờ vua tiêu chuẩn.
     */
    public static String toAlgebraicNotation(ChessPiece piece, int startX, int startY, int endX, int endY, ChessPiece targetPiece, ChessTile[][] board) {
        StringBuilder notation = new StringBuilder();

        // Ký hiệu quân cờ (trừ Tốt)
        if (!(piece instanceof Pawn)) {
            // Xử lý riêng cho Knight (Mã) để tránh trùng với King
            if (piece instanceof Knight) {
                notation.append('N'); // Knight được ký hiệu là 'N'
            } else {
                notation.append(piece.getClass().getSimpleName().charAt(0)); // Lấy chữ cái đầu tiên của tên quân cờ
            }
        }

        // Ký hiệu bắt quân
        if (targetPiece != null) {
            if (piece instanceof Pawn) {
                notation.append((char) ('a' + startX)); // Thêm cột xuất phát của Tốt khi bắt quân
            }
            notation.append('x');
        }

        // Ô đích
        notation.append((char) ('a' + endX));
        notation.append(8 - endY);

        // Kiểm tra chiếu tướng hoặc chiếu bí
        PieceColor opponentColor = (piece.getColor() == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        if (isKingInCheck(board, opponentColor)) {
            if (isCheckmate(board, opponentColor)) {
                notation.append('#'); // Chiếu bí
            } else {
                notation.append('+'); // Chiếu tướng
            }
        }

        return notation.toString();
    }

    /**
     * Kiểm tra xem Vua của một bên có đang bị chiếu không.
     *
     * @param board Bàn cờ hiện tại.
     * @param color Màu của Vua cần kiểm tra.
     * @return true nếu Vua đang bị chiếu, false nếu không.
     */
    public static boolean isKingInCheck(ChessTile[][] board, PieceColor color) {
        // Tìm vị trí của Vua
        int kingX = -1, kingY = -1;
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                ChessPiece piece = board[y][x].getPiece();
                if (piece instanceof King && piece.getColor() == color) {
                    kingX = x;
                    kingY = y;
                    break;
                }
            }
            if (kingX != -1) break; // Thoát vòng lặp nếu đã tìm thấy Vua
        }

        // Nếu không tìm thấy Vua, trả về false (không thể xảy ra trong cờ vua tiêu chuẩn)
        if (kingX == -1) {
            return false;
        }

        // Kiểm tra xem có quân cờ đối phương nào tấn công ô của Vua không
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                ChessPiece piece = board[y][x].getPiece();
                if (piece != null && piece.getColor() != color) {
                    // Kiểm tra xem quân cờ đối phương có thể tấn công Vua không
                    if (piece.isValidMove(x, y, kingX, kingY, board)) {
                        return true;
                    }
                }
            }
        }

        return false; // Vua không bị chiếu
    }

    /**
     * Kiểm tra xem Vua của một bên có đang bị chiếu bí không.
     *
     * @param board Bàn cờ hiện tại.
     * @param color Màu của Vua cần kiểm tra.
     * @return true nếu Vua đang bị chiếu bí, false nếu không.
     */
    public static boolean isCheckmate(ChessTile[][] board, PieceColor color) {
        // Kiểm tra xem Vua có đang bị chiếu không
        if (!isKingInCheck(board, color)) {
            return false;
        }

        // Tìm tất cả các nước đi hợp lệ của Vua và các quân cờ khác
        for (int y = 0; y < BOARD_SIZE; y++) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                ChessPiece piece = board[y][x].getPiece();
                if (piece != null && piece.getColor() == color) {
                    for (int destY = 0; destY < BOARD_SIZE; destY++) {
                        for (int destX = 0; destX < BOARD_SIZE; destX++) {
                            if (piece.isValidMove(x, y, destX, destY, board)) {
                                // Thử di chuyển và kiểm tra xem Vua còn bị chiếu không
                                ChessPiece targetPiece = board[destY][destX].getPiece();
                                board[destY][destX].setPiece(piece);
                                board[y][x].setPiece(null);

                                boolean stillInCheck = isKingInCheck(board, color);

                                // Hoàn tác nước đi
                                board[y][x].setPiece(piece);
                                board[destY][destX].setPiece(targetPiece);

                                if (!stillInCheck) {
                                    return false; // Có ít nhất một nước đi hợp lệ để thoát chiếu
                                }
                            }
                        }
                    }
                }
            }
        }

        return true; // Không có nước đi nào để thoát chiếu (chiếu bí)
    }

    /**
     * Kiểm tra xem nước đi có thoát chiếu không.
     *
     * @param startX       Tọa độ cột ban đầu.
     * @param startY       Tọa độ hàng ban đầu.
     * @param endX         Tọa độ cột đích.
     * @param endY         Tọa độ hàng đích.
     * @param board        Bàn cờ hiện tại.
     * @param currentColor Màu của người chơi hiện tại.
     * @return true nếu nước đi thoát chiếu, false nếu không.
     */
    public static boolean isMoveValidUnderCheck(int startX, int startY, int endX, int endY, ChessTile[][] board, PieceColor currentColor) {
        // Lưu trạng thái hiện tại của bàn cờ
        ChessPiece originalPiece = board[endY][endX].getPiece();

        // Thử di chuyển quân cờ
        board[endY][endX].setPiece(board[startY][startX].getPiece());
        board[startY][startX].setPiece(null);

        // Kiểm tra xem Vua có còn bị chiếu không
        boolean isKingInCheck = isKingInCheck(board, currentColor);

        // Hoàn tác nước đi
        board[startY][startX].setPiece(board[endY][endX].getPiece());
        board[endY][endX].setPiece(originalPiece);

        // Nếu Vua không còn bị chiếu, nước đi hợp lệ
        return !isKingInCheck;
    }
}