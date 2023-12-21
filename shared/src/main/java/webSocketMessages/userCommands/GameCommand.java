package webSocketMessages.userCommands;

public class GameCommand extends UserGameCommand {
    public final Integer gameID;

    public GameCommand(CommandType cmd, String authToken, Integer gameID) {
        super(authToken);
        super.commandType = cmd;
        this.gameID = gameID;
    }
}
