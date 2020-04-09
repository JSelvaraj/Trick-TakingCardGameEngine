package src.ai;

import src.card.Card;
import src.gameEngine.Hand;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class State {
    private List<Card> deck;
    private List<List<Card>> playerHands;

    public State(List<Card> deck, int playerCount) {
        this.deck = new LinkedList<>(deck);
        playerHands = new ArrayList<>(playerCount);
        for (int i = 0; i < playerCount; i++) {
            playerHands.add(new LinkedList<>());
        }
    }

    public List<Card> getDeck() {
        return deck;
    }

    public List<List<Card>> getPlayerHands() {
        return playerHands;
    }
}
