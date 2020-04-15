package src.team;

import src.gameEngine.Bid;
import src.player.Player;

public class Team {

    int teamNumber;
    int teamName;
    int score;
    int tricksWon;
    Player[] players;

    public Team(Player[] players, int teamNumber) {
        this.players = players;
        this.teamNumber = teamNumber;
        score = 0;
        tricksWon = 0;
        setPlayers(players);
    }

    private void setPlayers(Player[] players) {
        for (Player player: players) {
            player.setTeam(this);
        }
    }

    public void printTeam() {
        System.out.println("Team: " + teamNumber);
        for (Player player: players) {
            System.out.println("Player no. : " + player.getPlayerNumber() );
        }
    }

    public void setTricksWon(int tricksWon) {
        this.tricksWon = tricksWon;
    }

    public int getTricksWon() {
        return tricksWon;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Player[] getPlayers() {
        return players;
    }

    public int getTeamNumber() {
        return teamNumber;
    }
}


