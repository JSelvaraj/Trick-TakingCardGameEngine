package src.player;

import src.card.Card;
import src.functions.validCards;
import src.gameEngine.Bid;
import src.gameEngine.Hand;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * Abstract class to represent a player
 */
public abstract class Player {
    private int playerNumber;
    private Hand hand = null;
    private Predicate<Card> canBePlayed;

    Player(int playerNumber, Predicate<Card> canBePlayed) {
        this.playerNumber = playerNumber;
        this.hand = new Hand();
        this.canBePlayed = validCards.getCanBePlayedPredicate(this.hand, canBePlayed);
    }

    Player(int playerNumber) {
        this.playerNumber = playerNumber;
        this.hand = new Hand();
        this.canBePlayed = null;
    }

    /**
     * Initialises the predicate that checks if a move is valid.
     *
     * @param validCard Predicate that checks if a card is valid.
     */
    public void initCanBePlayed(Predicate<Card> validCard) {
        this.canBePlayed = validCards.getCanBePlayedPredicate(this.hand, validCard);
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public Hand getHand() {
        return hand;
    }

    public abstract Card playCard(String trumpSuit, Hand currentTrick);

    public abstract void broadcastPlay(Card card, int playerNumber);

    public abstract Bid makeBid(IntPredicate validBid);

    Predicate<Card> getCanBePlayed() {
        return canBePlayed;
    }

    public void setCanBePlayed(Predicate<Card> canBePlayed) {
        this.canBePlayed = canBePlayed;
    }
}
