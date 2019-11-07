import java.util.Arrays;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.List;

public class CardComparator implements Comparator<Card> {


    /* suitOrder and rankOrder are arrays of strings where the position in the array denotes its ranking in game.
    *  for suitOrder a lower index indicates a more valuable suit
    *       e.g. in the array {"HEARTS", "CLUBS", "DIAMONDS", "SPADES"} Hearts > Clubs > Diamonds > SPADES
    *  for rankOrder a lower index indicates a less valuable rank
    *       e.g. in the array  {"ACE","TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "JACK", "QUEEN", "KING"}*/
    private List<String> suitOrder, rankOrder;


    public CardComparator(String[] suitOrder, String[] rankOrder) {
        this.rankOrder = Arrays.asList(rankOrder);
        this.suitOrder = Arrays.asList(suitOrder);
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
        if (suitOrder.indexOf(card1.getSUIT()) == -1
                || suitOrder.indexOf(card2.getSUIT()) == -1
                || rankOrder.indexOf(card1.getSUIT()) == -1
                || rankOrder.indexOf(card2.getSUIT()) == -1) {
            throw new InputMismatchException("Rank/Suit does not match accepted Ranks/Suits");
        }

        if (suitOrder.indexOf(card1.getSUIT()) == -1)

        /* This first compares the suits of the cards then the ranks */
        if (suitOrder.indexOf(card1.getSUIT()) < suitOrder.indexOf(card2.getSUIT())) {
            return 1;
        } else if (suitOrder.indexOf(card1.getSUIT()) > suitOrder.indexOf(card2.getSUIT())) {
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
        return 0; // technically impossible to get to the line but for some reason there is a compiler error without it
    }
}
