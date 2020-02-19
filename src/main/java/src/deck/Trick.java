package src.deck;

import src.card.Card;

import java.util.List;

public class Trick {
    private final Card winningCard;
    private final String trumpSuit;
    private List<Card> trick;

    public Trick(Card winningCard, String trumpSuit, List<Card> trick) {
        this.winningCard = winningCard;
        this.trumpSuit = trumpSuit;
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
}
