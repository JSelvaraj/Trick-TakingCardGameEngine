import org.apache.commons.math3.random.MersenneTwister;

import java.util.LinkedList;

public class Deck {
    private LinkedList<Card> cards;

    public Deck () {
        initStandardDeck();
    }

    public Deck (LinkedList cards) {
        this.cards = cards;
    }

    public int getDeckSize () {
        return cards.size();
    }

    public void initStandardDeck () {
        cards = new LinkedList<>();
        String[] suits = {"HEARTS", "CLUBS", "DIAMONDS", "SPADES"};
        String[] ranks = {"ACE","TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "JACK", "QUEEN", "KING"};
        for (String currentSuit: suits) {
            for (String currentRank: ranks) {
                cards.add(new Card(currentRank, currentSuit));
            }
        }
    }

    public LinkedList<Card> drawCard (int number) {
        LinkedList<Card> result = new LinkedList<>();
        for (int i = 0; i < number; i++) {
            result.add(cards.pop());
        }
        return result;
    }

    public Card drawCard() {
        return drawCard(1).pop();
    }

    public void shuffle (long seed) {
        MersenneTwister twister = new MersenneTwister(seed);
        LinkedList<Card> newDeck = new LinkedList<>();

        while (newDeck.size() < cards.size()) {
            int nextIndex = twister.nextInt() % cards.size();
            if (newDeck.indexOf(cards.get(nextIndex)) == -1) newDeck.add(cards.get(nextIndex)); //only new cards are added
        }
        cards = newDeck;
    }
}
