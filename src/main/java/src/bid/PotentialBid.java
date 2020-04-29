package src.bid;

import src.player.Player;

public class PotentialBid {
    String bidSuit;
    String bidInput;
    ContractBid adjustedHighestBid;
    Player player;
    boolean firstRound;


    public PotentialBid(String bidSuit, String bidInput, ContractBid adjustedHighestBid, Player player, boolean firstRound) {
        this.bidSuit = bidSuit;
        this.bidInput = bidInput;
        this.adjustedHighestBid = adjustedHighestBid;
        this.player = player;
        this.firstRound = firstRound;
    }

    public String getBidSuit() {
        return bidSuit;
    }

    public String getBidInput() {
        return bidInput;
    }

    public ContractBid getAdjustedHighestBid() {
        return adjustedHighestBid;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isFirstRound() {
        return firstRound;
    }
}
