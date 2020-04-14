package src.ai;

import src.card.Card;

import java.util.ArrayList;
import java.util.Collections;
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
    private boolean done;

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
        this.done = false;
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
        if (currentTrick.size() == 0) {
            trickStartedBy = playernumber;
        }
        currentTrick.add(card);
        playerObservations.get(playernumber).addCardPlayed(card);
        cardSequence.add(card);
    }

    public void addKnownCards(int playerNumber, List<Card> cards){
        playerObservations.get(playerNumber).getHasCards().addAll(cards);
        cardsRemaining.removeAll(cards);
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

    /**
     * Checks if a given observation is a previous history of this observation
     *
     * @param observation The observation to compare against
     * @return True if the given cardSequence of the observation is a subsequence of this observation starting at 0.
     */
    public boolean isPreviousHistory(GameObservation observation) {
        return Collections.indexOfSubList(cardSequence, observation.cardSequence) == 0;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
