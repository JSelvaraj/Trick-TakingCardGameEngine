package src.gameEngine;

public class PotentialBid {
    String bidSuit;
    String bidInput;
    Bid[] bidTable;
    int currentPlayer;

    public PotentialBid(String bidSuit, String bidInput, Bid[] bidTable, int currentPlayer) {
        this.bidSuit = bidSuit;
        this.bidInput = bidInput;
        this.bidTable = bidTable;
        this.currentPlayer = currentPlayer;
    }

    public String getBidSuit() {
        return bidSuit;
    }

    public String getBidInput() {
        return bidInput;
    }

    public Bid[] getBidTable() {
        return bidTable;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }
}
