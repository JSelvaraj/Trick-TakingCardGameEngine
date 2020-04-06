package src.ai;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import src.card.Card;
import src.card.CardComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrickSimulator {
    private List<Pair<Card, Integer>> trick;
    private CardComparator cardComparator;

    public TrickSimulator(Map<String, Integer> suitOrder) {
        this.trick = new ArrayList<>();
        this.cardComparator = new CardComparator(suitOrder);
    }

    public int evaluateWinner() {
        Pair<Card, Integer> currentWinner = trick.get(0);
        Pair<Card, Integer> card;
        for (int i = 1; i < trick.size(); i++) {
            card = trick.get(i);
            if (cardComparator.compare(card.getLeft(), currentWinner.getLeft()) > 0)
                currentWinner = card;
        }
        return currentWinner.getRight();
    }

    public void addCard(int playerNumber, Card card) {
        trick.add(new ImmutablePair<>(card, playerNumber));
    }
}
