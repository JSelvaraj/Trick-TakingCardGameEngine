package src.networking;

import jdk.internal.util.xml.impl.Input;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import src.exceptions.InvalidGameDescriptionException;
import src.gameEngine.GameEngine;
import src.parser.GameDesc;
import src.parser.Parser;
import src.player.LocalPlayer;
import src.player.NetworkPlayer;
import src.player.Player;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.InputMismatchException;

public class Networking {

    private static final int PORTNUMBER = 6969;
    private static final int SEED = 420;


    public static void hostGame(String gameDescFile) throws InvalidGameDescriptionException {
        JSONObject gameJSON = Parser.readJSONFile(gameDescFile);
        Parser parser = new Parser();
        GameDesc gameDesc = parser.parseGameDescription(gameJSON);
        int numberOfPlayers = gameDesc.getNUMBEROFPLAYERS();

        Player[] players = new Player[numberOfPlayers];

        ArrayList<Socket> networkPlayers = new ArrayList<>();
        JSONArray playersJSONArray = new JSONArray();
        try {
            InetAddress address = InetAddress.getLocalHost();
            JSONObject hostInfo = new JSONObject();
            hostInfo.put("ip", address);
            hostInfo.put("port", PORTNUMBER);
            playersJSONArray.put(hostInfo);

            for (int i = 1; i < players.length; i++) {
                ServerSocket socket = new ServerSocket(PORTNUMBER);
                System.out.println("IP: " + address);
                System.out.println(" Port: " + socket.getLocalPort());
                NetworkPlayer networkPlayer = new NetworkPlayer(i, socket.accept());
                System.out.println("Connection received");
                players[i] = networkPlayer;
                System.out.println("waiting for player info");
                networkPlayers.add(networkPlayer.getPlayerSocket());
                BufferedReader reader = new BufferedReader(new InputStreamReader(networkPlayer.getPlayerSocket().getInputStream()));
                String JSONfile = reader.readLine();
                System.out.println("File read = " + JSONfile); // debugging sout
                JSONObject object = (JSONObject) new JSONTokener(JSONfile).nextValue();
                //object.put("playerNumber", i);
                playersJSONArray.put(object);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("gathered players");



        players[0] = new LocalPlayer(0);
        JSONObject forClients = new JSONObject();

        System.out.println("Sending spec + players + seed");
        forClients.put("spec", gameJSON);
        forClients.put("players", playersJSONArray);
        forClients.put("seed", SEED); //TODO change to parser.seed when parser is edited.

        for (Socket playerSocket : networkPlayers) {
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream(), StandardCharsets.UTF_8));
                out.write(forClients.toString() + "\n");
                out.flush();
                System.out.println("msg sent");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Socket playerSocket : networkPlayers) {
            try {
                System.out.println("Waiting for rdy Message");
                BufferedReader reader = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
                String rdyString = reader.readLine();
                JSONObject rdyMsg = (JSONObject) new JSONTokener(rdyString).nextValue();
                if (!rdyMsg.getBoolean("ready")) {
                    throw new InputMismatchException("Ready message not correct");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        GameEngine.main(gameDesc, 0, players, SEED);


    }

    public static void connectToGame(String ip, int port) throws InvalidGameDescriptionException {

        class PlayerInfo {
            String ip;
            int port;

            public PlayerInfo(String ip, int port) {
                this.ip = ip;
                this.port = port;
            }

            public PlayerInfo() {

            }
        }

        int playerNumber = -1;
        try {
            Socket hostSocket = new Socket(ip, port); // connect to host
            ServerSocket serverSocket = new ServerSocket(PORTNUMBER);

            JSONObject playerInfo = new JSONObject();
            playerInfo.put("ip", serverSocket.getInetAddress().toString());
            playerInfo.put("port", serverSocket.getLocalPort());

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(hostSocket.getOutputStream()));

            System.out.println("About to write");

            writer.write(playerInfo.toString()); // send your socket info to host
            writer.flush();

            System.out.println("");

            BufferedReader reader = new BufferedReader(new InputStreamReader(hostSocket.getInputStream())); // get specs, players[], seed from host
            System.out.println("Waiting for JSON file");
            String JSONfile = reader.readLine();

            JSONObject fromHost = (JSONObject) new JSONTokener(JSONfile).nextValue();
            JSONObject gameJSON = fromHost.getJSONObject("spec");
            JSONArray playersInfoJSON = fromHost.getJSONArray("players");
            int seed = fromHost.getInt("seed");
            int numberOfPlayers = playersInfoJSON.length();
            ArrayList<PlayerInfo> info = new ArrayList<>(); // will contain the player[] array info from the host json file
            Player[] players = new Player[numberOfPlayers];

            for (int i = 0; i < playersInfoJSON.length(); i++) {
                JSONObject player = playersInfoJSON.getJSONObject(i);
                info.add(new PlayerInfo(player.getString("ip"), player.getInt("port")));
                if (player.getString("ip").equals(serverSocket.getInetAddress().toString()) &&
                        player.getInt("port") == serverSocket.getLocalPort()) {
                    playerNumber = i;
                }

            }

            if (playerNumber == -1) {
                throw new InputMismatchException("player number wasn't found");
            }

            ArrayList<Socket> playerSockets = new ArrayList<>();
            playerSockets.add(hostSocket);
            for (int i = 1; i < players.length; i++) {
                if (players[i] != null) { // 0 is host, already connected
                    continue;
                }

                if (i < playerNumber) {
                    NetworkPlayer networkPlayer = new NetworkPlayer(i, serverSocket.accept());
                    players[i] = networkPlayer;
                    playerSockets.add(networkPlayer.getPlayerSocket());
                } else if (i > playerNumber) {
                    PlayerInfo infoHolder = new PlayerInfo();
                    infoHolder.ip = info.get(i).ip;
                    infoHolder.port = info.get(i).port;
//                    for (PlayerInfo info_tmp : info) {
//                        if (info_tmp.playerNumber == i) {
//                            infoHolder = info_tmp;
//                            break;
//                        }
//                    }
                    Socket socket2 = new Socket(infoHolder.ip, infoHolder.port);
                    NetworkPlayer networkPlayer = new NetworkPlayer(i, socket2);
                    players[i] = networkPlayer;
                    playerSockets.add(networkPlayer.getPlayerSocket());
                }
            }

            JSONObject rdyObject = new JSONObject();
            rdyObject.put("ready", true);
            rdyObject.put("playerIndex", playerNumber);
            for (Socket playerSocket : playerSockets) {
                BufferedWriter readyWriter = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
                readyWriter.write(rdyObject.toString());
            }
            Parser parser = new Parser();
            GameDesc gameDesc = parser.parseGameDescription(gameJSON);
            GameEngine.main(gameDesc, 0, players, seed);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
