package src.player;

import src.card.Card;
import src.functions.validCards;
import src.gameEngine.Bid;
import src.gameEngine.Hand;
import src.parser.GameDesc;

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

    Player() {
        this.hand = new Hand();
        this.canBePlayed = null;
    }

    public void startHand(StringBuilder trumpSuit) {

    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    /**
     * Initialises the player..
     *
     * @param validCard Predicate that checks if a card is valid.
     */
    public void initPlayer(Predicate<Card> validCard, GameDesc desc, StringBuilder trumpSuit) {
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

    public abstract void broadcastBid(Bid bid, int playerNumber);
}
