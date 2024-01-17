package chess.rules;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class KingMovementRule extends MovementRule {
    @Override
    public Collection<ChessMove> moves(ChessBoard board, ChessPosition position) {
        var moves = new ArrayList<ChessMove>();
        calculateMoves(board, position, -1, 0, moves, false);
        calculateMoves(board, position, 1, 0, moves, false);
        calculateMoves(board, position, 0, 1, moves, false);
        calculateMoves(board, position, 0, -1, moves, false);
        calculateMoves(board, position, -1, -1, moves, false);
        calculateMoves(board, position, 1, 1, moves, false);
        calculateMoves(board, position, -1, 1, moves, false);
        calculateMoves(board, position, 1, -1, moves, false);

        addCastleMoves(board, position, moves);

        return moves;
    }


    void addCastleMoves(ChessBoard board, ChessPosition pos, Collection<ChessMove> moves) {
        var king = board.getPiece(pos);
        var color = king.getTeamColor();
        var teamRow = color == ChessGame.TeamColor.BLACK ? 8 : 1;

        var kingPos = new ChessPosition(teamRow, 5);
        if (king.equals(board.getPiece(kingPos)) && board.isOriginalPosition(kingPos)) {
            if (board.isOriginalPosition(new ChessPosition(teamRow, 8)) &&
                    board.isSquareEmpty(teamRow, 6) &&
                    board.isSquareEmpty(teamRow, 7)) {
                moves.add(new ChessMove(pos, new ChessPosition(teamRow, 7), null));
            }
            if (board.isOriginalPosition(new ChessPosition(teamRow, 1)) &&
                    board.isSquareEmpty(teamRow, 2) &&
                    board.isSquareEmpty(teamRow, 3) &&
                    board.isSquareEmpty(teamRow, 4)) {
                moves.add(new ChessMove(pos, new ChessPosition(teamRow, 3), null));
            }
        }
    }

}
