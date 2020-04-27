package src.ai;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import src.card.Card;
import src.card.CardComparator;
import src.gameEngine.GameEngine;
import src.parser.GameDesc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

/**
 * Class to simulate a trick being played.
 */
public class TrickSimulator { //TODO test this
    private List<Pair<Card, Integer>> trick;
    StringBuilder trumpSuit;
    AtomicBoolean breakflag;

    public TrickSimulator(StringBuilder trumpSuit) {
        this.trick = new ArrayList<>();
        this.trumpSuit = trumpSuit;
    }
    
    public int evaluateWinner(GameDesc desc) {
        Map<String, Integer> suitOrder = GameEngine.generateSuitOrder(desc, trumpSuit, trick.get(0).getLeft());
        CardComparator cardComparator = new CardComparator(suitOrder, desc.getRANKORDER());
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

    public List<Pair<Card, Integer>> getTrick() {
        return trick;
    }



}
