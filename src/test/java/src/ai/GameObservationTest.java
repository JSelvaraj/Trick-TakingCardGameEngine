package src.ai;

import org.junit.jupiter.api.Test;
import src.card.Card;
import src.deck.Deck;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameObservationTest {

    @Test
    void isPreviousHistory() {
        List<Card> deck = new Deck().cards;
        Iterator<Card> cardIterator = deck.iterator();
        GameObservation previousHistory = new GameObservation(deck, 4, 13);
        previousHistory.updateGameState(1, cardIterator.next());
        previousHistory.updateGameState(2, cardIterator.next());
        previousHistory.updateGameState(3, cardIterator.next());
        previousHistory.updateGameState(0, cardIterator.next());
        GameObservation newObservation = new GameObservation(previousHistory);
        newObservation.updateGameState(1, cardIterator.next());
        assertTrue(newObservation.isPreviousHistory(previousHistory));
    }
}