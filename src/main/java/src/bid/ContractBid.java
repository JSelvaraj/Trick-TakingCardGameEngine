package src.bid;

import src.bid.Bid;
import src.player.Player;
import src.team.Team;

public class ContractBid extends Bid {
    boolean redoubling;
    Player declarer;
    Team team;

    public ContractBid(boolean doubling, String suit, int bidValue, boolean blind, boolean redoubling, boolean vulnerable,
                       Player declarer, Team team) {
        super(doubling, suit, bidValue, blind, vulnerable); //TODO update to support vulnerable
        this.redoubling = redoubling;
        this.declarer = declarer;
        this.team = team;
    }


    @Override
    public String toString() {
        return "ContractBid{" +
                ", redoubling=" + redoubling +
                ", doubling=" + super.isDoubling() +
                ", suit=" + super.getSuit() +
                ", value=" + super.getBidValue() +
                ", declarer=" + declarer.getPlayerNumber() +
                ", team=" + team.getTeamNumber() +
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

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
