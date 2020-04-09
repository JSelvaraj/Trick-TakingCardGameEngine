package src.ai;

import src.card.Card;

import java.util.List;
import java.util.function.Predicate;

public class CardPOMDP {
    private Predicate<Card> validCards;
    private double epsilon = 0.001;
    private double gamma;
    long timeout;

    private void search(List<GameObservation> history) {
        long startTime = System.nanoTime();
        do {

        } while (System.nanoTime() - startTime < timeout);
    }

    private double rollout(GameObservation state, List<GameObservation> history, int depth) {
        if (Math.pow(gamma, depth) < epsilon) {
            return 0;
        }
        int r = 0;
        return r + gamma * rollout(null, null, depth + 1);
    }

    private double simulate(GameObservation state, List<GameObservation> history, int depth) {
        if (Math.pow(gamma, depth) < epsilon) {
            return 0;
        }
        return rollout(state, history, depth);
    }


}
