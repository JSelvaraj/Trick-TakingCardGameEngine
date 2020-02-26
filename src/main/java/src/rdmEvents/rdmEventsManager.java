package src.rdmEvents;

import src.team.Team;

import java.util.ArrayList;
import java.util.Random;

public class rdmEventsManager {

    int maxAcceptableScoreSeparation;
    int scoreThreshold;
    int rdmEventProb;
    int probIncrement;
    String eventChoice;
    Team weakestTeam = null;
    Team strongestTeam = null;

    public rdmEventsManager(int maxAcceptableScoreSeparation, int scoreThreshold, int rdmEventProb, int probIncrement,
                            Team initialTeam1, Team initialTeam2) {
        this.maxAcceptableScoreSeparation = maxAcceptableScoreSeparation;
        this.scoreThreshold = scoreThreshold;
        this.rdmEventProb = rdmEventProb;
        this.probIncrement = probIncrement;
        this.weakestTeam = initialTeam1;
        this.strongestTeam = initialTeam2;
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
            if ((rdmEventProb -= probIncrement) >= 0) {
                rdmEventProb -= probIncrement;
            }
        }
    }

    public rdmEvent eventChooser(String eventPlayTime) {
        if (new Random().nextInt(rdmEventProb) == 0) {
            switch (eventPlayTime) {
                case "TRICK":
                    //choose out of events
                    eventChoice = "SwapHands";
                    return new rdmEvent("SwapHands", weakestTeam, strongestTeam);
                case "HAND":
                    //choose out of events
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

}
