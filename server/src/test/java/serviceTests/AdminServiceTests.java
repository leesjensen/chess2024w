package serviceTests;

import dataAccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.AdminService;
import service.AuthService;
import service.GameService;
import service.UserService;
import util.CodedException;

public class AdminServiceTests {

    @Test
    public void clear() throws Exception {
        var memoryDataAccess = new MemoryDataAccess();
        var userService = new UserService(memoryDataAccess);
        var user = new UserData("juan", "too many secrets", "juan@byu.edu");
        var authData = userService.registerUser(user);

        var gameService = new GameService(memoryDataAccess);
        gameService.createGame("testGame");

        var service = new AdminService(memoryDataAccess);
        Assertions.assertDoesNotThrow(service::clearApplication);

        var authService = new AuthService(memoryDataAccess);
        Assertions.assertThrows(CodedException.class, () -> authService.createSession(user));
        Assertions.assertNull(authService.getAuthData(authData.authToken()));

        var games = gameService.listGames();
        Assertions.assertEquals(0, games.size());
    }
}
