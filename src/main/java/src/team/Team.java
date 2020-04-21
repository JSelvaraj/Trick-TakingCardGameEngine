package src.team;

import src.player.Player;

public class Team {

    int teamNumber;
    int teamName;
    int score;
    int tricksWon;
    Player[] players;
    boolean vulnerable;
    int gamesWon;

    public Team(Player[] players, int teamNumber) {
        this.players = players;
        this.teamNumber = teamNumber;
        score = 0;
        tricksWon = 0;
        setPlayers(players);
        vulnerable = false;
        gamesWon = 0;
    }

    private void setPlayers(Player[] players) {
        for (Player player: players) {
            player.setTeam(this);
        }
    }

    public boolean findPlayer(Player player) {
        for (Player playerInTeam: getPlayers()) {
            if (player.getPlayerNumber() == playerInTeam.getPlayerNumber()) {
                return true;
            }
        }
        return false;
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

    public boolean isVulnerable() {
        return vulnerable;
    }

    public void setVulnerable(boolean vulnerable) {
        this.vulnerable = vulnerable;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }
}


