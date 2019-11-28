package src.gameEngine;

/**
 * Object to represent a bid
 */
public class Bid {
    private int bidValue;

    //Spades has a 'blind' type of bid where they don't look at cards before bidding
    private boolean blind;

    public Bid(int bidValue, boolean blind) {
        this.bidValue = bidValue;
        this.blind = blind;
    }

    public int getBidValue() {
        return bidValue;
    }

    public boolean isBlind() {
        return blind;
    }
}
