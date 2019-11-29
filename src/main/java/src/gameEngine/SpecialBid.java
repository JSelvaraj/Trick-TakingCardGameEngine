package src.gameEngine;

/**
 * Object to represent special types of bids
 */
public class SpecialBid extends Bid{
    //Spades has a 'Nil' bid where there is an extra bonus if the bid succeeds and a penalty if it fails
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
