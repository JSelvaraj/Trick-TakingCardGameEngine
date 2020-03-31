package src;


import com.damnhandy.uri.template.UriTemplateBuilderException;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import src.networking.DiscoverGames;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.InputMismatchException;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSocketHandler extends WebSocketClient {
    private AtomicBoolean discoveringGames = new AtomicBoolean(false);
    private static String LOCATION = "ws://localhost:8081";


    public WebSocketHandler(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Opened connection");

    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
        JsonReader reader = new JsonReader(new StringReader(message));

        try {
            String attribute = reader.nextName();
            String type;
            if (attribute.equals("type")) {
                type = reader.nextString();
                switch (type) {
                    case "DiscoverGame":
                        TreeSet<String> beacons;
                        discoveringGames.set(true);
                        while (discoveringGames.get()) {
                            beacons =  DiscoverGames.search();
                            JsonArray array = new JsonArray();
                            for (String beacon: beacons) {
                                array.add(beacon);
                            }
                            send(array.getAsString());
                        }
                        break;
                    case "StopDiscoverGames":
                        discoveringGames.set(false);
                        break;

                }





            } else {
                throw new InputMismatchException("Wrong json format");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println( "Connection closed by " + ( remote ? "remote peer" : "us" ) + " Code: " + code + " Reason: " + reason );
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

     public static void main(String[] args) throws URISyntaxException {
         WebSocketHandler connection = new WebSocketHandler(new URI(LOCATION));
         connection.connect();


     }


}
