package src.ai;

import src.card.Card;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Encapsulates the state of another player at a specific point in the game, from the POV of another player.
 */
public class PlayerObservation implements Cloneable {
    private final int playerIndex;
    private List<Card> cardsPlayed;
    private List<Card> hasCards;
    private int cardsLeft;

    public PlayerObservation(int playerIndex, int initialCards) {
        this.playerIndex = playerIndex;
        this.cardsPlayed = new LinkedList<>();
        this.hasCards = new LinkedList<>();
        this.cardsLeft = initialCards;
    }

    public PlayerObservation(int playerIndex, List<Card> cardsPlayed, List<Card> hasCards, int cardsLeft) {
        this.playerIndex = playerIndex;
        this.cardsPlayed = cardsPlayed;
        this.hasCards = hasCards;
        this.cardsLeft = cardsLeft;
    }

    /**
     * Constructor to copy an observation.
     *
     * @param observation observation to copy from.
     */
    public PlayerObservation(PlayerObservation observation) {
        this.playerIndex = observation.playerIndex;
        this.cardsPlayed = new LinkedList<>();
        Collections.copy(cardsPlayed, observation.cardsPlayed);
        this.hasCards = new LinkedList<>();
        Collections.copy(hasCards, observation.hasCards);
        this.cardsLeft = observation.cardsLeft;
    }

    /**
     * Add a card to the list of played cards.
     *
     * @param card the card that has been played.
     */
    public void addCardPlayed(Card card) {
        cardsPlayed.add(card);
        cardsPlayed.remove(card);
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

    public List<Card> getHasCards() {
        return hasCards;
    }

}
