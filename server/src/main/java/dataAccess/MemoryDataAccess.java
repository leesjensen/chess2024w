package dataAccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * In memory representation of @DataAccess.
 */
public class MemoryDataAccess implements DataAccess {

    private int nextID = 1000;
    final private Map<String, UserData> users = new HashMap<>();
    final private Map<String, AuthData> auths = new HashMap<>();
    final private Map<Integer, GameData> games = new HashMap<>();

    public MemoryDataAccess() {
    }

    public void clear() {
        users.clear();
        auths.clear();
        games.clear();
    }

    public UserData writeUser(UserData user) throws DataAccessException {
        if (users.get(user.username()) == null) {
            users.put(user.username(), user);
            return user;
        }

        throw new DataAccessException("duplicate");
    }

    public UserData readUser(String username) {
        return users.get(username);
    }

    public AuthData writeAuth(String username) {
        var auth = new AuthData(AuthData.generateToken(), username);
        auths.put(auth.authToken(), auth);
        return auth;
    }

    public AuthData readAuth(String authToken) {
        return auths.get(authToken);
    }

    public void deleteAuth(String authToken) {
        auths.remove(authToken);
    }

    public GameData newGame(String gameName) {
        var gameID = nextID++;
        var gameData = new GameData(gameID, null, null, gameName, new ChessGame(), GameData.State.UNDECIDED);
        games.put(gameData.gameID(), gameData);
        gameData.game().getBoard().resetBoard();
        gameData.game().setTeamTurn(ChessGame.TeamColor.WHITE);
        return gameData;
    }

    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }

    public GameData readGame(int gameID) {
        return games.get(gameID);
    }

    public Collection<GameData> listGames() {
        return games.values();
    }


    public String description() {
        return "Memory Database";
    }
}

