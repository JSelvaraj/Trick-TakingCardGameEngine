package src.player;

import src.card.Card;
import src.gameEngine.Hand;
import java.util.function.Predicate;
import src.functions.validCards;

public abstract class Player {
    private int playerNumber;
    private Hand hand = null;
    private Predicate<Card> canBePlayed;

    public Player(int playerNumber, Predicate<Card> canBePlayed){
        this.playerNumber = playerNumber;
        this.hand = new Hand();
        this.canBePlayed = validCards.getCanBePlayedPredicate(this.hand, canBePlayed);
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    Hand getHand() {
        return hand;
    }

    public abstract Card playCard(String trumpSuit, Hand currentTrick);
    public abstract int makeBid();

    Predicate<Card> getCanBePlayed() {
        return canBePlayed;
    }
}
