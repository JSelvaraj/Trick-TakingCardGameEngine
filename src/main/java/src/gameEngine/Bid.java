package src.gameEngine;

/**
 * Object to represent a bid
 */
public class Bid {

    private boolean doubling;
    private String suit;
    private int bidValue;
    private boolean blind;

    //Spades has a 'blind' type of bid where they don't look at cards before bidding

    public Bid(boolean doubling, String suit, int bidValue, boolean blind) {
        this.doubling = doubling;
        this.suit = suit;
        this.bidValue = bidValue;
        this.blind = blind;
    }

    public int getBidValue() {
        return bidValue;
    }

    public boolean isDoubling() {
        return doubling;
    }

    public String getSuit() {
        return suit;
    }

    public boolean isBlind() {
        return blind;
    }
}
