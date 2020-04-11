package src;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;
import src.exceptions.InvalidJSONMessageException;
import src.gameEngine.HostRunner;
import src.gameEngine.PlayerRunner;
import src.networking.DiscoverGames;
import src.parser.Parser;
import src.player.LocalPlayer;
import src.player.RandomPlayer;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSocketHandler extends WebSocketServer {
    private AtomicBoolean discoveringGames = new AtomicBoolean(false);
    private static String LOCATION = "ws://localhost:8081";
    private static final int PORT = 9091;

    public WebSocketHandler(InetSocketAddress address) {
        super(address);
    }




    public static void main(String[] args) {
        InetSocketAddress home = new InetSocketAddress(PORT);
        WebSocketHandler connection = new WebSocketHandler(home);
        connection.start();
        System.out.println("Done");


    }


    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Opened connection");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received: " + message);
        JsonObject request = new Gson().fromJson(message, JsonObject.class);
        String type = request.get("type").getAsString();
        switch (type) {
            case "DiscoverGame":
                TreeSet<String> beacons;
                beacons = DiscoverGames.search();
                JsonArray array = new JsonArray();
                for (String beacon : beacons) {
                    System.out.println(beacon);
                    array.add(beacon);
                }
                JsonObject discoverGamesResponse = new JsonObject();
                discoverGamesResponse.add("type", new JsonPrimitive("DiscoverGame"));
                discoverGamesResponse.add("beacons", array);
                System.out.println("Object: " + discoverGamesResponse.toString());
                conn.send(discoverGamesResponse.toString());
                break;
            case "GetGameList":
                File folder = new File("Games/");
                JSONObject object = new JSONObject();
                JSONArray gameListArray = new JSONArray();
                for (File game : Objects.requireNonNull(folder.listFiles())) { // iterates through the Games folder to get all game descriptions
                    JSONObject gameDesc = Parser.readJSONFile(game.getPath());
                    JSONObject gameDescAndPath = new JSONObject();
                    gameDescAndPath.put("path", game.getPath());
                    gameDescAndPath.put("gamedesc", gameDesc);
                    gameListArray.put(gameDescAndPath);
                }
                object.put("type", "GetGameList");
                object.put("games", gameListArray);
                conn.send(object.toString());
                break;
            case "HostGame":
                String path = request.get("gamepath").getAsString();
                Thread thread = new Thread(new HostRunner(new LocalPlayer(), 0, path)); //local port as 0 means its assigned at runtime by system.
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

            default:
                throw new InvalidJSONMessageException("Message format not recognised");





        }


    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Server Started \nwaiting for connection...");

    }
}
