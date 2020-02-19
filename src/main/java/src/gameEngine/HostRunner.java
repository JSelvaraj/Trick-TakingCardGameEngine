package src.gameEngine;

import src.exceptions.InvalidGameDescriptionException;
import src.networking.Networking;
import src.player.Player;

public class HostRunner implements Runnable {
    private Player player;
    private int localPort;
    private String gameFile;

    public HostRunner(Player player, int localPort, String gameFile) {
        this.player = player;
        this.localPort = localPort;
        this.gameFile = gameFile;
    }

    public HostRunner(Player player, String gameFile) {
        this(player, 0, gameFile);
    }

    @Override
    public void run() {
        try {
            Networking.hostGame(this.gameFile, this.localPort, this.player);
        } catch (InvalidGameDescriptionException e) {
            e.printStackTrace();
        }
    }


}
