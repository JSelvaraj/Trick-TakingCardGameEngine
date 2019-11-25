package src.player;

import src.card.Card;

public class NetworkPlayer extends Player {

    public NetworkPlayer(int playerNumber) {
        super(playerNumber);
    }

    @Override
    public Card playCard() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int makeBid() {
        throw new UnsupportedOperationException();
    }
}
