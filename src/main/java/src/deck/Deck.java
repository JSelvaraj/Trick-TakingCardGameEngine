package src.deck;

import src.card.Card;

import java.util.LinkedList;

/**
 * Object that stores a deck (LinkedList) of Cards
 */
public class Deck {
    public LinkedList<Card> cards;

    public Deck() {
        initStandardDeck();
    }

    public Deck(LinkedList<Card> cards) {
        this.cards = cards;
    }

    public int getDeckSize() {
        return cards.size();
    }


    //Creates Cards based on the suits and ranks provided by the game desc, and adds them to the list.
    public static LinkedList<Card> makeDeck(String[] suits, String[] ranks) {
        LinkedList<Card> deck = new LinkedList<>();
        for (String currentSuit : suits) {
            for (String currentRank : ranks) {
                deck.add(new Card(currentSuit, currentRank));
            }
        }
        return deck;

    }

    //Creates standard deck if no suits and ranks provided.
    private void initStandardDeck() {
        String[] suits = {"CLUBS", "DIAMONDS", "HEARTS", "SPADES"};
        String[] ranks = {"ACE", "KING", "QUEEN", "JACK", "TEN", "NINE", "EIGHT", "SEVEN", "SIX", "FIVE", "FOUR", "THREE", "TWO"};
        cards = makeDeck(suits, ranks);
    }

    //Returns top card from deck
    public Card drawCard() {
        return cards.pop();
    }

    @Override
    public String toString() {
        return cards.toString();
    }
}
