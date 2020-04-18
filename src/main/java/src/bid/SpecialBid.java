package src.bid;

import src.bid.Bid;

/**
 * Object to represent special types of bids
 */
public class SpecialBid extends Bid {
    //Spades has a 'Nil' bid where there is an extra bonus if the bid succeeds and a penalty if it fails
    private int pointsGained;
    private int penalty;
    private String trumpSuit;
    private boolean vulnerable;
    private int contractPoints;

    public SpecialBid(int bidValue, int pointsGained, int penalty, boolean blind) {
        super(false,null, bidValue, blind);
        this.pointsGained = pointsGained;
        this.penalty = penalty;
    }

    public SpecialBid(boolean doubling, String suit, int bidValue, boolean blind, int pointsGained, int penalty, String trumpSuit, boolean vulnerable, int contractPoints) {
        super(doubling, suit, bidValue, blind);
        this.pointsGained = pointsGained;
        this.penalty = penalty;
        this.trumpSuit = trumpSuit;
        this.vulnerable = vulnerable;
        this.contractPoints = contractPoints;
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
