package src.rdmEvents;

import javafx.scene.control.RadioButton;
import src.team.Team;

import java.util.ArrayList;
import java.util.Random;

public class RdmEventsManager {

    int maxAcceptableScoreSeparation;
    int scoreThreshold;
    int rdmEventProb;
    int getRdmEventProbDEFAULT;
    int probIncrement;
    Team weakestTeam = null;
    Team strongestTeam = null;
    boolean enabled;
    Random rand;

    public RdmEventsManager(int maxAcceptableScoreSeparation, int scoreThreshold, int rdmEventProb, int probIncrement,
                            Team initialTeam1, Team initialTeam2, boolean enabled, Random rand) {
        this.maxAcceptableScoreSeparation = maxAcceptableScoreSeparation;
        this.scoreThreshold = scoreThreshold;
        this.rdmEventProb = rdmEventProb;
        this.getRdmEventProbDEFAULT = rdmEventProb;
        this.probIncrement = probIncrement;
        this.weakestTeam = initialTeam1;
        this.strongestTeam = initialTeam2;
        this.enabled = enabled;
        this.rand = rand;
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
            if ((rdmEventProb -= probIncrement) > 0) {
                rdmEventProb -= probIncrement;
            }
            else {
                rdmEventProb = 1;
            }
        }
    }

    public RdmEvent eventChooser(String eventPlayTime) {
            if (enabled && rand.nextInt(rdmEventProb) == 0) {
                switch (eventPlayTime) {
                    case "MID-TRICK":
                        return null;
                    case "TRICK":
                        rdmEventProb = getRdmEventProbDEFAULT;
                        return new RdmEvent("SwapHands", weakestTeam, strongestTeam);
                    case "HAND":
                        System.out.println("here");
                        rdmEventProb = getRdmEventProbDEFAULT;
                        return new RdmEvent("BombDeck", weakestTeam, strongestTeam);
                    default:
                        return null;
                }
            } else {
                return null;
            }
        }



}
