package src.player;

import src.card.Card;
import src.gameEngine.Hand;

public class NetworkPlayer extends Player {

    public NetworkPlayer(int playerNumber) {
        super(playerNumber);
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
