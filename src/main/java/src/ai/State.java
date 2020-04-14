package src.ai;

import src.card.Card;
import src.deck.Shuffle;

import java.util.*;

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

    public State(State state) {
        this.deck = new LinkedList<>(state.deck);
        this.playerHands = new ArrayList<>(state.deck.size());
        for (int i = 0; i < state.playerHands.size(); i++) {
            playerHands.add(new LinkedList<Card>(state.playerHands.get(i)));
        }
    }

    public List<Card> getDeck() {
        return deck;
    }

    public void playCard(int playerNumber, Card card) {
        if (this.playerHands.get(playerNumber).remove(card)) throw new IllegalArgumentException();
    }

    public void addCard(int playerNumber, Card card) {
        this.playerHands.get(playerNumber).add(card);
    }

    public void addCardCollection(int playerNumber, Collection<? extends Card> cards) {
        this.playerHands.get(playerNumber).addAll(cards);
    }

    public int getNumberOfPlayers() {
        return this.playerHands.size();
    }

    public int getHandSize(int playerNumber) {
        return this.playerHands.get(playerNumber).size();
    }

    public List<List<Card>> getPlayerHands() {
        return playerHands;
    }

    /**
     * Generate a possible state based on a game observation
     *
     * @param history The knowledge known about a game.
     * @return A possible game state based on the observation.
     */
    public static State generateBeliefState(GameObservation history, Shuffle shuffle) {
        State state = new State(history.getCardsRemaining(), history.getPlayerObservations().size());
        //Add the initial cards we know that each player has.
        for (int i = 0; i < state.getNumberOfPlayers(); i++) {
            state.addCardCollection(i, history.getPlayerObservations().get(i).getHasCards());
        }
        //Shuffle the deck
        shuffle.shuffle(state.getDeck());
        Iterator<Card> deckIterator = state.getDeck().iterator();
        //Iterate over each player, until each player has the correct number of cards.
        for (int i = 0; i < state.getNumberOfPlayers(); i++) {
            while (state.getHandSize(i) < (history.getPlayerObservations().get(i).getCardsLeft() - history.getPlayerObservations().get(i).getHasCards().size())) {
                state.addCard(i, deckIterator.next());
                deckIterator.remove();
            }
        }
        //Update the deck to remove all the cards that were added to players hands.
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return deck.equals(state.deck) && ((State) o).playerHands.equals(playerHands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deck, playerHands);
    }
}
