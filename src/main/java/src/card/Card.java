package src.card;


import java.util.Objects;

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
        return this.SUIT.equals(card.getSUIT()) && this.RANK.equals(card.RANK);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return SUIT.equals(card.SUIT) &&
                RANK.equals(card.RANK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(SUIT, RANK);
    }
}


