package src.card;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Objects;

public class Card {

    private final String SUIT;
    private final String RANK;

    public Card(String suit, String rank) {
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

    public String getJSON() {
        JsonObject card = new JsonObject();
        card.add("rank", new JsonPrimitive(RANK));
        card.add("suit", new JsonPrimitive(SUIT));
        return card.getAsString();
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


