package src.gameEngine;

import src.exceptions.InvalidGameDescriptionException;
import src.networking.Networking;
import src.player.Player;

public class PlayerRunner implements Runnable {
    private Player player;
    private String hostAddress;
    private int hostPort;
    private int localPort;

    public PlayerRunner(Player player, String hostAddress, int hostPort, int localPort) {
        this.player = player;
        this.hostAddress = hostAddress;
        this.hostPort = hostPort;
        this.localPort = localPort;
    }

    public PlayerRunner(Player player, String hostAddress, int hostPort) {
        new PlayerRunner(player, hostAddress, hostPort, 0);
    }

    @Override
    public void run() {
        try {
            Networking.connectToGame(this.localPort, this.hostAddress, this.hostPort, player);
        } catch (InvalidGameDescriptionException e) {
            e.printStackTrace();
        }
    }


}
