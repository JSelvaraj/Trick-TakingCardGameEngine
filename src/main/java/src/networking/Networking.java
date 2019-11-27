package src.networking;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import src.parser.Parser;
import src.player.LocalPlayer;
import src.player.NetworkPlayer;
import src.player.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

public class Networking {

    private final int PORTNUMBER = 6969;
    private int numberOfPlayers;

    class PlayerInfo {
        String ip;
        int port;
        int playerNumber;

        public PlayerInfo (String ip, int port, int playerNumber) {
            this.ip = ip;
            this.port = port;
            this.playerNumber = playerNumber;
        }
    }


    public Networking (int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }


    public Player[] hostGame(String gameDescFile)  {
        Player[] players = new Player[numberOfPlayers];
        NetworkPlayer[] networkPlayers = new NetworkPlayer[numberOfPlayers];
        PlayerInfo[] playersInfo = new PlayerInfo[numberOfPlayers];
        JSONArray playersJSONArray = new JSONArray();
        try {
            ServerSocket socket = new ServerSocket(PORTNUMBER);
            for (int i = 1; i < players.length ; i++) {
                NetworkPlayer networkPlayer = new NetworkPlayer(i, socket.accept());

                players[i] = networkPlayer;
                networkPlayers[i] = networkPlayer;
                BufferedReader reader = new BufferedReader(new InputStreamReader(networkPlayer.getPlayerSocket().getInputStream()));
                String JSONfile = reader.readLine();

                System.out.println("File read = " + JSONfile); // debugging sout

                JSONObject object = (JSONObject) new JSONTokener(JSONfile).nextValue();
                object.put("playerNumber", i);
                playersJSONArray.put(object);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        players[0] = new LocalPlayer(0);
        JSONObject forClients = new JSONObject();
        forClients.put("spec", Parser.readJSONFile(gameDescFile));
        forClients.put("players", playersJSONArray);
        forClients.put("seed", 420);


        for (int i = 1; i < players.length; i++) {
            try {
                OutputStreamWriter out = new OutputStreamWriter(networkPlayers[i].getPlayerSocket().getOutputStream(), StandardCharsets.UTF_8);
                out.write(forClients.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return players;
    }

//    public Player[] connectToGame( ) {
//        Player[] players = new Player[numberOfPlayers];
//
//
//    }
}
