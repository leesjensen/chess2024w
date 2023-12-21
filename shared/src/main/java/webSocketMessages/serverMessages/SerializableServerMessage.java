package webSocketMessages.serverMessages;

import com.google.gson.Gson;

public class SerializableServerMessage extends ServerMessage {

    public SerializableServerMessage(ServerMessageType type) {
        super(type);
    }


    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
