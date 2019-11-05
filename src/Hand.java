import java.sql.SQLOutput;
import java.util.LinkedList;
import java.util.ListIterator;

public class Hand {

    private LinkedList<Card> hand;

    public  Hand (LinkedList<Card> hand) {
        this.hand = hand;
    }

    public int getHandSize () {
        return hand.size();
    }

    public String toString() {
        if (hand.size() == 0) return "EMPTY HAND";
        String handString = "";
        ListIterator<Card> iterator = hand.listIterator();
        while (iterator.hasNext()) {
            Card card = iterator.next();
            handString += card.toString();
            if (iterator.hasNext()) handString += ", ";
        }
        return handString;
    }

    public Card giveCard (int num) {
        return hand.remove(num - 1);
    }

    public void getCard (Card card) {
        hand.add(card);
    }

    public LinkedList<Card> getHand() {
        return hand;
    }
}
