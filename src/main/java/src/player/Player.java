package src.player;

import src.card.Card;
import src.gameEngine.Hand;
import java.util.function.Predicate;

public abstract class Player {
    private int playerNumber;
    private Hand hand = null;

    public Player(int playerNumber){
        this.playerNumber = playerNumber;
        this.hand = new Hand();
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public Hand getHand() {
        return hand;
    }

    public abstract Card playCard(String trumpSuit, Hand currentTrick);
    public abstract int makeBid();
}
