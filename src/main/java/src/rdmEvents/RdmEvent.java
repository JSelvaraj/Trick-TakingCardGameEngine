package src.rdmEvents;

import src.player.Player;
import src.team.Team;

import java.util.Random;

public class RdmEvent {
    String name;
    Team weakestTeam;
    Team strongestTeam;
    Random rand;

    public RdmEvent(String name, Team weakestTeam, Team strongestTeam, Random rand) {
        this.name = name;
        this.weakestTeam = weakestTeam;
        this.strongestTeam = strongestTeam;
        this.rand = rand;
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
}
