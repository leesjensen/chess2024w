package service;

import chess.ChessGame;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.GameData;
import util.CodedException;

import java.util.Collection;

/**
 * Provides endpoints for manipulating games.
 * <p>[GET] /game - Lists games
 * <p>[POST] / game - Create game
 * <p>[PUT] / - Join game
 */
public class GameService {

    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * List all the games. This includes pending, active and completed games.
     *
     * @return the collection of games.
     */
    public Collection<GameData> listGames() throws CodedException {
        try {
            return dataAccess.listGames();
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Server error");
        }
    }

    /**
     * Creates a new game.
     *
     * @param gameName to create
     * @return the newly created game.
     */
    public GameData createGame(String gameName) throws CodedException {
        try {
            return dataAccess.newGame(gameName);
        } catch (DataAccessException ex) {
            throw new CodedException(500, "Server error");
        }
    }

    /**
     * Verifies that the specified game exists, and, if a color is specified,
     * adds the caller as the requested color to the game. If no color is specified
     * the user is joined as an observer. This request is idempotent.
     *
     * @param username joining the game.
     * @param color    to join the game as. If null then the user is joined as an observer.
     * @return the updated game.
     */
    public GameData joinGame(String username, ChessGame.TeamColor color, int gameID) throws CodedException {
        try {
            var gameData = dataAccess.readGame(gameID);
            if (gameData == null) {
                throw new CodedException(400, "Unknown game");
            } else if (color == null) {
                return gameData;
            } else if (gameData.isGameOver()) {
                throw new CodedException(403, "Game is over");
            } else {
                if (color == ChessGame.TeamColor.WHITE) {
                    if (gameData.whiteUsername() == null || gameData.whiteUsername().equals(username)) {
                        gameData = gameData.setWhite(username);
                    } else {
                        throw new CodedException(403, "Color taken");
                    }
                } else if (color == ChessGame.TeamColor.BLACK) {
                    if (gameData.blackUsername() == null || gameData.blackUsername().equals(username)) {
                        gameData = gameData.setBlack(username);
                    } else {
                        throw new CodedException(403, "Color taken");
                    }
                }
                dataAccess.updateGame(gameData);
            }
            return gameData;
        } catch (DataAccessException ignored) {
            throw new CodedException(500, "Server error");
        }
    }

}
