package src.bid;

import src.bid.Bid;
import src.player.Player;

public class ContractBid extends Bid {
    boolean redoubling;
    Player declarer;
    public ContractBid(boolean doubling, String suit, int bidValue, boolean blind, boolean redoubling, Player declarer) {
        super(doubling, suit, bidValue, blind);
        this.redoubling = redoubling;
        this.declarer = declarer;
    }

    @Override
    public String toString() {
        return "ContractBid{" +
                ", redoubling=" + redoubling +
                ", doubling=" + super.isDoubling() +
                ", suit=" + super.getSuit() +
                ", value=" + super.getBidValue() +
                ", declarer=" + declarer +
                '}';
    }

    public boolean isRedoubling() {
        return redoubling;
    }


    public void setRedoubling(boolean redoubling) {
        this.redoubling = redoubling;
    }

    public Player getDeclarer() {
        return declarer;
    }

    public void setDeclarer(Player declarer) {
        this.declarer = declarer;
    }
}
