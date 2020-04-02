package src.gameEngine;

import org.java_websocket.WebSocket;
import src.exceptions.InvalidGameDescriptionException;
import src.networking.Networking;
import src.player.Player;

public class HostRunner implements Runnable {
    private Player player;
    private int localPort;
    private String gameFile;
    private WebSocket webSocket = null;


    public HostRunner(Player player, int localPort, String gameFile, WebSocket webSocket) {
        this.player = player;
        this.localPort = localPort;
        this.gameFile = gameFile;
        this.webSocket = webSocket;
    }

    public HostRunner(Player player, int localPort, String gameFile) {
        this(player,localPort,gameFile,null);
    }

    public HostRunner(Player player, String gameFile) {
        this(player, 0, gameFile);
    }

    @Override
    public void run() {
        try {
            if (webSocket == null) {
                Networking.hostGame(this.gameFile, this.localPort, this.player);
            } else {
                Networking.hostGame(this.gameFile, this.localPort, this.player, webSocket);
            }
        } catch (InvalidGameDescriptionException e) {
            e.printStackTrace();
        }
    }


}
