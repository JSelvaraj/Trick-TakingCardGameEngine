package src.card;

/**
 * Object to represent a standard card
 */
public class Card {

    private final String SUIT;
    private final String RANK;

    public Card (String suit, String rank) {
        SUIT = suit;
        RANK = rank;
    }

    /**
     * @return rank and card as a string
     */
    @Override
    public String toString() {
        return this.RANK + " OF " + this.SUIT;
    }

    public String getRANK() {
        return RANK;
    }

    public String getSUIT() {
        return SUIT;
    }

    /**
     * @param card - Card to compare
     * @return Checks if the card has the same rank and suit as another card.
     */
    public boolean equals(Card card) {
        return this.SUIT.equals(card.getSUIT()) && this.RANK.equals(card.RANK);
    }
}


