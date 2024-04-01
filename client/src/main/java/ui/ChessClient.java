package ui;

import chess.*;
import model.GameData;
import util.ExceptionUtil;
import util.ResponseException;
import webSocketMessages.userCommands.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static util.EscapeSequences.*;


public class ChessClient implements DisplayHandler {

    private State userState = State.LOGGED_OUT;
    private String authToken;
    private GameData gameData;
    private GameData[] games;
    final private ServerFacade server;
    final private WebSocketFacade webSocket;


    public ChessClient(String hostname) throws Exception {
        server = new ServerFacade(hostname);
        webSocket = new WebSocketFacade(hostname, this);
    }

    public String eval(String input) {

        var result = "Error with command. Try: Help";
        try {
            input = input.toLowerCase();
            var tokens = input.split(" ");
            if (tokens.length == 0) {
                tokens = new String[]{"Help"};
            }

            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            try {
                result = (String) this.getClass().getDeclaredMethod(tokens[0], String[].class).invoke(this, new Object[]{params});
            } catch (NoSuchMethodException e) {
                result = String.format("Unknown command\n%s", help(params));
            }
        } catch (Throwable e) {
            var root = ExceptionUtil.getRoot(e);
            result = String.format("Error: %s", root.getMessage());
        }
        return result;
    }

    public void clear() throws Exception {
        server.clear();
    }

    private String clear(String[] ignored) throws Exception {
        clear();
        userState = State.LOGGED_OUT;
        gameData = null;
        return "Cleared the world";
    }

    private String help(String[] ignored) {
        return switch (userState) {
            case LOGGED_IN -> getHelp(loggedInHelp);
            case OBSERVING -> getHelp(observingHelp);
            case BLACK, WHITE -> getHelp(playingHelp);
            default -> getHelp(loggedOutHelp);
        };
    }

    private String quit(String[] ignored) {
        return "quit";
    }


    private String login(String[] params) throws ResponseException {
        if (userState == State.LOGGED_OUT && params.length == 2) {
            var response = server.login(params[0], params[1]);
            authToken = response.authToken();
            userState = State.LOGGED_IN;
            return String.format("Logged in as %s", params[0]);
        }
        return "Failure";
    }

    private String register(String[] params) throws ResponseException {
        if (userState == State.LOGGED_OUT && params.length == 3) {
            var response = server.register(params[0], params[1], params[2]);
            authToken = response.authToken();
            userState = State.LOGGED_IN;
            return String.format("Logged in as %s", params[0]);
        }
        return "Failure";
    }

    private String logout(String[] ignore) throws ResponseException {
        verifyAuth();

        if (userState != State.LOGGED_OUT) {
            server.logout(authToken);
            userState = State.LOGGED_OUT;
            authToken = null;
            return "Logged out";
        }
        return "Failure";
    }

    private String create(String[] params) throws ResponseException {
        verifyAuth();

        if (params.length == 1 && userState == State.LOGGED_IN) {
            var gameData = server.createGame(authToken, params[0]);
            return String.format("Create %d", gameData.gameID());
        }
        return "Failure";
    }

    private String list(String[] ignore) throws ResponseException {
        verifyAuth();
        games = server.listGames(authToken);
        StringBuilder buf = new StringBuilder();
        for (var i = 0; i < games.length; i++) {
            var game = games[i];
            var gameText = String.format("%d. %s white:%s black:%s state: %s%n", i, game.gameName(), game.whiteUsername(), game.blackUsername(), game.state());
            buf.append(gameText);
        }
        return buf.toString();
    }


    private String join(String[] params) throws Exception {
        verifyAuth();
        if (userState == State.LOGGED_IN) {
            if (params.length == 2 && (params[1].equalsIgnoreCase("WHITE") || params[1].equalsIgnoreCase("BLACK"))) {
                var gamePos = Integer.parseInt(params[0]);
                if (games != null && gamePos >= 0 && gamePos < games.length) {
                    var gameID = games[gamePos].gameID();
                    var color = ChessGame.TeamColor.valueOf(params[1].toUpperCase());
                    gameData = server.joinGame(authToken, gameID, color);
                    userState = (color == ChessGame.TeamColor.WHITE ? State.WHITE : State.BLACK);
                    webSocket.sendCommand(new JoinPlayerCommand(authToken, gameID, color));
                    return String.format("Joined %d as %s", gameData.gameID(), color);
                }
            }
        }

        return "Failure";
    }


    private String observe(String[] params) throws Exception {
        verifyAuth();
        if (userState == State.LOGGED_IN) {
            if (params.length == 1) {
                var gameID = Integer.parseInt(params[0]);
                gameData = server.joinGame(authToken, gameID, null);
                userState = State.OBSERVING;
                webSocket.sendCommand(new GameCommand(UserGameCommand.CommandType.JOIN_OBSERVER, authToken, gameID));
                return String.format("Joined %d as observer", gameData.gameID());
            }
        }

        return "Failure";
    }

    private String redraw(String[] ignored) throws Exception {
        verifyAuth();
        if (isPlaying() || isObserving()) {
            printGame();
            return "";
        }
        return "Failure";
    }

    private String legal(String[] params) throws Exception {
        verifyAuth();
        if (isPlaying() || isObserving()) {
            if (params.length == 1) {
                var pos = new ChessPosition(params[0]);
                var highlights = new ArrayList<ChessPosition>();
                highlights.add(pos);
                for (var move : gameData.game().validMoves(pos)) {
                    highlights.add(move.getEndPosition());
                }
                var color = userState == State.BLACK ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
                printGame(color, highlights);
                return "";
            }
        }
        return "Failure";
    }

