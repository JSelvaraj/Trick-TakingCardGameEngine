package src.gameEngine;

import org.java_websocket.WebSocket;
import src.exceptions.InvalidGameDescriptionException;
import src.networking.Networking;
import src.player.Player;

public class PlayerRunner implements Runnable {
    private Player player;
    private String hostAddress;
    private int hostPort;
    private int localPort;
    boolean localConnection = false;
    private boolean printMoves = true;
    WebSocket webSocket;
    boolean enableRandomEvents;

    public PlayerRunner(Player player, String hostAddress, int hostPort, int localPort, boolean localConnection, boolean printMoves, boolean enableRandomEvents, WebSocket webSocket) {
        this.player = player;
        this.hostAddress = hostAddress;
        this.hostPort = hostPort;
        this.localPort = localPort;
        this.localConnection = localConnection;
        this.printMoves = printMoves;
        this.webSocket = webSocket;
        this.enableRandomEvents = enableRandomEvents;
    }

    public PlayerRunner(Player player, String hostAddress, int hostPort, boolean localConnection, boolean printMoves) {
        this(player, hostAddress, hostPort, 0, localConnection, printMoves, false, null);
    }

    @Override
    public void run() {
        try {
            if (webSocket == null) {
            Networking.connectToGame(this.localPort, this.hostAddress, this.hostPort, player, this.localConnection, printMoves, enableRandomEvents);
            } else {
                Networking.connectToGame(this.localPort, this.hostAddress, this.hostPort, player, this.localConnection, printMoves, webSocket);
            }
        } catch (InvalidGameDescriptionException e) {
            e.printStackTrace();
        }
    }


}
