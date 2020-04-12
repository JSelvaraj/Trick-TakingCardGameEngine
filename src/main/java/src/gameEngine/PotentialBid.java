package src.gameEngine;

import src.player.Player;

public class PotentialBid {
    String bidSuit;
    String bidInput;
    int currentPlayer;
    Player[] players;
    ContractBid adjustedHighestBid;


    public PotentialBid(String bidSuit, String bidInput, int currentPlayer, Player[] players, ContractBid adjustedHighestBid) {
        this.bidSuit = bidSuit;
        this.bidInput = bidInput;
        this.currentPlayer = currentPlayer;
        this.players = players;
        this.adjustedHighestBid = adjustedHighestBid;
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

    public Player[] getPlayers() {
        return players;
    }

    public ContractBid getAdjustedHighestBid() {
        return adjustedHighestBid;
    }
}
