package src.gameEngine;

import src.card.*;
import java.util.LinkedList;
import java.util.ListIterator;

public class Hand {

    private LinkedList<Card> hand;

    public  Hand (LinkedList<Card> hand) {
        this.hand = hand;
    }
    public Hand () {
        this.hand = new LinkedList<>();
    }

    public int getHandSize () {
        return hand.size();
    }

    public String toString() {
        if (hand.size() == 0) return "EMPTY HAND";
        String handString = "";
        ListIterator<Card> iterator = hand.listIterator();
        int i = 0;
        while (iterator.hasNext()) {
            Card card = iterator.next();
            handString += card.toString() + " [" + i + "]";
            if (iterator.hasNext()) handString += ", ";
            i++;
        }
        return handString;
    }

    public Card get(int index) {
        return hand.get(index);
    }

    public Card giveCard (int num) {
        return this.hand.remove(num);
    }
    public Card giveCard (Card card) {
        return this.hand.remove(this.hand.indexOf(card));
    }

    public void getCard (Card card) {
        this.hand.add(card);
    }

    public LinkedList<Card> getHand() {
        return hand;
    }

    public void dropHand () {
        this.hand = new LinkedList<>();
    }
}
