package src.ai;

import src.card.Card;
import src.gameEngine.Hand;

import java.util.List;
import java.util.Map;

public class State {
    private Hand deck;
    private Hand[] playerHands;

    public State(Hand deck, int playerCount) {
        this.deck = deck;
        playerHands = new Hand[playerCount];
        for (int i = 0; i < playerCount; i++) {
            playerHands[i] = new Hand();
        }
    }

    public Hand getDeck() {
        return deck;
    }

    public Hand[] getPlayerHands() {
        return playerHands;
    }
}
