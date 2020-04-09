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

    public List<Card> getDeck() {
        return deck;
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
        State state = new State(history.getDeck(), history.getPlayerObservations().size());
        //Add the initial cards we know that each player has.
        for (int i = 0; i < state.getPlayerHands().size(); i++) {
            state.getPlayerHands().get(i).addAll(history.getPlayerObservations().get(i).getCardsPlayed());
        }
        //Shuffle the deck
        shuffle.shuffle(state.getDeck());
        Iterator<Card> deckIterator = state.getDeck().iterator();
        //Iterate over each player, until each player has the correct number of cards.
        for (int i = 0; i < state.getPlayerHands().size(); i++) {
            while (state.getPlayerHands().get(i).size() < history.getPlayerObservations().get(i).getCardsLeft()) {
                state.getPlayerHands().get(i).add(deckIterator.next());
                deckIterator.remove();
            }
        }
        //Update the deck to remove all the cards that were added to players hands.
        return state;
    }
}
