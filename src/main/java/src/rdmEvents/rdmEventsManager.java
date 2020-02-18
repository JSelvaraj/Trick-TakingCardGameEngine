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

    public rdmEventsManager(int maxAcceptableScoreSeparation, int scoreThreshold, int rdmEventProb, int probIncrement) {
        this.maxAcceptableScoreSeparation = maxAcceptableScoreSeparation;
        this.scoreThreshold = scoreThreshold;
        this.rdmEventProb = rdmEventProb;
        this.probIncrement = probIncrement;
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
            if ((rdmEventProb -= probIncrement) >= 0){
                rdmEventProb -= probIncrement;
            }
        }
    }

    public rdmEvent eventChooser() {
        if (new Random().nextInt(rdmEventProb) == 0) {
            //choose out of possible events
            eventChoice = "SwapHands";
            return new rdmEvent("SwapHands", weakestTeam, strongestTeam);
        }
        else {
            return null;
        }
    }
}
