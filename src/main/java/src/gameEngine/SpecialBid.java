package src.gameEngine;

public class SpecialBid {
    private int bidValue;
    private int pointsGained;
    private int penalty;
    private boolean blind;

    public SpecialBid(int bidValue, int pointsGained, int penalty, boolean blind) {
        this.bidValue = bidValue;
        this.pointsGained = pointsGained;
        this.penalty = penalty;
        this.blind = blind;
    }

    public int getBidValue() {
        return bidValue;
    }

    public int getPointsGained() {
        return pointsGained;
    }

    public int getPenalty() {
        return penalty;
    }

    public boolean isBlind() {
        return blind;
    }
}
