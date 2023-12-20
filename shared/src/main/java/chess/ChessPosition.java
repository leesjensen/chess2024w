package chess;

import java.util.Locale;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }


    public ChessPosition(String notation) throws Exception {
        notation = notation.toLowerCase(Locale.ROOT);
        if (notation.length() == 2) {
            col = notation.charAt(0) - 'a' + 1;
            row = notation.charAt(1) - '1' + 1;
            return;
        }
        throw new Exception("Invalid notation");
    }


    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return col;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var position = (ChessPosition) o;
        return (row == position.row && col == position.col);
    }

    @Override
    public int hashCode() {
        return ((100 * row) + (10 * col));
    }


    @Override
    public String toString() {
        return String.format("%d%d", row, col);
    }
}
