package src.gameEngine;

import org.java_websocket.WebSocket;
import src.exceptions.InvalidGameDescriptionException;
import src.networking.Networking;
import src.player.Player;

import java.util.concurrent.Semaphore;

public class HostRunner implements Runnable {
    private Player player;
    private int localPort;
    private String gameFile;
    boolean enableRandomEvents;
    private WebSocket webSocket = null;
    private Semaphore lock;

    public HostRunner(Player player, int localPort, String gameFile, boolean enableRandomEvents, WebSocket webSocket) {
        this.player = player;
        this.localPort = localPort;
        this.gameFile = gameFile;
        this.enableRandomEvents = enableRandomEvents;
        this.webSocket = webSocket;
    }

    public HostRunner(Player player, int localPort, String gameFile, boolean enableRandomEvents, WebSocket webSocket, Semaphore lock) {
        this(player, localPort, gameFile, enableRandomEvents, webSocket);
        this.lock = lock;

    }

    public HostRunner(Player player, int localPort, String gameFile, boolean enableRandomEvents) {
        this.player = player;
        this.localPort = localPort;
        this.gameFile = gameFile;
        this.enableRandomEvents = enableRandomEvents;
    }
    public HostRunner(Player player, int localPort, String gameFile) {
        this(player,localPort,gameFile,false, null);
    }

    public HostRunner(Player player, String gameFile) {
        this(player, 0, gameFile, false, null);
    }

    @Override
    public void run() {
        try {
            if (webSocket == null) {
                Networking.hostGame(this.gameFile, this.localPort, this.player, enableRandomEvents);
            } else {
                Networking.hostGame(this.gameFile, this.localPort, this.player, enableRandomEvents, webSocket, lock);
            }
        } catch (InvalidGameDescriptionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
