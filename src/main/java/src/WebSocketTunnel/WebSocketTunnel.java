package src.WebSocketTunnel;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import src.WebSocketHandler;

import java.net.URI;

public class WebSocketTunnel extends WebSocketClient {

    private WebSocketHandler handler;

    public WebSocketTunnel(URI serverUri, WebSocketHandler handler) {
        super(serverUri);
        this.handler = handler;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Tunnel Connected to Game");

    }

    @Override
    public void onMessage(String message) {

        for (WebSocket socket : handler.getConnections()) {
            if (socket != this.getConnection()) {
                System.out.println("MESSAGE SENT:" + socket.getLocalSocketAddress() + " :" + message);
                socket.send(message);
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }
}
