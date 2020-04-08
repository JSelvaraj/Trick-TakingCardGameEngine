package src.rdmEvents;

import javafx.scene.control.RadioButton;
import src.parser.GameDesc;
import src.player.Player;
import src.team.Team;

import java.util.ArrayList;
import java.util.Random;

public class RdmEventsManager {

    int maxAcceptableScoreSeparation;
    int scoreThreshold;
    double rdmEventProb;
    double getRdmEventProbDEFAULT;
    double probIncrement = 0.2;
    Team weakestTeam = null;
    Team strongestTeam = null;
    boolean enabled;
    Random rand;
    Player[] players;
    String[] specialCardTypes = {"BOMB", "HEAVEN"};

    public RdmEventsManager(GameDesc desc, double rdmEventProb,
                            Team initialTeam1, Team initialTeam2, Random rand, Player[] players, boolean enabled) {
        this.maxAcceptableScoreSeparation = 10;
        this.scoreThreshold = 10;
        this.rdmEventProb = rdmEventProb;
        this.getRdmEventProbDEFAULT = rdmEventProb;
        this.weakestTeam = initialTeam1;
        this.strongestTeam = initialTeam2;
        this.enabled = enabled;
        this.rand = rand;
        this.players = players;
    }

    public void checkGameCloseness(ArrayList<Team> teams) {
        int highestScore = 0;
        int lowestScore = scoreThreshold + 1;

        for (Team team : teams) {
            if (team.getScore() < lowestScore) {
                weakestTeam = team;
                lowestScore = team.getScore();
            }
            if (team.getScore() > lowestScore) {
                strongestTeam = team;
                highestScore = team.getScore();
            }
        }

        if (highestScore - lowestScore > maxAcceptableScoreSeparation) {
            System.out.println("Balancing needed");
            if ((rdmEventProb += probIncrement) > 0) {
                rdmEventProb += probIncrement;
            }
            else {
                rdmEventProb = 1;
            }
        }
    }

    public RdmEvent eventChooser(String eventPlayTime) {
        if (enabled && rand.nextDouble() < rdmEventProb) {
            switch (eventPlayTime) {
                case "TRICK":
                    rdmEventProb = getRdmEventProbDEFAULT;
                    return new RdmEvent("SwapCard", weakestTeam, strongestTeam, rand, players);
                    //return new RdmEvent("SwapHands", weakestTeam, strongestTeam);
                case "HAND":
                    rdmEventProb = getRdmEventProbDEFAULT;
                    return new RdmEvent(specialCardTypes[rand.nextInt(specialCardTypes.length)], rand, players);
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

}
