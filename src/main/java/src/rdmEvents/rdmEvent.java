package src.rdmEvents;

import src.team.Team;

public class rdmEvent {
    String name;
    Team weakestTeam;
    Team strongestTeam;

    public rdmEvent(String name, Team weakestTeam, Team strongestTeam) {
        this.name = name;
        this.weakestTeam = weakestTeam;
        this.strongestTeam = strongestTeam;
    }

}
