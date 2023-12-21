package ui;

import com.google.gson.Gson;
import webSocketMessages.serverMessages.ErrorMessage;
import webSocketMessages.serverMessages.LoadMessage;
import webSocketMessages.serverMessages.NotificationMessage;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.GameCommand;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

//need to extend Endpoint for websocket to work properly
public class WebSocketFacade extends Endpoint {

    Session session;
    DisplayHandler responseHandler;


    public WebSocketFacade(int port, DisplayHandler responseHandler) throws DeploymentException, IOException, URISyntaxException {
        var url = String.format("ws://localhost:%d/connect", port);
        URI socketURI = new URI(url);
        this.responseHandler = responseHandler;


        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, socketURI);

        //set message handler
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                var gson = new Gson();

                try {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    switch (serverMessage.getServerMessageType()) {
                        case LOAD_GAME -> responseHandler.updateBoard(gson.fromJson(message, LoadMessage.class).game);
                        case NOTIFICATION ->
                                responseHandler.message(gson.fromJson(message, NotificationMessage.class).message);
                        case ERROR -> responseHandler.error(gson.fromJson(message, ErrorMessage.class).errorMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error: " + e.getMessage());
                }
            }
        });
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void sendCommand(GameCommand command) throws IOException {
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }
}

