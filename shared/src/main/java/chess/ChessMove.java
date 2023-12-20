package chess;

import java.util.Locale;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private final ChessPosition start;
    private final ChessPosition end;
    private final ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition start, ChessPosition end,
                     ChessPiece.PieceType promotionPiece) {
        this.start = new ChessPosition(start.getRow(), start.getColumn());
        this.end = new ChessPosition(end.getRow(), end.getColumn());
        this.promotionPiece = promotionPiece;
    }


    public ChessMove(String notation) throws Exception {
        notation = notation.toLowerCase(Locale.ROOT);
        if (notation.length() >= 4) {
            int colStart = notation.charAt(0) - 'a' + 1;
            int rowStart = notation.charAt(1) - '1' + 1;
            int colEnd = notation.charAt(2) - 'a' + 1;
            int rowEnd = notation.charAt(3) - '1' + 1;

            start = new ChessPosition(rowStart, colStart);
            end = new ChessPosition(rowEnd, colEnd);
            if (notation.length() == 5) {
                promotionPiece = switch (notation.charAt(4)) {
                    case 'q' -> ChessPiece.PieceType.QUEEN;
                    case 'b' -> ChessPiece.PieceType.BISHOP;
                    case 'n' -> ChessPiece.PieceType.KNIGHT;
                    case 'r' -> ChessPiece.PieceType.ROOK;
                    default -> null;
                };
            } else {
                promotionPiece = null;
            }
            return;
        }
        throw new Exception("Invalid notation");
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return start;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return end;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessMove move = (ChessMove) o;
        return (start.equals(move.start) && end.equals(move.end) && promotionPiece == move.promotionPiece);
    }

    @Override
    public int hashCode() {
        var promotionCode = (promotionPiece == null ? 9 : promotionPiece.ordinal());
        return (1000 * start.hashCode()) + end.hashCode() + promotionCode;
    }


    @Override
    public String toString() {
        var p = (promotionPiece == null ? "" : ":" + promotionPiece);
        return String.format("%s:%s%s", start.toString(), end.toString(), p);
    }
}
