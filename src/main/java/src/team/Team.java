package src.team;

import src.card.Card;
import src.player.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Team {

    int teamNumber;
    int teamName;
    int gameScore;
    int tricksWon;
    int cumulativeScore;
    Player[] players;
    boolean vulnerable;
    int gamesWon;
    private List<Card> cardsWon;

    public Team(Player[] players, int teamNumber) {
        this.players = players;
        this.teamNumber = teamNumber;
        gameScore = 0;
        tricksWon = 0;
        setPlayers(players);
        vulnerable = false;
        gamesWon = 0;
        cumulativeScore = 0;
        cardsWon = new LinkedList<>();
    }

    public void addCardsWon(Collection<Card> cards) {
        this.cardsWon.addAll(cards);
    }

    public List<Card> getCardsWon() {
        return cardsWon;
    }

    private void setPlayers(Player[] players) {
        for (Player player : players) {
            player.setTeam(this);
        }
    }

    public boolean findPlayer(Player player) {
        for (Player playerInTeam : getPlayers()) {
            if (player.getPlayerNumber() == playerInTeam.getPlayerNumber()) {
                return true;
            }
        }
        return false;
    }

    public void printTeam() {
        System.out.println("Team: " + teamNumber);
        for (Player player : players) {
            System.out.println("Player no. : " + player.getPlayerNumber());
        }
    }

    public void setTricksWon(int tricksWon) {
        this.tricksWon = tricksWon;
    }

    public int getTricksWon() {
        return tricksWon;
    }

    public int getGameScore() {
        return gameScore;
    }

    public void setGameScore(int gameScore) {
        this.gameScore = gameScore;
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

    public int getCumulativeScore() {
        return cumulativeScore;
    }

    public void setCumulativeScore(int cumulativeScore) {
        this.cumulativeScore = cumulativeScore;
    }
}


