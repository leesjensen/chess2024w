package util;

import chess.*;
import com.google.gson.*;

public class ChessSerializer {

    public static void main(String[] args) {
        var board = new ChessBoard();
        board.resetBoard();
        var json = new Gson().toJson(board);
        System.out.println(json);

        var object = new Gson().fromJson(json, ChessBoard.class);
//        var object = createSerializer().fromJson(json, ChessBoard.class);
        System.out.println(object);
    }

    public static class Pawn extends ChessPiece {
        public Pawn(ChessGame.TeamColor color, PieceType type) {
            super(color, PieceType.PAWN);
        }
    }

    public static class Rook extends ChessPiece {
        public Rook(ChessGame.TeamColor color, PieceType type) {
            super(color, PieceType.ROOK);
        }
    }

    public static class Bishop extends ChessPiece {
        public Bishop(ChessGame.TeamColor color) {
            super(color, PieceType.BISHOP);
        }
    }

    public static class Knight extends ChessPiece {
        public Knight(ChessGame.TeamColor color, PieceType type) {
            super(color, PieceType.KNIGHT);
        }
    }

    public static class Queen extends ChessPiece {
        public Queen(ChessGame.TeamColor color, PieceType type) {
            super(color, PieceType.QUEEN);
        }
    }

    public static class King extends ChessPiece {
        public King(ChessGame.TeamColor color, PieceType type) {
            super(color, PieceType.KING);
        }
    }

    public static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(ChessPiece.class,
                (JsonDeserializer<ChessPiece>) (el, type, ctx) -> {
                    ChessPiece chessPiece = null;
                    if (el.isJsonObject()) {
                        String pieceType = el.getAsJsonObject().get("type").getAsString();
                        switch (ChessPiece.PieceType.valueOf(pieceType)) {
                            case PAWN -> chessPiece = ctx.deserialize(el, Pawn.class);
                            case ROOK -> chessPiece = ctx.deserialize(el, Rook.class);
                            case KNIGHT -> chessPiece = ctx.deserialize(el, Knight.class);
                            case BISHOP -> chessPiece = ctx.deserialize(el, Bishop.class);
                            case QUEEN -> chessPiece = ctx.deserialize(el, Queen.class);
                            case KING -> chessPiece = ctx.deserialize(el, King.class);
                        }
                    }
                    return chessPiece;
                });

        return gsonBuilder.create();
    }
}
