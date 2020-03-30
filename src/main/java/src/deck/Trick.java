package src.deck;

import src.card.Card;

import java.util.List;

public class Trick {
    private final Card winningCard;
    private final String trumpSuit;
    private final int winningPlayer;
    private List<Card> trick;

    public Trick(Card winningCard, String trumpSuit, int winningPlayer, List<Card> trick) {
        this.winningCard = winningCard;
        this.trumpSuit = trumpSuit;
        this.winningPlayer = winningPlayer;
        this.trick = trick;
    }

    public Card getWinningCard() {
        return winningCard;
    }

    public String getTrumpSuit() {
        return trumpSuit;
    }

    public List<Card> getTrick() {
        return trick;
    }

    @Override
    public String toString() {
        return "Trick{" +
                "winningCard=" + winningCard +
                ", trumpSuit='" + trumpSuit + '\'' +
                ", trick=" + trick.toString() +
                '}';
    }

    public int getWinningPlayer() {
        return winningPlayer;
    }
}
