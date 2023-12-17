package chess;

import com.google.gson.Gson;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor turn;

    public ChessGame() {
        this.board = new ChessBoard();
        this.turn = TeamColor.WHITE;
    }

    public static ChessGame create(String serializedGame) {
        return new Gson().fromJson(serializedGame, ChessGame.class);
    }
    
    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param turn the team whose turn it is
     */
    public void setTeamTurn(TeamColor turn) {
        this.turn = turn;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        var validMoves = new HashSet<ChessMove>();
        var piece = board.getPiece(startPosition);
        if (piece != null) {
            var possibleMoves = piece.pieceMoves(board, startPosition);

            // Make sure none of the possible moves are illegal.
            for (var move : possibleMoves) {
                if (board.isMoveLegal(move)) {
                    validMoves.add(move);
                }
            }
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        var piece = board.getPiece(move.getStartPosition());
        if (piece != null) {
            if (piece.getTeamColor() == turn) {
                var validMoves = piece.pieceMoves(board, move.getStartPosition());
                if (validMoves.contains(move)) {
                    if (board.isMoveLegal(move)) {
                        board.movePiece(move);
                        turn = (turn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
                        return;
                    }
                }
            }
        }

        throw new InvalidMoveException(String.format("Move %s is not valid", move));
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        var king = board.getPlacement(teamColor, ChessPiece.PieceType.KING);

        return king.isAttacked(board);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        var king = board.getPlacement(teamColor, ChessPiece.PieceType.KING);

        // Is the king being attacked?
        if (!king.isAttacked(board)) {
            return false;
        }

        // Try every move on the board and see if it gets us out of this.
        for (var placement : board.collection()) {
            if (placement.getPiece().getTeamColor() == teamColor) {
                for (var move : placement.pieceMoves(board)) {
                    var newBoard = new ChessBoard(board);
                    newBoard.movePiece(move);
                    var kingPlacement = newBoard.getPlacement(teamColor, ChessPiece.PieceType.KING);
                    if (!kingPlacement.isAttacked(newBoard)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // Is the king being attacked?
        var king = board.getPlacement(teamColor, ChessPiece.PieceType.KING);
        if (king.isAttacked(board)) {
            return false;
        }
        // Does the player have any valid moves?
        for (var placement : board.collection()) {
            if (placement.getPiece().getTeamColor() == teamColor) {
                if (!validMoves(placement.getPos()).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var game = (ChessGame) o;

        if (!Objects.equals(board, game.board)) return false;
        return turn == game.turn;
    }

    @Override
    public int hashCode() {
        int result = board != null ? board.hashCode() : 0;
        result = 31 * result + (turn != null ? turn.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
