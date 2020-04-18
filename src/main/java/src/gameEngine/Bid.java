package src.gameEngine;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Object to represent a bid
 */
public class Bid{

    private boolean doubling;
    private String suit;
    //Negative if pass
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

    public void setDoubling(boolean doubling) {
        this.doubling = doubling;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public void setBidValue(int bidValue) {
        this.bidValue = bidValue;
    }

    public void setBlind(boolean blind) { this.blind = blind;}

    public String toJson() {
        JsonObject bid = new JsonObject();
        bid.add("type", new JsonPrimitive("bid"));
        bid.add("doubling", new JsonPrimitive(doubling));
        bid.add("suit", new JsonPrimitive(suit));
        bid.add("value", new JsonPrimitive(bidValue));
        bid.add("blindbid", new JsonPrimitive(blind));
        return bid.getAsString();
    }
}
