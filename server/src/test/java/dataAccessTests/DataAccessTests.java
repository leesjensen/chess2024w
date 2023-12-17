package dataAccessTests;

import dataAccess.DataAccess;
import dataAccess.DbInfo;
import dataAccess.MemoryDataAccess;
import dataAccess.MySqlDataAccess;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

public class DataAccessTests {

    private DataAccess startDB(Class<? extends DataAccess> databaseClass) throws Exception {
        DataAccess db;
        if (MySqlDataAccess.class.equals(databaseClass)) {
            var dataInfo = new DbInfo("test", "admin", "monkeypie", "jdbc:mysql://localhost:3306");
            db = new MySqlDataAccess(dataInfo);
        } else {
            db = new MemoryDataAccess();
        }
        db.clear();
        return db;
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    public void writeReadUser(Class<? extends DataAccess> dbClass) throws Exception {
        DataAccess db = startDB(dbClass);
        var user = new UserData("juan", "too many secrets", "juan@byu.edu");

        Assertions.assertEquals(user, db.writeUser(user));
        Assertions.assertEquals(user, db.readUser(user.username()));
    }

    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    public void writeReadAuth(Class<? extends DataAccess> dbClass) throws Exception {
        DataAccess db = startDB(dbClass);
        var user = new UserData("juan", "too many secrets", "juan@byu.edu");

        var authData = db.writeAuth(user.username());
        Assertions.assertEquals(user.username(), authData.username());
        Assertions.assertFalse(authData.authToken().isEmpty());

        var returnedAuthData = db.readAuth(authData.authToken());
        Assertions.assertEquals(user.username(), returnedAuthData.username());
        Assertions.assertEquals(authData.authToken(), returnedAuthData.authToken());

        var secondAuthData = db.writeAuth(user.username());
        Assertions.assertEquals(user.username(), secondAuthData.username());
        Assertions.assertNotEquals(authData.authToken(), secondAuthData.authToken());
    }


    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    public void writeReadGame(Class<? extends DataAccess> dbClass) throws Exception {
        DataAccess db = startDB(dbClass);

        var game = db.newGame("blitz");
        var updatedGame = game.setBlack("joe");
        db.updateGame(updatedGame);

        var retrievedGame = db.readGame(game.gameID());
        Assertions.assertEquals(retrievedGame, updatedGame);
    }


    @ParameterizedTest
    @ValueSource(classes = {MySqlDataAccess.class, MemoryDataAccess.class})
    public void listGame(Class<? extends DataAccess> dbClass) throws Exception {
        DataAccess db = startDB(dbClass);

        var games = List.of(db.newGame("blitz"), db.newGame("fisher"), db.newGame("lightning"));
        var returnedGames = db.listGames();
        Assertions.assertIterableEquals(games, returnedGames);
    }
}
