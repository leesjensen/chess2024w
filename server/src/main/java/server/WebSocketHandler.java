package server;

import chess.*;
import com.google.gson.Gson;
import dataAccess.*;
import model.*;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import util.StringUtil;
import webSocketMessages.serverMessages.*;
import webSocketMessages.userCommands.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;

@WebSocket
public class WebSocketHandler {

    private final DataAccess dataAccess;

    public static class Connection {
        public UserData user;
        public GameData game;
        public Session session;

        public Connection(UserData user, Session session) {
            this.user = user;
            this.session = session;
        }


        private void send(String msg) throws Exception {
            session.getRemote().sendString(msg);
        }

        private void sendError(String msg) throws Exception {
            sendError(session.getRemote(), msg);
        }

        private static void sendError(RemoteEndpoint endpoint, String msg) throws Exception {
            var errMsg = (new ErrorMessage(String.format("ERROR: %s", msg))).toString();
            endpoint.sendString(errMsg);
        }

    }

    public static class ConnectionManager {
        public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

        public void add(String username, Connection connection) {
            connections.put(username, connection);
        }

        public Connection get(String username) {
            return connections.get(username);
        }

        public void remove(Session session) {
            Connection removeConnection = null;
            for (var c : connections.values()) {
                if (c.session.equals(session)) {
                    removeConnection = c;
                    break;
                }
            }

            if (removeConnection != null) {
                connections.remove(removeConnection.user.username());
            }
        }

        public void broadcast(int gameID, String excludeUsername, String msg) throws Exception {
            var removeList = new ArrayList<Connection>();
            for (var c : connections.values()) {
                if (c.session.isOpen()) {
                    if (c.game.gameID() == gameID && !StringUtil.isEqual(c.user.username(), excludeUsername)) {
                        c.send(msg);
                    }
                } else {
                    removeList.add(c);
                }
            }

            // Clean up any connections that were left open.
            for (var c : removeList) {
                connections.remove(c.user.username());
            }
        }

        @Override
        public String toString() {
            var sb = new StringBuilder("[\n");
            for (var c : connections.values()) {
                sb.append(String.format("  {'game':%d, 'user': %s}%n", c.game.gameID(), c.user));
            }
            sb.append("]");
            return sb.toString();
        }
    }

    private final ConnectionManager connections = new ConnectionManager();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {
    }


    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        connections.remove(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        try {
            var command = readJson(message, GameCommand.class);
            var connection = getConnection(command.getAuthString(), session);
            if (connection != null) {
                switch (command.getCommandType()) {
                    case JOIN_PLAYER -> join(connection, readJson(message, JoinPlayerCommand.class));
                    case JOIN_OBSERVER -> observe(connection, command);
                    case MAKE_MOVE -> move(connection, readJson(message, MoveCommand.class));
                    case LEAVE -> leave(connection, command);
                    case RESIGN -> resign(connection, command);
                }
            } else {
                Connection.sendError(session.getRemote(), "unknown user");
            }
        } catch (Exception e) {
            Connection.sendError(session.getRemote(), util.ExceptionUtil.getRoot(e).getMessage());
        }
    }

    private void join(Connection connection, JoinPlayerCommand command) throws Exception {
        var gameData = dataAccess.readGame(command.gameID);
        if (gameData != null) {
            var expectedUsername = (command.playerColor == ChessGame.TeamColor.BLACK) ? gameData.blackUsername() : gameData.whiteUsername();
            if (StringUtil.isEqual(expectedUsername, connection.user.username())) {
                connection.game = gameData;
                var loadMsg = (new LoadMessage(gameData)).toString();
                connection.send(loadMsg);

                var notificationMsg = (new NotificationMessage(String.format("%s joined %s as %s", connection.user.username(), gameData.gameName(), command.playerColor))).toString();
                connections.broadcast(gameData.gameID(), connection.user.username(), notificationMsg);
            } else {
                connection.sendError("player has not joined game");
            }
        } else {
            connection.sendError("unknown game");
        }
    }

    private void observe(Connection connection, GameCommand command) throws Exception {
        var gameData = dataAccess.readGame(command.gameID);
        if (gameData != null) {
            connection.game = gameData;
            var loadMsg = (new LoadMessage(gameData)).toString();
            connection.send(loadMsg);

            var notificationMsg = (new NotificationMessage(String.format("%s observing %s", connection.user.username(), gameData.gameName()))).toString();
            connections.broadcast(gameData.gameID(), connection.user.username(), notificationMsg);
        } else {
            connection.sendError("unknown game");
        }
    }

