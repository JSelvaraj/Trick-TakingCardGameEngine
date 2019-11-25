package src.player;

import src.card.Card;
import src.gameEngine.Hand;

import java.util.function.Predicate;

public class NetworkPlayer extends Player {

    public NetworkPlayer(int playerNumber, Predicate<Card> validCard) {
        super(playerNumber, validCard);
    }

    @Override
    public Card playCard(String trumpSuit, Hand currentTrick) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int makeBid() {
        throw new UnsupportedOperationException();
    }
}
