package src.card;

import java.util.*;

/**
 * Comparator class to define how cards are ranked
 */
public class CardComparator implements Comparator<Card> {

    //TODO: Convert to hashmap similar to suitmap
    /**
     * rankOrder is an array of strings where the position in the array denotes its ranking in game.
     * a higher index in the array indicates a more valuable rank.
     */
    private List<String> rankOrder;

    /**
     * suitMap is a map of the suits and their ranking in the game defined by the game description,
     * a lower value in the map indicates a more valuable suit.
     */
    private Map<String, Integer> suitMap;

    /**
     * Constructor that sets default rankorder
     *
     * @param suitMap Ranking of suits
     */
    public CardComparator(Map<String, Integer> suitMap) {
        String[] ranks = {"TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "JACK", "QUEEN", "KING", "ACE"};
        this.rankOrder = Arrays.asList(ranks);
        this.suitMap = suitMap;
    }

    public CardComparator(Map<String, Integer> suitMap, String[] ranks) {
        this.rankOrder = Arrays.asList(ranks);
        this.suitMap = suitMap;
    }

    /**
     * Constructor that sets rankings
     *
     * @param suitMap Ranking of suits
     */
    public CardComparator(HashMap<String, Integer> suitMap, String[] rankOrder) {
        this.rankOrder = Arrays.asList(rankOrder);
        this.suitMap = suitMap;
    }

    /**
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
        //TODO: Possibly change so that it can handle if there is a game where a cards rank could give greater value than suit.
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
