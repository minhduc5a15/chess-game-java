package com.minhduc5a12.chess;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.utils.BoardUtils;
import com.minhduc5a12.chess.utils.SoundPlayer;
import com.minhduc5a12.chess.pieces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChessBoard extends JPanel {
    private static final int BOARD_SIZE = 8;
    private static final int TILE_SIZE = 100;

    private static final Color BLACK_COLOR = new Color(0x629f53);
    private static final Color WHITE_COLOR = new Color(0xe8f0d0);

    private final ChessTile[][] tiles = new ChessTile[BOARD_SIZE][BOARD_SIZE];

    private ChessTile selectedTile = null;

    private final List<ChessTile> validMoves = new ArrayList<>();

    private PieceColor currentPlayerColor = PieceColor.WHITE;

    private Consumer<PieceColor> onTurnChange;

    public ChessBoard() {
        setPreferredSize(new Dimension(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE));
        setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        initializeTiles();
        initializePieces();
        addMouseListener(new ChessMouseListener());
    }

    private void initializeTiles() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Color tileColor = (row + col) % 2 == 0 ? WHITE_COLOR : BLACK_COLOR;
                tiles[row][col] = new ChessTile(tileColor, row, col);
                add(tiles[row][col]);
            }
        }
    }

    private void initializePieces() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            tiles[6][i].setPiece(new Pawn(PieceColor.WHITE, "images/white_pawn.png"));
        }

        // Thêm quân tốt đen
        for (int i = 0; i < BOARD_SIZE; i++) {
            tiles[1][i].setPiece(new Pawn(PieceColor.BLACK, "images/black_pawn.png"));
        }

        // Thêm quân Xe trắng
        tiles[7][0].setPiece(new Rook(PieceColor.WHITE, "images/white_rook.png"));
        tiles[7][7].setPiece(new Rook(PieceColor.WHITE, "images/white_rook.png"));

        // Thêm quân Xe đen
        tiles[0][0].setPiece(new Rook(PieceColor.BLACK, "images/black_rook.png"));
        tiles[0][7].setPiece(new Rook(PieceColor.BLACK, "images/black_rook.png"));

        // Thêm quân Mã trắng
        tiles[7][1].setPiece(new Knight(PieceColor.WHITE, "images/white_knight.png"));
        tiles[7][6].setPiece(new Knight(PieceColor.WHITE, "images/white_knight.png"));

        // Thêm quân Mã đen
        tiles[0][1].setPiece(new Knight(PieceColor.BLACK, "images/black_knight.png"));
        tiles[0][6].setPiece(new Knight(PieceColor.BLACK, "images/black_knight.png"));

        // Thêm quân Tượng trắng
        tiles[7][2].setPiece(new Bishop(PieceColor.WHITE, "images/white_bishop.png"));
        tiles[7][5].setPiece(new Bishop(PieceColor.WHITE, "images/white_bishop.png"));

        // Thêm quân Tượng đen
        tiles[0][2].setPiece(new Bishop(PieceColor.BLACK, "images/black_bishop.png"));
        tiles[0][5].setPiece(new Bishop(PieceColor.BLACK, "images/black_bishop.png"));

        // Thêm quân Hậu trắng
        tiles[7][3].setPiece(new Queen(PieceColor.WHITE, "images/white_queen.png"));

        // Thêm quân Hậu đen
        tiles[0][3].setPiece(new Queen(PieceColor.BLACK, "images/black_queen.png"));

        // Thêm quân Vua trắng
        tiles[7][4].setPiece(new King(PieceColor.WHITE, "images/white_king.png"));

        // Thêm quân Vua đen
        tiles[0][4].setPiece(new King(PieceColor.BLACK, "images/black_king.png"));
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                tiles[row][col].paintComponent(g);
            }
        }

        g.setColor(Color.BLACK);
        for (ChessTile tile : validMoves) {
            int x = tile.getCol() * TILE_SIZE + TILE_SIZE / 2 - 5; // Tọa độ x của hình tròn
            int y = tile.getRow() * TILE_SIZE + TILE_SIZE / 2 - 5; // Tọa độ y của hình tròn
            g.fillOval(x, y, 10, 10); // Vẽ hình tròn nhỏ
        }
    }

    private class ChessMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int col = e.getX() / TILE_SIZE;
            int row = e.getY() / TILE_SIZE;

            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                ChessTile clickedTile = tiles[row][col];

                if (selectedTile == null) {
                    // Chọn ô A (ô chứa quân cờ)
                    if (clickedTile.getPiece() != null && clickedTile.getPiece().getColor() == currentPlayerColor) {
                        selectedTile = clickedTile;
                        selectedTile.setSelected(true); // Đánh dấu ô A được chọn

                        // Tính toán các ô hợp lệ
                        validMoves.clear();
                        for (int r = 0; r < BOARD_SIZE; r++) {
                            for (int c = 0; c < BOARD_SIZE; c++) {
                                if (selectedTile.getPiece().isValidMove(selectedTile.getCol(), selectedTile.getRow(), c, r, tiles)) {
                                    // Kiểm tra xem nước đi này có thoát chiếu không
                                    if (BoardUtils.isMoveValidUnderCheck(selectedTile.getCol(), selectedTile.getRow(), c, r, tiles, currentPlayerColor)) {
                                        validMoves.add(tiles[r][c]);
                                    }
                                }
                            }
                        }
                        repaint(); // Vẽ lại bàn cờ để hiển thị các ô hợp lệ
                    }
                } else {
                    // Chọn ô B (ô đích)
                    ChessPiece selectedPiece = selectedTile.getPiece();

                    // Kiểm tra nước đi hợp lệ
                    if (selectedPiece.isValidMove(selectedTile.getCol(), selectedTile.getRow(), col, row, tiles)) {
                        // Kiểm tra xem nước đi này có thoát chiếu không
                        if (BoardUtils.isMoveValidUnderCheck(selectedTile.getCol(), selectedTile.getRow(), col, row, tiles, currentPlayerColor)) {
                            // Kiểm tra xem nước đi có phải là nhập thành không
                            boolean isCastling = selectedPiece instanceof King && Math.abs(col - selectedTile.getCol()) == 2;
                            // Phát âm thanh tương ứng
                            if (isCastling) {
                                SoundPlayer.playCastleSound(); // Phát âm thanh nhập thành
                            } else {
                                // Kiểm tra xem có quân cờ ở ô đích không (ăn quân)
                                ChessPiece targetPiece = clickedTile.getPiece();
                                if (targetPiece != null) {
                                    SoundPlayer.playCaptureSound(); // Phát âm thanh khi ăn quân
                                } else {
                                    SoundPlayer.playMoveSound(); // Phát âm thanh khi di chuyển quân
                                }
                            }

                            // Di chuyển quân cờ
                            clickedTile.setPiece(selectedPiece);
                            selectedTile.setPiece(null);

                            // Đánh dấu quân cờ đã di chuyển
                            selectedPiece.setHasMoved(true);

                            // Xử lý nhập thành (Castling)
                            if (isCastling) {
                                performCastling(selectedTile.getRow(), col);
                                System.out.println(col > selectedTile.getCol() ? "O-O" : "O-O-O"); // In nhập thành
                            }

                            // Đổi lượt cho người chơi tiếp theo
                            currentPlayerColor = (currentPlayerColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

                            // Cập nhật lượt đi trên giao diện
                            if (onTurnChange != null) {
                                onTurnChange.accept(currentPlayerColor);
                            }

                            // In ra bàn cờ để kiểm tra
                            BoardUtils.printBoard(tiles);
                        } else {
                            System.out.println("Invalid move: King is still in check!");
                        }
                    }

                    // Bỏ đánh dấu ô A và xóa danh sách các ô hợp lệ
                    selectedTile.setSelected(false);
                    selectedTile = null;
                    validMoves.clear();
                    repaint(); // Vẽ lại bàn cờ
                }
            }
        }

        private void performCastling(int row, int kingEndX) {
            int direction = (kingEndX > 4) ? 1 : -1; // Hướng di chuyển (cánh Vua hoặc cánh Hậu)
            int rookStartX = (direction == 1) ? 7 : 0; // Vị trí Xe ban đầu
            int rookEndX = (direction == 1) ? kingEndX - 1 : kingEndX + 1; // Vị trí Xe sau khi nhập thành

            // Di chuyển Xe
            ChessTile rookStartTile = tiles[row][rookStartX];
            ChessPiece rook = rookStartTile.getPiece();
            tiles[row][rookEndX].setPiece(rook);
            rookStartTile.setPiece(null);

            // Đánh dấu Xe đã di chuyển
            rook.setHasMoved(true);

        }
    }

    // Phương thức để thiết lập callback cập nhật lượt đi
    public void setOnTurnChange(Consumer<PieceColor> onTurnChange) {
        this.onTurnChange = onTurnChange;
    }
}