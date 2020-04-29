package src.networking;

import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import org.java_websocket.WebSocket;
import org.json.JSONArray;
import org.json.JSONObject;
import src.exceptions.InvalidGameDescriptionException;
import src.gameEngine.GameEngine;
import src.parser.GameDesc;
import src.parser.Parser;
import src.player.NetworkPlayer;
import src.player.Player;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Networking {

    private static final int PORTNUMBER = 6969;
    private static final int SEED = new Random().nextInt();
    private static int currentNumberOfPlayers = 1;
    private static final Semaphore hostStarted = new Semaphore(0);

    public static int getCurrentNumberOfPlayers() {
        return currentNumberOfPlayers;
    }

    public static void hostGame(String gameDescFile, int hostPort, Player localPlayer, boolean enableRandomEvents) throws InvalidGameDescriptionException, InterruptedException {
        JSONObject gameJSON = Parser.readJSONFile(gameDescFile);
        Parser parser = new Parser();
        GameDesc gameDesc = parser.parseGameDescription(gameJSON);
        int numberOfPlayers = gameDesc.getNUMBEROFPLAYERS();
        Player[] players = new Player[numberOfPlayers];
        ArrayList<Socket> networkPlayers = new ArrayList<>();
        JSONArray playersJSONArray = new JSONArray();
        Thread broadcast = new Thread(new BroadcastGames(gameDesc.getName(), hostPort, gameDesc.getNUMBEROFPLAYERS()));
        broadcast.start();
        try {
            InetAddress address = InetAddress.getLocalHost();
            JSONObject hostInfo = new JSONObject();
            hostInfo.put("ip", address.getHostAddress());
            hostInfo.put("port", hostPort);
            playersJSONArray.put(hostInfo);
            ServerSocket socket = new ServerSocket(hostPort);
            for (int i = 1; i < players.length; i++) {
                System.out.println("IP: " + address);
                System.out.println("Port: " + socket.getLocalPort());
                NetworkPlayer networkPlayer;
                //Starts the connection and allows local players to connect.
                synchronized (hostStarted) {
                    hostStarted.release();
                    System.out.println("Server started");
                    networkPlayer = new NetworkPlayer(i, socket.accept());
                }
                System.out.println("Connection received");
                players[i] = networkPlayer;
                System.out.println("waiting for player info");
                networkPlayers.add(networkPlayer.getPlayerSocket());
                JsonStreamParser reader = new JsonStreamParser(new InputStreamReader(networkPlayer.getPlayerSocket().getInputStream()));
                JsonElement JSONfile = reader.next();

                System.out.println("File read = " + JSONfile); // debugging sout

                JSONObject object = new JSONObject(JSONfile.getAsJsonObject().toString());
                //object.put("playerNumber", i);
                playersJSONArray.put(object);
                currentNumberOfPlayers++; //required for the game beacon.
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("gathered players");

        //Adds the host player
        players[0] = localPlayer;
        localPlayer.setPlayerNumber(0);
        JSONObject forClients = new JSONObject();

        System.out.println("Sending spec + players + seed");
        forClients.put("spec", gameJSON);
        forClients.put("players", playersJSONArray);
        forClients.put("seed", SEED);

        for (Socket playerSocket : networkPlayers) {
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream(), StandardCharsets.UTF_8));
                out.write(forClients.toString());
                out.flush();
                System.out.println("msg sent");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /* Receives ready message from all the players */
        for (Socket playerSocket : networkPlayers) {
            try {
                System.out.println("Waiting for rdy Message");
                JsonStreamParser reader = new JsonStreamParser(new InputStreamReader(playerSocket.getInputStream()));
                JsonElement rdyString = reader.next();
                JSONObject rdyMsg = new JSONObject(rdyString.getAsJsonObject().toString());
                if (!rdyMsg.getBoolean("ready")) {
                    throw new InputMismatchException("Ready message not correct");
                }
                System.out.println("Recieved ACK from " + rdyMsg.getInt("playerIndex"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Only send ready after you have ready from other players.
        JSONObject rdyObject = new JSONObject();
        rdyObject.put("ready", true);
        rdyObject.put("playerIndex", 0);
        for (Socket playerSocket : networkPlayers) {
            try {
                System.out.println("Sending messages");
                OutputStreamWriter readyWriter = new OutputStreamWriter(playerSocket.getOutputStream());
                readyWriter.write(rdyObject.toString());
                readyWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GameEngine.main(gameDesc, 0, players, SEED, true, enableRandomEvents);


    }

    public static void hostGame(String gameDescFile, int hostPort, Player localPlayer, WebSocket webSocket) throws InvalidGameDescriptionException, InterruptedException {
        JSONObject gameJSON = Parser.readJSONFile(gameDescFile);
        Parser parser = new Parser();
        GameDesc gameDesc = parser.parseGameDescription(gameJSON);
        int numberOfPlayers = gameDesc.getNUMBEROFPLAYERS();
        Player[] players = new Player[numberOfPlayers];
        ArrayList<Socket> networkPlayers = new ArrayList<>();
        JSONArray playersJSONArray = new JSONArray();
        Thread broadcast = new Thread(new BroadcastGames(gameDesc.getName(), hostPort, gameDesc.getNUMBEROFPLAYERS()));
        broadcast.start();
        try {
            InetAddress address = InetAddress.getLocalHost();
            JSONObject hostInfo = new JSONObject();
            hostInfo.put("ip", address.getHostAddress());
            hostInfo.put("port", hostPort);
            playersJSONArray.put(hostInfo);
            ServerSocket socket = new ServerSocket(hostPort);
            for (int i = 1; i < players.length; i++) {
                System.out.println("IP: " + address);
                System.out.println(" Port: " + socket.getLocalPort());
                NetworkPlayer networkPlayer;
                //Starts the connection and allows local players to connect.
                synchronized (hostStarted) {
                    hostStarted.release();
                    System.out.println("Server started");
                    networkPlayer = new NetworkPlayer(i, socket.accept());
                }
                System.out.println("Connection received");
                players[i] = networkPlayer;
                System.out.println("waiting for player info");
                networkPlayers.add(networkPlayer.getPlayerSocket());
                JsonStreamParser reader = new JsonStreamParser(new InputStreamReader(networkPlayer.getPlayerSocket().getInputStream()));
                JsonElement JSONfile = reader.next();

                System.out.println("File read = " + JSONfile); // debugging sout

                JSONObject object = new JSONObject(JSONfile.getAsJsonObject().toString());
                //object.put("playerNumber", i);
                playersJSONArray.put(object);
                currentNumberOfPlayers++; //required for the game beacon.

                //Sends the received player to the GUI
                JSONObject GUImessage = new JSONObject();
//                GUImessage.put("type", "gameplay");
                GUImessage.put("type", "playerjoin");
                GUImessage.put("playerindex", i);
                GUImessage.put("player", object);
                webSocket.send(GUImessage.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("gathered players");

        //Adds the host player
        players[0] = localPlayer;
        localPlayer.setPlayerNumber(0);
        JSONObject forClients = new JSONObject();

        System.out.println("Sending spec + players + seed");
        forClients.put("spec", gameJSON);
        forClients.put("players", playersJSONArray);
        forClients.put("seed", SEED); //TODO change to parser.seed when parser is edited.

        for (Socket playerSocket : networkPlayers) {
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream(), StandardCharsets.UTF_8));
                out.write(forClients.toString());
                out.flush();
                System.out.println("msg sent");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /* Receives ready message from all the players */
        for (Socket playerSocket : networkPlayers) {
            try {
                System.out.println("Waiting for rdy Message");
                JsonStreamParser reader = new JsonStreamParser(new InputStreamReader(playerSocket.getInputStream()));
                JsonElement rdyString = reader.next();
                JSONObject rdyMsg = new JSONObject(rdyString.getAsJsonObject().toString());
                if (!rdyMsg.getBoolean("ready")) {
                    throw new InputMismatchException("Ready message not correct");
                }
                System.out.println("Recieved ACK from " + rdyMsg.getInt("playerIndex"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Only send ready after you have ready from other players.
        JSONObject rdyObject = new JSONObject();
        rdyObject.put("ready", true);
        rdyObject.put("playerIndex", 0);
        for (Socket playerSocket : networkPlayers) {
            try {
                System.out.println("Sending messages");
                OutputStreamWriter readyWriter = new OutputStreamWriter(playerSocket.getOutputStream());
                readyWriter.write(rdyObject.toString());
                readyWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GameEngine.main(gameDesc, 0, players, SEED, true, false, webSocket);


    }

    public static void connectToGame(int localPort, String ip, int port, Player localPlayer, boolean localConnection, boolean printMoves, boolean enableRandomEvents) throws InvalidGameDescriptionException, InterruptedException {
        //Wait for host to start if connecting to a local one.
        if (localConnection) {
            try {
                //Wait to aquire and them immediately release, as it is only need
                hostStarted.acquire();
                hostStarted.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Continue as normal
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
            ServerSocket serverSocket = new ServerSocket(localPort);
            InetAddress address = InetAddress.getLocalHost();
            String addressString = address.getHostAddress();
            JSONObject playerInfo = new JSONObject();
            playerInfo.put("ip", addressString);
            playerInfo.put("port", serverSocket.getLocalPort());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(hostSocket.getOutputStream()));
            writer.write(playerInfo.toString() + "\n"); // send your socket info to host
            writer.flush();
            System.out.println("Sent file");
            System.out.println("Waiting for game description");

            System.out.println("");

            JsonStreamParser reader = new JsonStreamParser(new InputStreamReader(hostSocket.getInputStream())); // get specs, players[], seed from host
            System.out.println("Waiting for JSON file");
            JsonElement JSONfile = reader.next();

            JSONObject fromHost = new JSONObject(JSONfile.getAsJsonObject().toString());

//            System.out.println(fromHost.toString(4));

            JSONObject gameJSON = fromHost.getJSONObject("spec");
            JSONArray playersInfoJSON = fromHost.getJSONArray("players");
            int seed = fromHost.getInt("seed");
            int numberOfPlayers = playersInfoJSON.length();
            ArrayList<PlayerInfo> info = new ArrayList<>(); // will contain the player[] array info from the host json file
            Player[] players = new Player[numberOfPlayers];

            for (int i = 0; i < playersInfoJSON.length(); i++) {
                JSONObject player = playersInfoJSON.getJSONObject(i);
                info.add(new PlayerInfo(player.getString("ip"), player.getInt("port")));
                if (player.getString("ip").equals(addressString) &&
                        player.getInt("port") == serverSocket.getLocalPort()) {
                    playerNumber = i;
                }

            }

            if (playerNumber == -1) {
                throw new InputMismatchException("player number wasn't found");
            }
            //Sets the local player to their position.
            players[playerNumber] = localPlayer;
            localPlayer.setPlayerNumber(playerNumber);
            ArrayList<Socket> playerSockets = new ArrayList<>();
            players[0] = new NetworkPlayer(0, hostSocket);
            playerSockets.add(hostSocket);
            for (int i = 1; i < players.length; i++) {
                System.out.println(i + ":" + playerNumber);
                if (i == playerNumber) { // 0 is host, already connected
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
            System.out.println("Sending messages");
            for (Socket playerSocket : playerSockets) {
                BufferedWriter readyWriter = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
                readyWriter.write(rdyObject.toString());
                readyWriter.flush();
            }
            for (Socket playerSocket : playerSockets) {
                System.out.println("Waiting for Reponses.");
                JsonStreamParser reader2 = new JsonStreamParser(new InputStreamReader(playerSocket.getInputStream()));
                JsonElement rdyMsg = reader2.next();
                JSONObject recACKS = new JSONObject(rdyMsg.getAsJsonObject().toString());
                if (!recACKS.getBoolean("ready")) {
                    throw new InputMismatchException("Ready message not correct");
                }
                int index = recACKS.getInt("playerIndex");
                assert playerSocket == ((NetworkPlayer) players[index]).getPlayerSocket();
                System.out.println("ACK received from " + index);

            }
            Parser parser = new Parser();
            GameDesc gameDesc = parser.parseGameDescription(gameJSON);

            //TODO messaging for joingame protocol
            System.out.println("Starting game");
            GameEngine.main(gameDesc, 0, players, seed, printMoves, enableRandomEvents);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void connectToGame(int localPort, String ip, int port, Player localPlayer, boolean localConnection, boolean printMoves, boolean enableRandomEvents, WebSocket webSocket) throws InvalidGameDescriptionException, InterruptedException {
        //Wait for host to start if connecting to a local one.

        if (localConnection) {
            try {
                //Wait to aquire and them immediately release, as it is only need
                hostStarted.acquire();
                hostStarted.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Continue as normal
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
            ServerSocket serverSocket = new ServerSocket(localPort);
            InetAddress address = InetAddress.getLocalHost();
            String addressString = address.getHostAddress();
            JSONObject playerInfo = new JSONObject();
            playerInfo.put("ip", addressString);
            playerInfo.put("port", serverSocket.getLocalPort());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(hostSocket.getOutputStream()));
            writer.write(playerInfo.toString() + "\n"); // send your socket info to host
            writer.flush();
            System.out.println("Sent file");
            System.out.println("Waiting for game description");

            System.out.println("");

            JsonStreamParser reader = new JsonStreamParser(new InputStreamReader(hostSocket.getInputStream())); // get specs, players[], seed from host
            System.out.println("Waiting for JSON file");
            JsonElement JSONfile = reader.next();

            JSONObject fromHost = new JSONObject(JSONfile.getAsJsonObject().toString());

//            System.out.println(fromHost.toString(4));

            JSONObject gameJSON = fromHost.getJSONObject("spec");
            JSONArray playersInfoJSON = fromHost.getJSONArray("players");
            int seed = fromHost.getInt("seed");
            int numberOfPlayers = playersInfoJSON.length();
            ArrayList<PlayerInfo> info = new ArrayList<>(); // will contain the player[] array info from the host json file
            Player[] players = new Player[numberOfPlayers];

            for (int i = 0; i < playersInfoJSON.length(); i++) {
                JSONObject player = playersInfoJSON.getJSONObject(i);
                info.add(new PlayerInfo(player.getString("ip"), player.getInt("port")));
                if (player.getString("ip").equals(addressString) &&
                        player.getInt("port") == serverSocket.getLocalPort()) {
                    playerNumber = i;

                    //Sends localplayer number to GUI
                    JSONObject playerNumberJSON = new JSONObject();
                    playerNumberJSON.put("type", "playernumber");
                    playerNumberJSON.put("index", playerNumber);
                }

            }

            if (playerNumber == -1) {
                throw new InputMismatchException("player number wasn't found");
            }
            //Sets the local player to their position.
            players[playerNumber] = localPlayer;
            localPlayer.setPlayerNumber(playerNumber);
            ArrayList<Socket> playerSockets = new ArrayList<>();
            players[0] = new NetworkPlayer(0, hostSocket);
            playerSockets.add(hostSocket);
            for (int i = 1; i < players.length; i++) {
                System.out.println(i + ":" + playerNumber);
                if (i == playerNumber) { // 0 is host, already connected
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
            System.out.println("Sending messages");
            for (Socket playerSocket : playerSockets) {
                BufferedWriter readyWriter = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
                readyWriter.write(rdyObject.toString());
                readyWriter.flush();
            }
            for (Socket playerSocket : playerSockets) {
                System.out.println("Waiting for Reponses.");
                JsonStreamParser reader2 = new JsonStreamParser(new InputStreamReader(playerSocket.getInputStream()));
                JsonElement rdyMsg = reader2.next();
                JSONObject recACKS = new JSONObject(rdyMsg.getAsJsonObject().toString());
                if (!recACKS.getBoolean("ready")) {
                    throw new InputMismatchException("Ready message not correct");
                }
                int index = recACKS.getInt("playerIndex");
                assert playerSocket == ((NetworkPlayer) players[index]).getPlayerSocket();
                System.out.println("ACK received from " + index);

            }
            Parser parser = new Parser();
            GameDesc gameDesc = parser.parseGameDescription(gameJSON);

            System.out.println("Starting game");
            GameEngine.main(gameDesc, 0, players, seed, printMoves, enableRandomEvents, webSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
