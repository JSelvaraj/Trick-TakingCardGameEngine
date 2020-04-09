package src.ai;

import src.card.Card;
import src.deck.Shuffle;
import src.gameEngine.Hand;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class CardPOMDP {
    //Random generators.
    private Random random;
    private Shuffle shuffle;
    //The player number of the AI.
    private int playerNumber;
    private Predicate<Card> validCards;
    private double epsilon = 0.001;
    private double gamma = 1;
    private final double c = 0.5;
    long timeout;

    public CardPOMDP(Predicate<Card> validCards, long timeout, int playerNumber) {
        random = new Random();
        shuffle = new Shuffle(0); //TODO update seed
        this.validCards = validCards;
        this.timeout = timeout;
        this.playerNumber = playerNumber;
    }

    private Card search(GameObservation history) {
        long startTime = System.nanoTime();
        int bestScore = -1;
        Card bestAction = null;
        do {
            State gameState = State.generateBeliefState(history, shuffle);
            simulate(gameState, history, 0);
        } while (System.nanoTime() - startTime < timeout);
        return bestAction;
    }

    private double rollout(State state, GameObservation history, int depth) {
        if (Math.pow(gamma, depth) < epsilon) {
            return 0;
        }
        int r = 0;
        return r + gamma * rollout(null, null, depth + 1);
    }

    private double simulate(State state, GameObservation history, int depth) {
        if (Math.pow(gamma, depth) < epsilon) {
            return 0;
        }
        //If this is the first round.
        if (history.getRound() == 0) {

        }
        return rollout(state, history, depth);
    }

}
