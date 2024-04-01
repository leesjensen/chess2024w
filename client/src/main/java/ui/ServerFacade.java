package ui;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import server.JoinRequest;
import util.ResponseException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String serverName) {
        serverUrl = String.format("http://%s", serverName);
    }

    public ServerFacade(int port) {
        serverUrl = String.format("http://localhost:%d", port);
    }


    public void clear() throws ResponseException {
        var r = this.makeRequest("DELETE", "/db", null, null, Map.class);
    }

    public AuthData register(String username, String password, String email) throws ResponseException {
        var request = Map.of("username", username, "password", password, "email", email);
        return this.makeRequest("POST", "/user", request, null, AuthData.class);
    }

    public AuthData login(String username, String password) throws ResponseException {
        var request = Map.of("username", username, "password", password);
        return this.makeRequest("POST", "/session", request, null, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        this.makeRequest("DELETE", "/session", null, authToken, null);
    }

    public GameData createGame(String authToken, String gameName) throws ResponseException {
        var request = Map.of("gameName", gameName);
        return this.makeRequest("POST", "/game", request, authToken, GameData.class);
    }

    public GameData[] listGames(String authToken) throws ResponseException {
        record Response(GameData[] games) {
        }
        var response = this.makeRequest("GET", "/game", null, authToken, Response.class);
        return (response != null ? response.games : new GameData[0]);
    }

    public GameData joinGame(String authToken, int gameID, ChessGame.TeamColor color) throws ResponseException {
        var request = new JoinRequest(color, gameID);
        this.makeRequest("PUT", "/game", request, authToken, GameData.class);
        return getGame(authToken, gameID);
    }

    private GameData getGame(String authToken, int gameID) throws ResponseException {
        var games = listGames(authToken);
        for (var game : games) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        throw new ResponseException(404, "Missing game");
    }

    private <T> T makeRequest(String method, String path, Object request, String authToken, Class<T> clazz) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null) {
                http.addRequestProperty("Authorization", authToken);
            }

            if (request != null) {
                http.addRequestProperty("Accept", "application/json");
                String reqData = new Gson().toJson(request);
                try (OutputStream reqBody = http.getOutputStream()) {
                    reqBody.write(reqData.getBytes());
                }
            }
            http.connect();

            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (http.getResponseCode() == 200) {
                    if (clazz != null) {
                        var serializer = new Gson();
                        return serializer.fromJson(reader, clazz);
                    }
                    return null;
                }

                throw new ResponseException(http.getResponseCode(), reader);
            }
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }
}
