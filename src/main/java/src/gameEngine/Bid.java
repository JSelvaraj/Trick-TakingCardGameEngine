package src.gameEngine;

public class Bid {
    private int bidValue;
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
