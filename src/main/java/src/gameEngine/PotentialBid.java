package src.gameEngine;

import src.team.Team;

import java.util.ArrayList;

public class PotentialBid {
    String bidSuit;
    String bidInput;
    int currentPlayer;
    ArrayList<Team> teams;


    public PotentialBid(String bidSuit, String bidInput, int currentPlayer, ArrayList<Team> teams) {
        this.bidSuit = bidSuit;
        this.bidInput = bidInput;
        this.currentPlayer = currentPlayer;
        this.teams = teams;
    }

    public String getBidSuit() {
        return bidSuit;
    }

    public String getBidInput() {
        return bidInput;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public ArrayList<Team> getTeams() {
        return teams;
    }
}
