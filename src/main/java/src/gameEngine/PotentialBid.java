package src.gameEngine;

public class PotentialBid {
    private String bidSuit;
    private String bidInput;
    private ContractBid adjustedHighestBid;


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

    public Bid toBid() {
        return toBid(false);
    }

    public Bid toBid(boolean blind) {
        String suit;
        int bidValue;
        if (bidInput.equals("d")) {
            return new Bid(true, null, 0, blind);
        } else if (bidInput.equals("-2")) {
            return new Bid(false, null, -2, false);
        } else {
            bidValue = Integer.parseInt(bidInput);
            suit = bidSuit;
            return new Bid(false, suit, bidValue, blind);
        }
    }
}

