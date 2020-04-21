package src.bid;

import src.bid.ContractBid;

public class PotentialBid {
    String bidSuit;
    String bidInput;
    ContractBid adjustedHighestBid;


    public PotentialBid(String bidSuit, String bidInput, ContractBid adjustedHighestBid) {
        this.bidSuit = bidSuit;
        this.bidInput = bidInput;
        this.adjustedHighestBid = adjustedHighestBid;
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
}
