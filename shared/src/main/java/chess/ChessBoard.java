package chess;

import java.util.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    final private ChessPiece[][] squares = new ChessPiece[8][8];
    final public ArrayList<ChessMove> history = new ArrayList<>();

    public ChessBoard() {
    }


    public ChessBoard(ChessBoard copy) {
        for (var i = 0; i < 8; i++) {
            System.arraycopy(copy.squares[i], 0, squares[i], 0, 8);
        }
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        var pieces = new ChessPiece.PieceType[]{
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };
        for (var i = 0; i < 8; i++) {
            squares[0][i] = new ChessPiece(ChessGame.TeamColor.WHITE, pieces[i]);
            squares[1][i] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            squares[2][i] = null;
            squares[3][i] = null;
            squares[4][i] = null;
            squares[5][i] = null;
            squares[6][i] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
            squares[7][i] = new ChessPiece(ChessGame.TeamColor.BLACK, pieces[i]);
        }
    }

    public boolean isMoveLegal(ChessMove move) {
        var piece = getPiece(move.getStartPosition());

        // Test if this move was a castle, and if so is it valid.
        if (isMoveCastle(piece, move)) {
            if (!isCastleValid(piece, move)) {
                return false;
            }
        }

        // Test if this move causes the team's king to be put in check.
        var newBoard = new ChessBoard(this);
        newBoard.movePiece(move);
        var king = newBoard.getPlacement(piece.getTeamColor(), ChessPiece.PieceType.KING);
        return king == null || !king.isAttacked(newBoard);
    }

    public void movePiece(ChessMove move) {
        var piece = getPiece(move.getStartPosition());

        // Handle promotion
        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }
        // Handle castle
        else if ((piece.getPieceType() == ChessPiece.PieceType.KING) &&
                (Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn()) == 2)) {
            castle(move);
        }
        // Handle en passant
        else if ((piece.getPieceType() == ChessPiece.PieceType.PAWN) &&
                (move.getStartPosition().getColumn() != move.getEndPosition().getColumn()) &&
                (getPiece(move.getEndPosition()) == null)) {
            enPassant(move);
        }

        removePiece(move.getStartPosition());
        addPiece(move.getEndPosition(), piece);

        history.add(move);
    }


    public void enPassant(ChessMove move) {
        var killRow = move.getEndPosition().getRow() == 6 ? 5 : 4;
        var killPosition = new ChessPosition(killRow, move.getEndPosition().getColumn());

        removePiece(killPosition);
    }

    public void castle(ChessMove move) {
        var row = move.getEndPosition().getRow();
        var rookMove = new ChessMove(new ChessPosition(row, 1), new ChessPosition(row, 4), null);
        if (move.getEndPosition().getColumn() == 7) {
            rookMove = new ChessMove(new ChessPosition(row, 8), new ChessPosition(row, 6), null);
        }
        movePiece(rookMove);
    }

    private void removePiece(ChessPosition position) {
        squares[position.getRow() - 1][position.getColumn() - 1] = null;
    }

    public ChessPlacement getPlacement(ChessGame.TeamColor color, ChessPiece.PieceType type) {
        for (var placement : collection()) {
            if (placement.getPiece().getTeamColor() == color && placement.getPiece().getPieceType() == type) {
                return placement;
            }
        }
        return null;
    }


    public boolean isAttacked(ChessPosition targetPos, ChessGame.TeamColor targetColor) {
        return !getAttackers(targetPos, targetColor).isEmpty();
    }

    public Collection<ChessPlacement> collection() {
        var result = new ArrayList<ChessPlacement>();

        for (var i = 0; i < 8; i++) {
            for (var j = 0; j < 8; j++) {
                if (squares[i][j] != null) {
                    result.add(new ChessPlacement(squares[i][j], new ChessPosition(i + 1, j + 1)));
                }
            }
        }
        return result;
    }


    public Collection<ChessPosition> getAttackers(ChessPosition targetPos, ChessGame.TeamColor targetColor) {
        var attackers = new ArrayList<ChessPosition>();

        for (var candidateAttacker : collection()) {
            if (candidateAttacker.getPiece().getTeamColor() != targetColor) {
                var moves = candidateAttacker.pieceMoves(this);
                for (var move : moves) {
                    if (move.getEndPosition().equals(targetPos)) {
                        attackers.add(candidateAttacker.getPos());
                        break;
                    }
                }
            }
        }
        return attackers;
    }

    public List<ChessMove> getHistory() {
        return history;
    }

    public boolean isSquareEmpty(int row, int col) {
        var pieceAt = getPiece(new ChessPosition(row, col));
        return pieceAt == null;
    }


    public ChessMove getLastMove() {
        return history.get(history.size() - 1);
    }

    public boolean isOriginalPosition(ChessPosition pos) {
        for (var bh : getHistory()) {
            if (bh.getStartPosition().equals(pos)) {
                return false;
            }
        }
        return true;
    }


    private boolean isMoveCastle(ChessPiece piece, ChessMove move) {
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            return Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn()) == 2;
        }
        return false;
    }


    private boolean isCastleValid(ChessPiece king, ChessMove move) {
        var color = king.getTeamColor();
        var teamRow = color == ChessGame.TeamColor.BLACK ? 8 : 1;
        // Make sure king not being attacked.
        if (posNotAttacked(teamRow, 5, color)) {
            var castleLeft = move.getEndPosition().getColumn() == 3;
            if (castleLeft) {
                return posNotAttacked(teamRow, 3, color) && posNotAttacked(teamRow, 4, color);
            } else {
                return posNotAttacked(teamRow, 6, color) && posNotAttacked(teamRow, 7, color);
            }
        }
        return false;
    }

    private boolean posNotAttacked(int row, int col, ChessGame.TeamColor color) {
        var pos = new ChessPosition(row, col);
        return !isAttacked(pos, color);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard board = (ChessBoard) o;
        return Arrays.deepEquals(squares, board.squares);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.deepHashCode(squares);
    }


    private static final int BLACK = 0;
    private static final int RED = 1;
    private static final int GREEN = 2;
    private static final int YELLOW = 3;
    private static final int BLUE = 4;
    private static final int MAGENTA = 5;
    private static final int CYAN = 6;
    private static final int WHITE = 7;

    private static final String COLOR_RESET = "\u001b[0m";

    /**
     * Set both the foreground and background color. Foreground is 3, background is 4.
     */
    private static String color(int FG, int BG) {
        return String.format("\u001b[3%d;4%dm", FG, BG);
    }

    /**
     * Set the foreground color.
     */
    private static String color(int FG) {
        return String.format("\u001b[1;3%dm", FG);
    }

    private static final String BORDER = color(BLACK, YELLOW);

    private static final String BOARD_BLACK = color(WHITE, BLACK);
    private static final String BOARD_WHITE = color(BLACK, WHITE);
    private static final String BOARD_HIGHLIGHT = color(GREEN, MAGENTA);

    private static final String BLACK_PIECE = color(RED);
    private static final String WHITE_PIECE = color(GREEN);

    private static final Map<ChessPiece.PieceType, String> pieceMap = Map.of(
            ChessPiece.PieceType.KING, "K",
            ChessPiece.PieceType.QUEEN, "Q",
            ChessPiece.PieceType.BISHOP, "B",
            ChessPiece.PieceType.KNIGHT, "N",
            ChessPiece.PieceType.ROOK, "R",
            ChessPiece.PieceType.PAWN, "P"
    );

    @Override
    public String toString() {
        return toString(ChessGame.TeamColor.WHITE, null);
    }


    public String toString(ChessGame.TeamColor playerColor, Collection<ChessPosition> highlights) {
        var sb = new StringBuilder();
        try {
            var currentSquare = BOARD_WHITE;
            var rows = new int[]{7, 6, 5, 4, 3, 2, 1, 0};
            var columns = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
            var columnsLetters = "    a  b  c  d  e  f  g  h    ";
            if (playerColor == ChessGame.TeamColor.BLACK) {
                columnsLetters = "    h  g  f  e  d  c  b  a    ";
                rows = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
                columns = new int[]{7, 6, 5, 4, 3, 2, 1, 0};
            }
            sb.append(BORDER).append(columnsLetters).append(COLOR_RESET).append("\n");
            for (var i : rows) {
                var row = " " + (i + 1) + " ";
                sb.append(BORDER).append(row).append(COLOR_RESET);
                for (var j : columns) {
                    var squareColor = currentSquare;
                    if (highlights != null && highlights.contains(new ChessPosition(i + 1, j + 1))) {
                        squareColor = BOARD_HIGHLIGHT;
                    }
                    var piece = squares[i][j];
                    if (piece != null) {
                        var color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_PIECE : BLACK_PIECE;
                        var p = pieceMap.get(piece.getPieceType());
                        sb.append(squareColor).append(color).append(" ").append(p).append(" ").append(COLOR_RESET);
                    } else {
                        sb.append(squareColor).append("   ").append(COLOR_RESET);
                    }
                    currentSquare = currentSquare.equals(BOARD_BLACK) ? BOARD_WHITE : BOARD_BLACK;
                }
                sb.append(BORDER).append(row).append(COLOR_RESET);
                sb.append('\n');
                currentSquare = currentSquare.equals(BOARD_BLACK) ? BOARD_WHITE : BOARD_BLACK;
            }
            sb.append(BORDER).append(columnsLetters).append(COLOR_RESET).append("\n");
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return sb.toString();
    }
}
