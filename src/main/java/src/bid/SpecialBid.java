package src.bid;

/**
 * Object to represent special types of bids
 */
public class SpecialBid extends Bid {
    //Spades has a 'Nil' bid where there is an extra bonus if the bid succeeds and a penalty if it fails
    private int bonusPoints;
    private int penalty;
    private String trumpSuit;
    private int contractPoints;
    private int overtrickPoints;
    private int undertrickPoints;
    private int[] undertrickIncrement; //TODO work to give undertrick to opponenent.


    public SpecialBid(int bidValue, int bonusPoints, int penalty, boolean blind) {
        super(false, null, bidValue, blind, false);
        this.bonusPoints = bonusPoints;
        this.penalty = penalty;
    }

    public SpecialBid(boolean doubling, String suit, int bidValue, boolean blind, int bonusPoints, int penalty, String trumpSuit, boolean vulnerable, int contractPoints) {
        super(doubling, suit, bidValue, blind, vulnerable);
        this.bonusPoints = bonusPoints;
        this.penalty = penalty;
        this.trumpSuit = trumpSuit;
        this.contractPoints = contractPoints;
    }

    public SpecialBid(boolean doubling, String suit, int bidValue, boolean blind, boolean vulnerable, int bonusPoints, int penalty, String trumpSuit, int contractPoints, int overtrickPoints, int undertrickPoints, int[] undertrickIncrement) {
        super(doubling, suit, bidValue, blind, vulnerable);
        this.bonusPoints = bonusPoints;
        this.penalty = penalty;
        this.trumpSuit = trumpSuit;
        this.contractPoints = contractPoints;
        this.overtrickPoints = overtrickPoints;
        this.undertrickPoints = undertrickPoints;
        this.undertrickIncrement = undertrickIncrement;
    }

    public int getBidValue() {
        return super.getBidValue();
    }

    public int getBonusPoints() {
        return bonusPoints;
    }

    public int getPenalty() {
        return penalty;
    }

    public boolean isBlind() {
        return super.isBlind();
    }

    /**
     * Tests if this special bid matches a given bid.
     *
     * @param bid The bid that has been made.
     * @return True if the bid matches.
     */
    public boolean bidMatches(Bid bid) {
        return (bid.getBidValue() == super.getBidValue() || super.getBidValue() == -1) &&
                bid.isBlind() == super.isBlind() &&
                bid.isDoubling() == super.isDoubling() &&
                bid.isVulnerable() == super.isVulnerable() &&
                super.getSuit().equals(bid.getSuit());
    }

    public String getTrumpSuit() {
        return trumpSuit;
    }

    public int getContractPoints() {
        return contractPoints;
    }

    public int getOvertrickPoints() {
        return overtrickPoints;
    }

    public int getUndertrickPoints() {
        return undertrickPoints;
    }

    public int[] getUndertrickIncrement() {
        return undertrickIncrement;
    }
}
