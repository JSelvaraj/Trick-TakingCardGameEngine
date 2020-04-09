package src.ai;

import src.card.Card;
import src.deck.Shuffle;
import src.gameEngine.Hand;

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
            State gameState = generateBeliefState(history);
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

    /**
     * Generate a possible state based on a game observation
     *
     * @param history The knowledge known about a game.
     * @return A possible game state based on the observation.
     */
    private State generateBeliefState(GameObservation history) {
        State state = new State(history.getDeck(), history.getPlayerObservations().size());
        //Add the initial cards we know that each player has.
        for (int i = 0; i < state.getPlayerHands().size(); i++) {
            state.getPlayerHands().get(i).addAll(history.getPlayerObservations().get(i).getCardsPlayed());
        }
        //Shuffle the deck
        shuffle.shuffle(state.getDeck());
        //Iterate over each player, until each player has the correct number of cards.
        for (int i = 0; i < state.getPlayerHands().size(); i++) {
            while (state.getPlayerHands().get(i).size() < history.getPlayerObservations().get(i).getCardsLeft()){

            }
        }

    }

}
