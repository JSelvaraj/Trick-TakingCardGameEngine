package src.networking;

import jdk.internal.util.xml.impl.Input;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import src.parser.Parser;
import src.player.LocalPlayer;
import src.player.NetworkPlayer;
import src.player.Player;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.InputMismatchException;

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
        Parser parser = new Parser();


        players[0] = new LocalPlayer(0);
        JSONObject forClients = new JSONObject();
        forClients.put("spec", Parser.readJSONFile(gameDescFile));
        forClients.put("players", playersJSONArray);
        forClients.put("seed", 420 ); //TODO change to parser.seed when parser is edited.

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

    public Player[] connectToGame(String ip, int port) {
        Player[] players = new Player[numberOfPlayers];
        int playerNumber = -1;
        try {
            Socket hostSocket = new Socket(ip, port);
            ServerSocket serverSocket = new ServerSocket(PORTNUMBER);
            JSONObject playerInfo = new JSONObject();
            playerInfo.put("ip", serverSocket.getInetAddress());
            playerInfo.put("port", serverSocket.getLocalPort());
            BufferedReader reader = new BufferedReader(new InputStreamReader(hostSocket.getInputStream()));
            OutputStreamWriter writer = new OutputStreamWriter(hostSocket.getOutputStream());
            writer.write(playerInfo.toString());

            String JSONfile = reader.readLine();
            JSONObject fromHost = (JSONObject) new JSONTokener(JSONfile).nextValue();
            JSONObject gameJSON = fromHost.getJSONObject("spec");
            JSONArray playersInfo = fromHost.getJSONArray("players");
            int seed = fromHost.getInt("seed");

            ArrayList<PlayerInfo> info = new ArrayList<>();

            for (Object player: playersInfo) {
                if (player instanceof JSONObject) {
                    info.add(new PlayerInfo(((JSONObject) player).getString("ip"),((JSONObject) player).getInt("port"), ((JSONObject) player).getInt("playerNumber")));
                    if (((JSONObject) player).getString("ip").equals(serverSocket.getInetAddress().toString()) &&
                            ((JSONObject) player).getInt("port") == serverSocket.getLocalPort()) {
                        playerNumber = ((JSONObject) player).getInt("playerNumber");
                        players[playerNumber] = new LocalPlayer(playerNumber);
                    }
                }
            }

            if (playerNumber == -1) {
                throw new InputMismatchException("player number wasn't found");
            }

            ArrayList<Socket> playerSockets = new ArrayList<>();
            playerSockets.add(hostSocket);
            for (int i = 1; i < players.length; i++) {
                if (players[i] != null) {
                    continue;
                }

                if (i < playerNumber) {
                    NetworkPlayer networkPlayer = new NetworkPlayer(i, serverSocket.accept());
                    players[i] = networkPlayer;
                    playerSockets.add(networkPlayer.getPlayerSocket());
                } else if (i > playerNumber) {
                    PlayerInfo infoHolder = null;
                    for (PlayerInfo info_tmp: info) {
                        if (info_tmp.playerNumber == i) {
                            infoHolder = info_tmp;
                            break;
                        }
                    }
                    Socket socket2 = new Socket(infoHolder.ip, infoHolder.port);
                    NetworkPlayer networkPlayer = new NetworkPlayer(infoHolder.playerNumber, socket2);
                    players[i] = networkPlayer;
                    playerSockets.add(networkPlayer.getPlayerSocket());
                }
            }

            JSONObject rdyObject = new JSONObject();
            rdyObject.put("ready", true);
            rdyObject.put("playerIndex", playerNumber);
            for (Socket playerSocket: playerSockets) {
                Writer readyWriter = new OutputStreamWriter(playerSocket.getOutputStream());
                readyWriter.write(rdyObject.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return players;

    }
}
