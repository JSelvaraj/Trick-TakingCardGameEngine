package src.gameEngine;

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
    boolean enableRandomEvents;

    public PlayerRunner(Player player, String hostAddress, int hostPort, int localPort, boolean localConnection, boolean printMoves, boolean enableRandomEvents) {
        this.player = player;
        this.hostAddress = hostAddress;
        this.hostPort = hostPort;
        this.localPort = localPort;
        this.localConnection = localConnection;
        this.printMoves = printMoves;
        this.enableRandomEvents = enableRandomEvents;
    }

    public PlayerRunner(Player player, String hostAddress, int hostPort, boolean localConnection, boolean printMoves, boolean enableRandomEvents) {
        this(player, hostAddress, hostPort, 0, localConnection, printMoves, enableRandomEvents);
    }

    @Override
    public void run() {
        try {
            Networking.connectToGame(this.localPort, this.hostAddress, this.hostPort, player, this.localConnection, printMoves, enableRandomEvents);
        } catch (InvalidGameDescriptionException e) {
            e.printStackTrace();
        }
    }


}