    private String move(String[] params) throws Exception {
        verifyAuth();
        if (params.length == 1) {
            var move = new ChessMove(params[0]);
            if (isMoveLegal(move)) {
                webSocket.sendCommand(new MoveCommand(authToken, gameData.gameID(), move));
                return "Success";
            }
        }
        return "Failure";
    }

    private String leave(String[] ignored) throws Exception {
        if (isPlaying() || isObserving()) {
            userState = State.LOGGED_IN;
            webSocket.sendCommand(new GameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameData.gameID()));
            gameData = null;
            return "Left game";
        }
        return "Failure";
    }

    private String resign(String[] ignored) throws Exception {
        if (isPlaying()) {
            webSocket.sendCommand(new GameCommand(GameCommand.CommandType.RESIGN, authToken, gameData.gameID()));
            userState = State.LOGGED_IN;
            gameData = null;
            return "Resigned";
        }
        return "Failure";
    }

    private void printGame() {
        var color = userState == State.BLACK ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        printGame(color, null);
    }

    private void printGame(ChessGame.TeamColor color, Collection<ChessPosition> highlights) {
        System.out.println("\n");
        System.out.print((gameData.game().getBoard()).toString(color, highlights));
        System.out.println();
    }

    public void printPrompt() {
        String gameState = "Not playing";
        if (gameData != null) {
            gameState = switch (gameData.state()) {
                case UNDECIDED -> String.format("%s's turn", gameData.game().getTeamTurn());
                case DRAW -> "Draw";
                case BLACK -> "Black won";
                case WHITE -> "White Won";
            };
        }
        System.out.print(RESET_TEXT_COLOR + String.format("\n[%s: %s] >>> ", userState, gameState) + SET_TEXT_COLOR_GREEN);
    }

    public boolean isMoveLegal(ChessMove move) {
        if (isTurn()) {
            var board = gameData.game().getBoard();
            var piece = board.getPiece(move.getStartPosition());
            if (piece != null) {
                var validMoves = piece.pieceMoves(board, move.getStartPosition());
                if (validMoves.contains(move)) {
                    return board.isMoveLegal(move);
                }
            }
        }
        return false;
    }

    public boolean isPlaying() {
        return (gameData != null && (userState == State.WHITE || userState == State.BLACK) && !isGameOver());
    }


    public boolean isObserving() {
        return (gameData != null && (userState == State.OBSERVING));
    }

    public boolean isGameOver() {
        return (gameData != null && gameData.isGameOver());
    }

    public boolean isTurn() {
        return (isPlaying() && userState.isTurn(gameData.game().getTeamTurn()));
    }

    @Override
    public void updateBoard(GameData newGameData) {
        gameData = newGameData;
        printGame();
        printPrompt();

        if (isGameOver()) {
            userState = State.LOGGED_IN;
            printPrompt();
            gameData = null;
        }
    }

    @Override
    public void message(String message) {
        System.out.println();
        System.out.println(SET_TEXT_COLOR_MAGENTA + "NOTIFY: " + message);
        printPrompt();
    }

    @Override
    public void error(String message) {
        System.out.println();
        System.out.println(SET_TEXT_COLOR_RED + "NOTIFY: " + message);
        printPrompt();

    }

    /**
     * Representation of all the possible client commands.
     */
    private record Help(String cmd, String description) {
    }

    static final List<Help> loggedOutHelp = List.of(
            new Help("register <USERNAME> <PASSWORD> <EMAIL>", "to create an account"),
            new Help("login <USERNAME> <PASSWORD>", "to play chess"),
            new Help("quit", "playing chess"),
            new Help("help", "with possible commands")
    );

    static final List<Help> loggedInHelp = List.of(
            new Help("create <NAME>", "a game"),
            new Help("list", "games"),
            new Help("join <ID> [WHITE|BLACK]", "a game"),
            new Help("observe <ID>", "a game"),
            new Help("logout", "when you are done"),
            new Help("quit", "playing chess"),
            new Help("help", "with possible commands")
    );

    static final List<Help> observingHelp = List.of(
            new Help("legal", "moves for the current board"),
            new Help("redraw", "the board"),
            new Help("leave", "the game"),
            new Help("quit", "playing chess"),
            new Help("help", "with possible commands")
    );

    static final List<Help> playingHelp = List.of(
            new Help("redraw", "the board"),
            new Help("leave", "the game"),
            new Help("move <crcr> [q|r|b|n]", "a piece with optional promotion"),
            new Help("resign", "the game without leaving it"),
            new Help("legal <cr>", "moves a given piece"),
            new Help("quit", "playing chess"),
            new Help("help", "with possible commands")
    );

    private String getHelp(List<Help> help) {
        StringBuilder sb = new StringBuilder();
        for (var me : help) {
            sb.append(String.format("  %s%s%s - %s%s%s%n", SET_TEXT_COLOR_BLUE, me.cmd, RESET_TEXT_COLOR, SET_TEXT_COLOR_MAGENTA, me.description, RESET_TEXT_COLOR));
        }
        return sb.toString();

    }

    private void verifyAuth() throws ResponseException {
        if (authToken == null) {
            throw new ResponseException(401, "Please login or register");
        }
    }

}
