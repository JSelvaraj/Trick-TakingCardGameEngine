package src.rdmEvents;

import src.player.Player;
import src.team.Team;

import java.util.Random;

public class RdmEvent {
    String name;
    Team weakestTeam;
    Team strongestTeam;
    Random rand;
    int originalPlayer;
    Player[] players;
    String status;


    public RdmEvent(String name, Team weakestTeam, Team strongestTeam, Random rand, String status) {
        this.name = name;
        this.weakestTeam = weakestTeam;
        this.strongestTeam = strongestTeam;
        this.rand = rand;
        this.status = status;
    }

    public RdmEvent(String name, Random rand) {
        this.name = name;
        this.rand = rand;
    }

    public RdmEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Team getWeakestTeam() {
        return weakestTeam;
    }

    public Team getStrongestTeam() {
        return strongestTeam;
    }

    public Random getRand() {
        return rand;
    }

    public int getOriginalPlayer() {
        return originalPlayer;
    }

    public Player[] getPlayers() {
        return players;
    }

    public void setOriginalPlayer(int originalPlayer) {
        this.originalPlayer = originalPlayer;
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
