package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class QueenMovementRule extends MovementRule {
    @Override
    public Collection<ChessMove> moves(ChessBoard board, ChessPosition position) {
        var moves = new HashSet<ChessMove>();
        calculateMoves(board, position, -1, 0, moves, true);
        calculateMoves(board, position, 1, 0, moves, true);
        calculateMoves(board, position, 0, 1, moves, true);
        calculateMoves(board, position, 0, -1, moves, true);
        calculateMoves(board, position, -1, -1, moves, true);
        calculateMoves(board, position, 1, 1, moves, true);
        calculateMoves(board, position, -1, 1, moves, true);
        calculateMoves(board, position, 1, -1, moves, true);
        return moves;
    }
}
