package src.card;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Objects;

public class Card {

    private final String SUIT;
    private final String RANK;
    private String specialType;
    private int pointValue;

    public Card(String suit, String rank) {
        SUIT = suit;
        RANK = rank;
    }

    public Card(String SUIT, String RANK, int pointValue) {
        this.SUIT = SUIT;
        this.RANK = RANK;
        this.pointValue = pointValue;
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

    public int getPointValue() {
        return pointValue;
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
        return new Gson().toJson(card);
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
    public void setSpecialType(String specialType) {
        this.specialType = specialType;
    }
    public String getSpecialType() {
        return specialType;
    }
    public static Card fromJson(String card) {
        JsonObject cardJson = new Gson().fromJson(card, JsonObject.class);
        return new Card(cardJson.get("suit").getAsString(), cardJson.get("rank").getAsString());
    }
}


