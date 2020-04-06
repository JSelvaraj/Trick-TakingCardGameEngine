package src.rdmEvents;

import src.team.Team;

public class RdmEvent {
    String name;
    Team weakestTeam;
    Team strongestTeam;

    public RdmEvent(String name, Team weakestTeam, Team strongestTeam) {
        this.name = name;
        this.weakestTeam = weakestTeam;
        this.strongestTeam = strongestTeam;
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
}
