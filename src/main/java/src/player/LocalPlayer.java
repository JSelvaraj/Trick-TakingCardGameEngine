package src.player;

import src.card.Card;

public class LocalPlayer extends Player {

    public LocalPlayer(int playerNumber) {
        super(playerNumber);
    }

    @Override
    public Card playCard() {
        return null;
    }

    @Override
    public int makeBid() {
        throw new UnsupportedOperationException();
    }
}
