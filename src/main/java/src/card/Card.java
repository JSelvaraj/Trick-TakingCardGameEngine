package src.card;


public class Card {

    private final String SUIT;
    private final String RANK;

    public Card (String suit, String rank) {
        SUIT = suit;
        RANK = rank;
    }

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

    public boolean equals(Card card) {
        return this.RANK.equals(card.getSUIT()) && this.RANK.equals(card.RANK);
    }
}


