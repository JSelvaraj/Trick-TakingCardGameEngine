package src.card;

import java.util.*;

public class CardComparator implements Comparator<Card> {


    /* suitOrder and rankOrder are arrays of strings where the position in the array denotes its ranking in game.
    *  for suitOrder a lower index indicates a more valuable suit
    *       e.g. in the array {"HEARTS", "CLUBS", "DIAMONDS", "SPADES"} Hearts > Clubs > Diamonds > SPADES
    *  for rankOrder a lower index indicates a less valuable rank
    *       e.g. in the array  {"ACE","TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "JACK", "QUEEN", "KING"}*/
    private List<String> suitOrder;
    private List<String> rankOrder ;

    private HashMap<String,Integer> suitMap;

    public CardComparator(String[] suitOrder, String[] rankOrder) {
        this.rankOrder = Arrays.asList(rankOrder);
        this.suitOrder = Arrays.asList(suitOrder);
    }

    public CardComparator(String[] suitOrder) {
        String[] ranks = {"TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "JACK", "QUEEN", "KING","ACE"};
        this.rankOrder = Arrays.asList(ranks);
        this.suitOrder = Arrays.asList(suitOrder);
    }

    public CardComparator(HashMap<String, Integer> suitMap) {
        String[] ranks = {"TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "JACK", "QUEEN", "KING","ACE"};
        this.rankOrder = Arrays.asList(ranks);
        this.suitMap = suitMap;
    }

    public CardComparator(HashMap<String, Integer> suitMap, String[] rankOrder) {
        this.rankOrder = Arrays.asList(rankOrder);
        this.suitMap = suitMap;
    }




    /**
     *
     * @param card1 the first card object
     * @param card2 the card that the first card object is being compared to
     * @return 1 if card1 > card2, 0 if card1 == card2, -1 card1 < card 2
     * @throws InputMismatchException
     */
    @Override
    public int compare(Card card1, Card card2) throws InputMismatchException {

        /* If the rank/suit provided to the comparator doesn't match the names of the ranks/suits used to
        *  make the comparator, then it throws an error */
        if (!suitMap.containsKey(card1.getSUIT())
                || !suitMap.containsKey(card2.getSUIT())
                || !rankOrder.contains(card1.getRANK())
                || !rankOrder.contains(card2.getRANK())) {
            throw new InputMismatchException("Rank/Suit does not match accepted Ranks/Suits");
        }


        /* This first compares the suits of the cards then the ranks */
        if (suitMap.get(card1.getSUIT()) < suitMap.get(card2.getSUIT())) {
            return 1;
        } else if (suitMap.get(card1.getSUIT()) > suitMap.get(card2.getSUIT())) {
            return -1;
        } else {
            if (rankOrder.indexOf(card1.getRANK()) < rankOrder.indexOf(card2.getRANK())) {
                return -1;
            } else if (rankOrder.indexOf(card1.getRANK()) > rankOrder.indexOf(card2.getRANK())) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
