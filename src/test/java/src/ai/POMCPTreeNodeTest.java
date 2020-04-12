package src.ai;

import org.junit.jupiter.api.Test;
import src.card.Card;
import src.deck.Deck;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class POMCPTreeNodeTest {

    @Test
    void addNode() {
        List<Card> deck = new Deck().cards;
        Iterator<Card> cardIterator = deck.iterator();
        //Create the initial observation.
        GameObservation observation =  new GameObservation(deck, 4, 13);
        POMCPTreeNode root = new POMCPTreeNode(observation);
        observation.updateGameState(1, cardIterator.next());
        observation.updateGameState(2, cardIterator.next());
        observation.updateGameState(3, cardIterator.next());
        observation.updateGameState(0, cardIterator.next());
        GameObservation newObservation = new GameObservation(observation);
        newObservation.updateGameState(1, cardIterator.next());
        assertTrue(root.addNode(newObservation));
        assertEquals(root.getChildren().get(0).getObservation(), newObservation);
    }

    @Test
    void findNode() {
        List<Card> deck = new Deck().cards;
        Iterator<Card> cardIterator = deck.iterator();
        //Create the initial observation.
        GameObservation observation =  new GameObservation(deck, 4, 13);
        POMCPTreeNode root = new POMCPTreeNode(observation);
        observation.updateGameState(1, cardIterator.next());
        observation.updateGameState(2, cardIterator.next());
        observation.updateGameState(3, cardIterator.next());
        observation.updateGameState(0, cardIterator.next());
        GameObservation newObservation = new GameObservation(observation);
        newObservation.updateGameState(1, cardIterator.next());
        root.addNode(newObservation);
        //Copy constructor used.
        GameObservation nonAddedObservation = new GameObservation(newObservation);
        //Make sure they aren't the same reference.
        assertNotSame(nonAddedObservation, newObservation);
        assertEquals(root.getChildren().get(0), root.findNode(nonAddedObservation));
    }
}