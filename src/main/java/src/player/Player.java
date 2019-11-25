package src.player;

import src.card.Card;
import src.gameEngine.Hand;
import java.util.function.Predicate;
import src.functions.validCards;

public abstract class Player {
    private int playerNumber;
    private Hand hand = null;
    private Predicate<Card> validCard;

    public Player(int playerNumber, Predicate<Card> validCard){
        this.playerNumber = playerNumber;
        this.hand = new Hand();
        this.validCard = validCards.getIsCardValidPredicate(this.hand, validCard);
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public Hand getHand() {
        return hand;
    }

    public abstract Card playCard(String trumpSuit, Hand currentTrick);
    public abstract int makeBid();

    public Predicate<Card> getValidCard() {
        return validCard;
    }
}
