package server;

import com.google.gson.Gson;
import dataAccess.DataAccess;
import dataAccess.MemoryDataAccess;
import model.*;
import service.*;
import spark.*;
import util.CodedException;

import java.util.*;
import java.util.logging.Logger;

public class Server {
    DataAccess dataAccess;
    UserService userService;
    GameService gameService;
    AdminService adminService;
    AuthService authService;

    public static final Logger log = Logger.getLogger("chess");

    public Server() {
    }

    public int run(int desiredPort, String dbConnectionUrl) {
        try {
            dataAccess = new MemoryDataAccess();

            userService = new UserService(dataAccess);
            gameService = new GameService(dataAccess);
            adminService = new AdminService(dataAccess);
            authService = new AuthService(dataAccess);

            Spark.port(desiredPort);
            Spark.externalStaticFileLocation("web");

            Spark.delete("/db", this::clearApplication);
            Spark.post("/user", this::registerUser);
            Spark.post("/session", this::createSession);
            Spark.delete("/session", this::deleteSession);
            Spark.get("/game", this::listGames);
            Spark.post("/game", this::createGame);
            Spark.put("/game", this::joinGame);
            Spark.afterAfter(this::log);


            Spark.exception(CodedException.class, this::errorHandler);
            Spark.exception(Exception.class, (e, req, res) -> errorHandler(new CodedException(500, e.getMessage()), req, res));
            Spark.notFound((req, res) -> {
                var msg = String.format("[%s] %s not found", req.requestMethod(), req.pathInfo());
                return errorHandler(new CodedException(404, msg), req, res);
            });

        } catch (Exception ex) {
            System.out.printf("Unable to start server: %s", ex.getMessage());
            System.exit(1);
        }

        Spark.awaitInitialization();
        return Spark.port();
    }


    public void stop() {
        Spark.stop();
    }

    public Object errorHandler(CodedException e, Request req, Response res) {
        var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage()), "success", false));
        res.type("application/json");
        res.status(e.statusCode());
        res.body(body);
        return body;
    }

    private void log(Request req, Response res) {
        log.info(String.format("[%s] %s - %s", req.requestMethod(), req.pathInfo(), res.status()));
    }

    /**
     * Endpoint for [DELETE] /db
     */
    public Object clearApplication(Request ignoreReq, Response res) throws CodedException {
        adminService.clearApplication();
        return send();
    }

    /**
     * Endpoint for [POST] /user - Register user
     * <pre>{ "username":"", "password":"", "email":"" }</pre>
     */
    private Object registerUser(Request req, Response ignore) throws CodedException {
        var user = getBody(req, UserData.class);
        var authToken = userService.registerUser(user);
        return send("username", user.username(), "authToken", authToken.authToken());
    }


    /**
     * Endpoint for [POST] /session
     * <pre>{ "username":"", "password":"" }</pre>
     */
    public Object createSession(Request req, Response ignore) throws CodedException {
        var user = getBody(req, UserData.class);
        var authData = authService.createSession(user);
        return send("username", user.username(), "authToken", authData.authToken());
    }

    /**
     * Endpoint for [DELETE] /session
     * Authorization header required.
     */
    public Object deleteSession(Request req, Response ignore) throws CodedException {
        var authData = throwIfUnauthorized(req);
        authService.deleteSession(authData.authToken());
        return send();
    }


    /**
     * Endpoint for [GET] /game
     * Authorization header required.
     */
    public Object listGames(Request req, Response ignoreRes) throws CodedException {
        throwIfUnauthorized(req);
        var games = gameService.listGames();
        return send("games", games.toArray());
    }

    /**
     * Endpoint for [POST] / game
     * Authorization header required.
     */
    public Object createGame(Request req, Response ignoreRes) throws CodedException {
        throwIfUnauthorized(req);
        var gameData = getBody(req, GameData.class);
        gameData = gameService.createGame(gameData.gameName());
        return send("gameID", gameData.gameID());
    }

    /**
     * Endpoint for [PUT] /
     * Authorization header required.
     * <pre>{ "playerColor":"WHITE/BLACK/empty", "gameID": 1234 }</pre>
     */
    public Object joinGame(Request req, Response ignoreRes) throws CodedException {
        var authData = throwIfUnauthorized(req);
        var joinReq = getBody(req, JoinRequest.class);
        gameService.joinGame(authData.username(), joinReq.playerColor(), joinReq.gameID());
        return send();
    }

    private <T> T getBody(Request request, Class<T> clazz) throws CodedException {
        var body = new Gson().fromJson(request.body(), clazz);
        if (body == null) {
            throw new CodedException(400, "Missing body");
        }
        return body;
    }

    private String send(Object... props) {
        Map<Object, Object> map = new HashMap<>();
        for (var i = 0; i + 1 < props.length; i = i + 2) {
            map.put(props[i], props[i + 1]);
        }
        return new Gson().toJson(map);
    }

    private AuthData throwIfUnauthorized(Request req) throws CodedException {
        var authToken = req.headers("authorization");
        if (authToken != null) {
            var authData = authService.getAuthData(authToken);
            if (authData != null) {
                return authData;
            }
        }

        throw new CodedException(401, "Not authorized");

    }

}
