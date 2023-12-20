package clientTests;

import chess.*;
import org.junit.jupiter.api.*;
import server.Server;
import util.ResponseException;
import ui.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @BeforeEach
    void clearDatabase() throws Exception {
        facade.clear();
    }
    
    @Test
    void register() throws Exception {
        var authData = facade.register("joe", "password", "joe@email.com");

        assertTrue(authData.authToken().length() > 10);
    }


    @Test
    void logoutLogin() throws Exception {
        var registerAuthData = facade.register("joe", "password", "joe@email.com");

        facade.logout(registerAuthData.authToken());
        assertThrows(ResponseException.class, () -> facade.listGames(registerAuthData.authToken()));

        var loginAuthData = facade.login("joe", "password");
        facade.listGames(loginAuthData.authToken());
    }


    @Test
    void createGame() throws Exception {
        var authData = facade.register("joe", "password", "joe@email.com");

        var game = facade.createGame(authData.authToken(), "blitz");

        assertTrue(game.gameID() > 0);
        game = facade.joinGame(authData.authToken(), game.gameID(), ChessGame.TeamColor.WHITE);
        assertEquals(new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK), game.game().getBoard().getPiece(new ChessPosition(1, 1)));
    }


    @Test
    void listGames() throws Exception {
        var authData = facade.register("joe", "password", "joe@email.com");

        var games = facade.listGames(authData.authToken());
        assertEquals(0, games.length);

        facade.createGame(authData.authToken(), "blitz");

        games = facade.listGames(authData.authToken());
        assertEquals(1, games.length);
        assertEquals("blitz", games[0].gameName());
    }

    @Test
    void joinGames() throws Exception {
        var authData = facade.register("joe", "password", "joe@email.com");

        var game = facade.createGame(authData.authToken(), "blitz");
        facade.joinGame(authData.authToken(), game.gameID(), ChessGame.TeamColor.WHITE);

        var games = facade.listGames(authData.authToken());
        assertEquals(1, games.length);
        assertEquals("blitz", games[0].gameName());
        assertEquals(game.gameID(), games[0].gameID());
        assertEquals(authData.username(), games[0].whiteUsername());
    }


}
