package src.bid;

import src.bid.ContractBid;
import src.player.Player;

public class PotentialBid {
    String bidSuit;
    String bidInput;
    ContractBid adjustedHighestBid;
    Player player;


    public PotentialBid(String bidSuit, String bidInput, ContractBid adjustedHighestBid, Player player) {
        this.bidSuit = bidSuit;
        this.bidInput = bidInput;
        this.adjustedHighestBid = adjustedHighestBid;
        this.player = player;
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
}
