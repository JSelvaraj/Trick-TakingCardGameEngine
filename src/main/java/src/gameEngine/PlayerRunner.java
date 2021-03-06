package src.gameEngine;

import org.java_websocket.WebSocket;
import src.exceptions.InvalidGameDescriptionException;
import src.networking.Networking;
import src.player.Player;

import java.util.concurrent.Semaphore;

public class PlayerRunner implements Runnable {
    private Player player;
    private String hostAddress;
    private int hostPort;
    private int localPort;
    boolean localConnection = false;
    private boolean printMoves = true;
    private WebSocket webSocket = null;
    boolean enableRandomEvents;
    Semaphore lock;

    public PlayerRunner(Player player, String hostAddress, int hostPort, int localPort, boolean localConnection, boolean printMoves, WebSocket webSocket, boolean enableRandomEvents, Semaphore lock) {
        this.player = player;
        this.hostAddress = hostAddress;
        this.hostPort = hostPort;
        this.localPort = localPort;
        this.localConnection = localConnection;
        this.printMoves = printMoves;
        this.webSocket = webSocket;
        this.enableRandomEvents = enableRandomEvents;
        this.lock = lock;
    }

    public PlayerRunner(Player player, String hostAddress, int hostPort, boolean localConnection, boolean printMoves, boolean enableRdmEvents) {
        this(player, hostAddress, hostPort, 0, localConnection, printMoves, null,  enableRdmEvents, null);
    }

    public PlayerRunner(Player player, String hostAddress, int hostPort, boolean localConnection, boolean printMoves) {
        this(player, hostAddress, hostPort, 0, localConnection, printMoves, null, false, null);
    }



    @Override
    public void run() {
        try {
            if (webSocket == null) {
                Networking.connectToGame(this.localPort, this.hostAddress, this.hostPort, player, this.localConnection, printMoves, enableRandomEvents);
            } else {
                System.out.println("CORRECT JOINGAME METHOD CALLED");
                Networking.connectToGame(this.localPort, this.hostAddress, this.hostPort, player, this.localConnection, printMoves, enableRandomEvents, webSocket, lock);
            }
        } catch (InvalidGameDescriptionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
