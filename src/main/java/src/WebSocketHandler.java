package src;


import com.google.gson.JsonArray;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import src.networking.DiscoverGames;
import src.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.InputMismatchException;
import java.util.Objects;
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
                            JsonObject object = new JsonObject();
                            object.add("beacons", array);
                            send(object.getAsString());
                        }
                        break;
                    case "StopDiscoverGames":
                        discoveringGames.set(false);
                        break;
                    case "GetGameList" :
                        File folder = new File("Games\\");
                        JSONObject object = new JSONObject();
                        JSONArray array = new JSONArray();
                        for (File game: Objects.requireNonNull(folder.listFiles())) { // iterates through the Games folder to get all game descriptions
                            JSONObject gameDesc = Parser.readJSONFile(game.getPath());
                            array.put(gameDesc);
                        }
                        object.put("type", "GetGameList");
                        object.put("games", array);
                        send(object.toString());
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
