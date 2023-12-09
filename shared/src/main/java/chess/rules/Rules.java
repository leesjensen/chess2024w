package chess.rules;

import chess.ChessPiece;

import java.util.HashMap;

import static chess.ChessPiece.PieceType.*;

public class Rules {
    static private final HashMap<ChessPiece.PieceType, MovementRule> rules = new HashMap<>();

    static {
        rules.put(KING, new KingMovementRule());
        rules.put(QUEEN, new QueenMovementRule());
        rules.put(KNIGHT, new KnightMovementRule());
        rules.put(BISHOP, new BishopMovementRule());
        rules.put(ROOK, new RookMovementRule());
        rules.put(PAWN, new PawnMovementRule());
    }

    static public MovementRule movementRule(ChessPiece.PieceType type) {
        return rules.get(type);
    }

}
