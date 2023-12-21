package webSocketMessages.serverMessages;

import model.GameData;

public class LoadMessage extends SerializableServerMessage {
    public GameData game;

    public LoadMessage(GameData gameData) {
        super(ServerMessageType.LOAD_GAME);
        this.game = gameData;
    }
}
