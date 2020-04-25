package src.bid;

import java.util.Objects;

/**
 * Object to represent a bid
 */
public class Bid{

    private boolean doubling;
    private String suit;
    //Negative if pass
    private int bidValue;
    private boolean blind;
    private boolean vulnerable;
    public static final String NOTRUMP = "NO TRUMP";

    //Spades has a 'blind' type of bid where they don't look at cards before bidding

    public Bid(boolean doubling, String suit, int bidValue, boolean blind, boolean vulnerable) {
        this.doubling = doubling;
        this.suit = suit;
        this.bidValue = bidValue;
        this.blind = blind;
        this.vulnerable = vulnerable;
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

    public void setDoubling(boolean doubling) {
        this.doubling = doubling;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public void setBidValue(int bidValue) {
        this.bidValue = bidValue;
    }

    public void setBlind(boolean blind) {
        this.blind = blind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bid bid = (Bid) o;
        return doubling == bid.doubling &&
                bidValue == bid.bidValue &&
                blind == bid.blind &&
                vulnerable == bid.vulnerable &&
                Objects.equals(suit, bid.suit);
    }

    @Override
    public String toString() {
        return "Bid{" +
                "doubling=" + doubling +
                ", suit='" + suit + '\'' +
                ", bidValue=" + bidValue +
                ", blind=" + blind +
                ", vulnerable=" + vulnerable +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(doubling, suit, bidValue, blind, vulnerable);
    }

    public boolean isVulnerable() {
        return vulnerable;
    }

    public void setVulnerable(boolean vulnerable) {
        this.vulnerable = vulnerable;
    }
}
