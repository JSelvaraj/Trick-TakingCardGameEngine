package src.gameEngine;

public class ContractBid extends Bid {
    boolean redoubling;
    public ContractBid(boolean doubling, String suit, int bidValue, boolean blind, boolean redoubling) {
        super(doubling, suit, bidValue, blind);
        this.redoubling = redoubling;
    }

    public boolean isRedoubling() {
        return redoubling;
    }

    @Override
    public String toString() {
        return "ContractBid{" +
                "redoubling=" + redoubling +
                "doubling=" + super.isDoubling() +
                "suit=" + super.getSuit() +
                "value=" + super.getBidValue() +
                '}';
    }

    public void setRedoubling(boolean redoubling) {
        this.redoubling = redoubling;
    }
}