    private void move(Connection connection, MoveCommand command) throws Exception {
        var gameData = dataAccess.readGame(command.gameID);
        if (gameData != null) {
            if (!gameData.isGameOver()) {
                if (isTurn(gameData, command.move, connection.user.username())) {
                    gameData.game().makeMove(command.move);
                    var notificationMsg = (new NotificationMessage(String.format("%s moved %s", connection.user.username(), command.move))).toString();
                    connections.broadcast(gameData.gameID(), connection.user.username(), notificationMsg);

                    gameData = handleGameStateChange(gameData);
                    dataAccess.updateGame(gameData);
                    connection.game = gameData;

                    var loadMsg = (new LoadMessage(gameData)).toString();
                    connections.broadcast(gameData.gameID(), "", loadMsg);
                } else {
                    connection.sendError("invalid move: " + command.move);
                }
            } else {
                connection.sendError("game is over: " + gameData.state());
            }
        } else {
            connection.sendError("unknown game");
        }
    }


    private void leave(Connection connection, GameCommand command) throws Exception {
        var gameData = dataAccess.readGame(command.gameID);
        if (gameData != null) {
            if (StringUtil.isEqual(gameData.blackUsername(), connection.user.username())) {
                gameData = gameData.setBlack(null);
            } else if (StringUtil.isEqual(gameData.whiteUsername(), connection.user.username())) {
                gameData = gameData.setWhite(null);
            }
            dataAccess.updateGame(gameData);
            connections.remove(connection.session);
            var notificationMsg = (new NotificationMessage(String.format("%s left", connection.user.username()))).toString();
            connections.broadcast(gameData.gameID(), "", notificationMsg);
        } else {
            connection.sendError("unknown game");
        }
    }

    private void resign(Connection connection, GameCommand command) throws Exception {
        var gameData = dataAccess.readGame(command.gameID);
        if (gameData != null && !gameData.isGameOver()) {
            var color = getPlayerColor(gameData, connection.user.username());
            if (color != null) {
                var state = color == ChessGame.TeamColor.WHITE ? GameData.State.BLACK : GameData.State.WHITE;
                gameData = gameData.setState(state);
                dataAccess.updateGame(gameData);
                connection.game = gameData;

                var notificationMsg = (new NotificationMessage(String.format("%s resigned", connection.user.username()))).toString();
                connections.broadcast(gameData.gameID(), "", notificationMsg);
            } else {
                connection.sendError("only players can resign");
            }
        } else {
            connection.sendError("unknown game");
        }
    }

    private boolean isTurn(GameData gameData, ChessMove move, String username) {
        var piece = gameData.game().getBoard().getPiece(move.getStartPosition());
        var turn = gameData.game().getTeamTurn();
        var turnUsername = turn.equals(WHITE) ? gameData.whiteUsername() : gameData.blackUsername();
        return (turnUsername.equals(username) && piece != null && piece.getTeamColor().equals(turn));
    }

    private ChessGame.TeamColor getPlayerColor(GameData gameData, String username) {
        if (StringUtil.isEqual(gameData.blackUsername(), username)) {
            return ChessGame.TeamColor.BLACK;
        } else if (StringUtil.isEqual(gameData.whiteUsername(), username)) {
            return WHITE;
        }
        return null;
    }

    private GameData handleGameStateChange(GameData gameData) throws Exception {
        NotificationMessage notificationMsg = null;
        var game = gameData.game();
        if (game.isInStalemate(WHITE) || game.isInStalemate(BLACK)) {
            gameData = gameData.setState(GameData.State.DRAW);
            notificationMsg = new NotificationMessage("game is a draw");
        } else if (game.isInCheckmate(WHITE)) {
            gameData = gameData.setState(GameData.State.BLACK);
            notificationMsg = new NotificationMessage(String.format("Black player, %s, wins!", gameData.blackUsername()));
        } else if (game.isInCheckmate(BLACK)) {
            gameData = gameData.setState(GameData.State.WHITE);
            notificationMsg = new NotificationMessage(String.format("White player, %s, wins!", gameData.whiteUsername()));
        } else if (game.isInCheck(WHITE)) {
            notificationMsg = new NotificationMessage(String.format("White player, %s, is in check!", gameData.whiteUsername()));
        } else if (game.isInCheck(BLACK)) {
            notificationMsg = new NotificationMessage(String.format("Black player, %s, is in check!", gameData.blackUsername()));
        }

        if (notificationMsg != null) {
            connections.broadcast(gameData.gameID(), "", notificationMsg.toString());
        }
        return gameData;
    }


    private static <T> T readJson(String json, Class<T> clazz) throws IOException {
        var gson = new Gson();
        var obj = gson.fromJson(json, clazz);
        if (obj == null) {
            throw new IOException("Invalid JSON");
        }
        return obj;
    }


    private Connection getConnection(String id, Session session) throws Exception {
        Connection connection = null;
        var authData = isAuthorized(id);
        if (authData != null) {
            connection = connections.get(authData.username());
            if (connection == null) {
                var user = dataAccess.readUser(authData.username());
                connection = new Connection(user, session);
                connections.add(authData.username(), connection);
            }
        }
        return connection;
    }


    public AuthData isAuthorized(String token) throws DataAccessException {
        if (token != null) {
            return dataAccess.readAuth(token);
        }
        return null;
    }

}
