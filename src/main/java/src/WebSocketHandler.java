package src;


import com.google.gson.Gson;
import com.google.gson.JsonArray;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import src.gameEngine.HostRunner;
import src.gameEngine.PlayerRunner;
import src.networking.DiscoverGames;
import src.parser.GameDesc;
import src.parser.Parser;
import src.player.LocalPlayer;
import src.player.RandomPlayer;

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
        JsonObject request = new Gson().fromJson(message, JsonObject.class);
        String type = request.get("type").getAsString();
        switch (type) {
            case "DiscoverGame":
                TreeSet<String> beacons;
                discoveringGames.set(true);
                while (discoveringGames.get()) {
                    beacons = DiscoverGames.search();
                    JsonArray array = new JsonArray();
                    for (String beacon : beacons) {
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
            case "GetGameList":
                File folder = new File("Games/");
                JSONObject object = new JSONObject();
                JSONArray array = new JSONArray();
                for (File game : Objects.requireNonNull(folder.listFiles())) { // iterates through the Games folder to get all game descriptions
                    JSONObject gameDesc = Parser.readJSONFile(game.getPath());
                    JSONObject gameDescAndPath = new JSONObject();
                    gameDescAndPath.put("path", game.getPath());
                    gameDescAndPath.put("gamedesc", gameDesc);
                    array.put(gameDescAndPath);
                }
                object.put("type", "GetGameList");
                object.put("games", array);
                send(object.toString());
                break;
            case "HostGame":
                String path = request.get("gamepath").getAsString();
                Thread thread = new Thread(new HostRunner(new LocalPlayer(), request.get("port").getAsInt(), path));
                thread.start();
                for (int i = 0; i < request.get("aiplayers").getAsInt(); i++) {
                    System.out.println("AI started");
                    PlayerRunner runner = new PlayerRunner(new RandomPlayer(),
                            "localhost",
                            request.get("port").getAsInt(),
                            true,
                            false);
                    Thread aiThread = new Thread(runner);
                    aiThread.start();
                }
                break;
            case "JoinGame":
                Thread thread2 = new Thread(new PlayerRunner(
                        new LocalPlayer(),
                        request.get("address").getAsString(),
                        request.get("port").getAsInt(),
                        request.get("localport").getAsInt(),
                        false,
                        true)
                );
                thread2.start();
                break;





        }


    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
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
