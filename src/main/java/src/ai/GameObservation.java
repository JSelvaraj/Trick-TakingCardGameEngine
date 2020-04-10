package src.ai;

import src.card.Card;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to encapsulate the game state at a particular turn
 */
public class GameObservation implements Cloneable {
    private int round;
    private int trickStartedBy;
    private List<Card> deck;
    private List<Card> cardsRemaining;
    private List<Card> currentTrick;
    private List<PlayerObservation> playerObservations;
    private List<Card> cardSequence;

    public GameObservation(List<Card> deck, int playerCount, int initialHandSize) {
        round = 0;
        this.deck = deck;
        currentTrick = new LinkedList<>();
        cardsRemaining = new LinkedList<>(deck);
        playerObservations = new ArrayList<>(playerCount);
        cardSequence = new ArrayList<>(deck.size());
        for (int i = 0; i < playerCount; i++) {
            playerObservations.add(new PlayerObservation(i, initialHandSize));
        }
    }

    public GameObservation(GameObservation gameObservation) {
        this.deck = new LinkedList<>(gameObservation.deck);
        this.currentTrick = new LinkedList<>(gameObservation.currentTrick);
        this.cardsRemaining = new LinkedList<>(gameObservation.cardsRemaining);
        this.playerObservations = new ArrayList<>(gameObservation.playerObservations);
        this.cardSequence = new ArrayList<>(gameObservation.cardSequence);
        this.round = gameObservation.round;
    }

    public List<Card> getDeck() {
        return deck;
    }

    public List<Card> getCurrentTrick() {
        return currentTrick;
    }

    public List<PlayerObservation> getPlayerObservations() {
        return playerObservations;
    }

    public void updateGameState(int playernumber, Card card) {
        currentTrick.add(card);
        playerObservations.get(playernumber).addCardPlayed(card);
        cardSequence.add(card);
    }

    public void incrementRound() {
        round++;
    }

    public int getRound() {
        return round;
    }

    public List<Card> getCardsRemaining() {
        return cardsRemaining;
    }

    public int getTrickStartedBy() {
        return trickStartedBy;
    }

    public void setTrickStartedBy(int trickStartedBy) {
        this.trickStartedBy = trickStartedBy;
    }

    public List<Card> getCardSequence() {
        return cardSequence;
    }
}
