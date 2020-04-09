package src.ai;

import org.junit.jupiter.api.Test;
import src.card.Card;
import src.deck.Deck;
import src.deck.Shuffle;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StateTest {

    @Test
    void generateBeliefStateEmptyObservation1() {
        List<Card> deck = new Deck().cards;
        final int handSize = 13;
        final int playerCount = 4;
        final int initialDeckSize = deck.size();
        GameObservation observation = new GameObservation(deck, playerCount, handSize);
        Shuffle shuffle = new Shuffle(0);
        State state = State.generateBeliefState(observation, shuffle);
        for (List<Card> playerHand : state.getPlayerHands()) {
            assertEquals(playerHand.size(), handSize);
        }
        //Check the deck size is now correct.
        assertEquals(initialDeckSize - handSize * playerCount, state.getDeck().size());
        //Check the original deck hasn't been modified.
        assertEquals(initialDeckSize, deck.size());
    }

    @Test
    void generateBeliefStateEmptyObservation2() {
        List<Card> deck = new Deck().cards;
        final int handSize = 6;
        final int playerCount = 2;
        final int initialDeckSize = deck.size();
        GameObservation observation = new GameObservation(deck, playerCount, handSize);
        Shuffle shuffle = new Shuffle(0);
        State state = State.generateBeliefState(observation, shuffle);
        for (List<Card> playerHand : state.getPlayerHands()) {
            assertEquals(playerHand.size(), handSize);
        }
        //Check the deck size is now correct.
        assertEquals(initialDeckSize - handSize * playerCount, state.getDeck().size());
        //Check the original deck hasn't been modified.
        assertEquals(initialDeckSize, deck.size());
    }

    @Test
    void generateBeliefStateNonEmpty() {
        List<Card> deck = new Deck().cards;
        final int handSize = 13;
        final int playerCount = 4;
        GameObservation observation = new GameObservation(deck, playerCount, handSize);
        //Give a player 13 cards already.
        observation.getPlayerObservations().get(2).getCardsPlayed().addAll(deck.subList(0, handSize));
        deck.removeAll(observation.getPlayerObservations().get(2).getCardsPlayed());
        final int initialDeckSize = deck.size();
        Shuffle shuffle = new Shuffle(0);
        State state = State.generateBeliefState(observation, shuffle);
        for (List<Card> playerHand : state.getPlayerHands()) {
            assertEquals(handSize, playerHand.size());
        }
        //Check the deck size is now correct.
        assertEquals(initialDeckSize - handSize * (playerCount - 1), state.getDeck().size());
        //Check the original deck hasn't been modified.
        assertEquals(initialDeckSize, deck.size());
    }
}