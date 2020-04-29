package src.bid;

import src.player.Player;

public class PotentialBid {
    String bidSuit;
    String bidInput;
    ContractBid adjustedHighestBid;
    Player player;
    boolean firstRound;


    public PotentialBid(String bidSuit, String bidInput, ContractBid adjustedHighestBid, Player player, boolean firstRound) {
        this.bidSuit = bidSuit;
        this.bidInput = bidInput;
        this.adjustedHighestBid = adjustedHighestBid;
        this.player = player;
        this.firstRound = firstRound;
    }

    public String getBidSuit() {
        return bidSuit;
    }

    public String getBidInput() {
        return bidInput;
    }

    public ContractBid getAdjustedHighestBid() {
        return adjustedHighestBid;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isFirstRound() {
        return firstRound;
    }

    public Bid toBid(boolean blind) {
        String suit;
        int bidValue;
        if (bidInput.equals("d")) {
            return new Bid(true, null, 0, blind, player.getTeam().isVulnerable());
        } else if (bidInput.equals("-2")) {
            return new Bid(false, null, -2, false, player.getTeam().isVulnerable());
        } else {
            bidValue = Integer.parseInt(bidInput);
            suit = bidSuit;
            return new Bid(false, suit, bidValue, blind, player.getTeam().isVulnerable());
        }
    }

}
