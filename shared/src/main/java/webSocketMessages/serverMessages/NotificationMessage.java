package webSocketMessages.serverMessages;


public class NotificationMessage extends SerializableServerMessage {
    public String message;

    public NotificationMessage(String message) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
    }
}
