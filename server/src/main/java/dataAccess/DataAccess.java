package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

/**
 * Represents all operations that may be performed on the database.
 */
public interface DataAccess {
    /**
     * Clears out all the data in the database.
     *
     * @throws DataAccessException for database or sql query violations (e.g. no error for not found).
     */
    void clear() throws DataAccessException;

    /**
     * Persist a user
     *
     * @param user with both username and ID provided.
     * @throws DataAccessException for database or sql query violations.
     */
    UserData writeUser(UserData user) throws DataAccessException;

    /**
     * Read a previously persisted user.
     *
     * @param userName for the desired user.
     * @return The requested @User
     * @throws DataAccessException for database or sql query violations (e.g. no error for not found).
     */
    UserData readUser(String userName) throws DataAccessException;

    /**
     * Persist the authorization token. If a token already exists in the database it is overwritten.
     *
     * @param username to persist.
     * @return The @AuthData for the user.
     * @throws DataAccessException for database or sql query violations.
     */
    AuthData writeAuth(String username) throws DataAccessException;

    /**
     * Read a previously persisted authorization token.
     *
     * @param authToken for the @AuthData to retrieve.
     * @return The @AuthToken for the user or Null if it doesn't exist.
     * @throws DataAccessException for database or sql query violations (e.g. no error for not found).
     */
    AuthData readAuth(String authToken) throws DataAccessException;

    /**
     * Clears out an authorization token. This call is idempotent.
     *
     * @param authToken to delete.
     * @throws DataAccessException for database or sql query violations.
     */
    void deleteAuth(String authToken) throws DataAccessException;

    /**
     * Creates a new game. A new gameID is assigned to the returned object.
     *
     * @param gameName to create
     * @throws DataAccessException for database or sql query violations.
     */
    GameData newGame(String gameName) throws DataAccessException;

    /**
     * Update an existing game.
     *
     * @param game to update
     * @throws DataAccessException for database or sql query violations.
     */
    void updateGame(GameData game) throws DataAccessException;

    /**
     * Read a previously persisted Game.
     *
     * @param gameID for the game to read
     * @return The requested Game or null if not found.
     * @throws DataAccessException for database or sql query violations (e.g. no error for not found).
     */
    GameData readGame(int gameID) throws DataAccessException;

    /**
     * The complete list of games. Since we don't delete games this will be the full list unless the clear operation is called.
     *
     * @return the list of @Game objects
     * @throws DataAccessException for database or sql query violations.
     */
    Collection<GameData> listGames() throws DataAccessException;

    String description();
}

