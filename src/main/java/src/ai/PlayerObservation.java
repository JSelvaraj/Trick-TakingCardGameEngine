package src.ai;

import src.card.Card;

import java.util.LinkedList;
import java.util.List;

/**
 * Encapsulates the state of another player at a specific point in the game, from the POV of another player.
 */
public class PlayerObservation {
    private final int playerIndex;
    private List<Card> cardsPlayed;
    private int cardsLeft;

    public PlayerObservation(int playerIndex, int initialCards) {
        this.playerIndex = playerIndex;
        this.cardsPlayed = new LinkedList<>();
        this.cardsLeft = initialCards;
    }

    /**
     * Add a card to the list of played cards.
     *
     * @param card the card that has been played.
     */
    public void addCard(Card card) {
        cardsPlayed.add(card);
        cardsLeft--;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public List<Card> getCardsPlayed() {
        return cardsPlayed;
    }

    public int getCardsLeft() {
        return cardsLeft;
    }
}
