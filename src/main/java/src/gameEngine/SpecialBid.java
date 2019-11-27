package src.gameEngine;

public class SpecialBid extends Bid{
    private int pointsGained;
    private int penalty;

    public SpecialBid(int bidValue, int pointsGained, int penalty, boolean blind) {
        super(bidValue, blind);
        this.pointsGained = pointsGained;
        this.penalty = penalty;
    }

    public int getBidValue() {
        return super.getBidValue();
    }

    public int getPointsGained() {
        return pointsGained;
    }

    public int getPenalty() {
        return penalty;
    }

    public boolean isBlind() {
        return super.isBlind();
    }
}
