package src.gameEngine;

import src.card.Card;

import java.util.LinkedList;
import java.util.ListIterator;


/**
 * Object to represent a player's hand of cards
 */
public class Hand implements Cloneable{

    private LinkedList<Card> hand;

    public Hand(LinkedList<Card> hand) {
        this.hand = hand;
    }

    public Hand() {
        this.hand = new LinkedList<>();
    }

    public int getHandSize() {
        return hand.size();
    }


    /**
     * @return string representation of hand
     */
    public String toString() {
        if (hand.size() == 0) return "EMPTY HAND";
        String handString = "";
        ListIterator<Card> iterator = hand.listIterator();
        int i = 0;
        while (iterator.hasNext()) {
            Card card = iterator.next();
            if (card == null) handString += "null";
            else handString += card.toString() + " [" + i + "]";
            if (iterator.hasNext()) handString += ", ";
            i++;
        }
        return handString;
    }

    public Card get(int index) {
        return hand.get(index);
    }


    /**
     * @param num index of card to be 'given' (removed from hand)
     * @return removed card
     */
    public Card giveCard(int num) {
        return this.hand.remove(num);
    }

    public Card giveCard(Card card) {
        return this.hand.remove(this.hand.indexOf(card));
    }


    /**
     * @param card to be added to hand
     */
    public void getCard(Card card) {
        this.hand.add(card);
    }

    public LinkedList<Card> getHand() {
        return hand;
    }

    public void dropHand() {
        this.hand = new LinkedList<>();
    }

    public void dropLast() {
        this.hand.removeLast();
    }

    @Override
    public Hand clone(){
        return new Hand((LinkedList<Card>) hand.clone());
    }
}
