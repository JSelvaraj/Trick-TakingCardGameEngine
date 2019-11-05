import java.util.Arrays;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.List;

public class CardComparator implements Comparator<Card> {


    private List<String> suitOrder, rankOrder;

    public CardComparator(String[] suitOrder, String[] rankOrder) {
        this.rankOrder = Arrays.asList(rankOrder);
        this.suitOrder = Arrays.asList(suitOrder);
    }


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

        /* This first compares */
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
