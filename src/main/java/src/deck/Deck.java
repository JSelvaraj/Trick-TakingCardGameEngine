package src.deck;

import org.apache.commons.math3.random.MersenneTwister;
import src.card.Card;

import java.util.LinkedList;

public class Deck {
    public LinkedList<Card> cards;

    public Deck () {
        initStandardDeck();
    }

    public Deck (LinkedList<Card> cards) {
        this.cards = cards;
    }

    public int getDeckSize () {
        return cards.size();
    }

    public static LinkedList<Card> makeDeck(String[] suits, String[] ranks) {
        LinkedList<Card> deck = new LinkedList<>();
        for (String currentSuit: suits) {
            for (String currentRank: ranks) {
                deck.add(new Card(currentSuit, currentRank));
            }
        }
        return deck;

    }

    private void initStandardDeck () {
        String[] suits = {"CLUBS", "DIAMONDS","HEARTS", "SPADES"};
        String[] ranks = {"ACE", "KING", "QUEEN", "JACK", "TEN", "NINE", "EIGHT", "SEVEN", "SIX", "FIVE", "FOUR", "THREE", "TWO"};
        cards = makeDeck(suits, ranks);
    }

    public LinkedList<Card> drawCard (int number) {
        LinkedList<Card> result = new LinkedList<>();
        for (int i = 0; i < number; i++) {
            result.add(cards.pop());
        }
        return result;
    }

    public Card drawCard() {
        return cards.pop();
    }

    public void shuffle (long seed) {
        MersenneTwister twister = new MersenneTwister(seed);
        LinkedList<Card> newDeck = new LinkedList<>();

        while (newDeck.size() < cards.size()) {
            int nextIndex = Math.floorMod(twister.nextInt(),cards.size());
            if (newDeck.indexOf(cards.get(nextIndex)) == -1) newDeck.add(cards.get(nextIndex)); //only new cards are added
        }
        cards = newDeck;
    }

}
