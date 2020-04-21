package src.bid;

import src.bid.ContractBid;
import src.player.Player;

public class PotentialBid {
    String bidSuit;
    String bidInput;
    ContractBid adjustedHighestBid;
    Player player;
    int bidNo;


    public PotentialBid(String bidSuit, String bidInput, ContractBid adjustedHighestBid, Player player, int bidNo) {
        this.bidSuit = bidSuit;
        this.bidInput = bidInput;
        this.adjustedHighestBid = adjustedHighestBid;
        this.player = player;
        this.bidNo = bidNo;
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

    public int getBidNo() {
        return bidNo;
    }
}
