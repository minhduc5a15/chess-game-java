package com.minhduc5a12.chess.core.pieces;

import java.util.ArrayList;
import java.util.List;

import com.minhduc5a12.chess.constants.PieceColor;
import com.minhduc5a12.chess.core.model.ChessMove;
import com.minhduc5a12.chess.core.model.ChessPiece;
import com.minhduc5a12.chess.core.model.ChessPosition;
import com.minhduc5a12.chess.utils.BoardUtils;

public class King extends ChessPiece {
    {
        this.pieceValue = Integer.MAX_VALUE;
    }

    public King(PieceColor color) {
        super(color, color.isWhite() ? "white_king.png" : "black_king.png");
    }

    @Override
    public List<ChessMove> generateValidMoves(ChessPosition start, ChessPieceMap pieceMap) {
        List<ChessMove> moves = new ArrayList<>();
        int startRow = start.row();
        int startCol = start.col();
        PieceColor color = getColor();

        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int[] dir : directions) {
            int newCol = startCol + dir[0];
            int newRow = startRow + dir[1];
            if (BoardUtils.isWithinBoard(newCol, newRow)) {
                ChessPosition pos = new ChessPosition(newCol, newRow);
                if (!pieceMap.hasPiece(pos) || (pieceMap.hasPiece(pos) && pieceMap.getPiece(pos).getColor() != color)) {
                    moves.add(new ChessMove(start, pos));
                }
            }
        }

        List<ChessMove> validMoves = new ArrayList<>();

        if (canCastleKingside(start, pieceMap)) {
            moves.add(new ChessMove(start, new ChessPosition(6, start.row())));
        }

        if (canCastleQueenside(start, pieceMap)) {
            moves.add(new ChessMove(start, new ChessPosition(2, start.row())));
        }

        for (ChessMove move : moves) {
            ChessPieceMap tempMap = BoardUtils.simulateMove(move, pieceMap);
            if (!BoardUtils.isKingInCheck(color, tempMap)) {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    public boolean canCastleKingside(ChessPosition start, ChessPieceMap pieceMap) {
        if (BoardUtils.isKingInCheck(getColor(), pieceMap)) {
            return false;
        }

        if (hasMoved()) {
            return false;
        }

        int row = start.row();
        ChessPosition rookPos = new ChessPosition(7, row);
        ChessPiece rook = pieceMap.getPiece(rookPos);

        if (!(rook instanceof Rook) || rook.hasMoved()) {
            return false;
        }

        for (int col = start.col() + 1; col < 7; col++) {
            if (pieceMap.hasPiece(new ChessPosition(col, row))) {
                return false;
            }
        }

        for (int col = start.col(); col <= start.col() + 2; col++) {
            ChessPosition pos = new ChessPosition(col, row);
            ChessPieceMap tempMap = BoardUtils.simulateMove(new ChessMove(start, pos), pieceMap);
            if (BoardUtils.isKingInCheck(getColor(), tempMap)) {
                return false;
            }
        }
        return true;
    }

    public boolean canCastleQueenside(ChessPosition start, ChessPieceMap pieceMap) {
        if (BoardUtils.isKingInCheck(getColor(), pieceMap)) {
            return false;
        }

        if (hasMoved()) {
            return false;
        }

        int row = start.row();
        ChessPosition rookPos = new ChessPosition(0, row);
        ChessPiece rook = pieceMap.getPiece(rookPos);

        if (!(rook instanceof Rook) || rook.hasMoved()) {
            return false;
        }

        for (int col = start.col() - 1; col > 0; col--) {
            if (pieceMap.hasPiece(new ChessPosition(col, row))) {
                return false;
            }
        }

        for (int col = start.col(); col >= start.col() - 2; col--) {
            ChessPosition pos = new ChessPosition(col, row);
            ChessPieceMap tempMap = BoardUtils.simulateMove(new ChessMove(start, pos), pieceMap);
            if (BoardUtils.isKingInCheck(getColor(), tempMap)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getPieceNotation() {
        return this.getColor().isWhite() ? "K" : "k";
    }

    @Override
    public ChessPiece deepCopy() {
        King copy = new King(this.getColor());
        copy.pieceValue = this.pieceValue;
        copy.setHasMoved(this.hasMoved());
        return copy;
    }
}