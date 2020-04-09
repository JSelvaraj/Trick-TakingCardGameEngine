package src.ai;

import src.card.Card;
import src.deck.Shuffle;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CardPOMDP {
    //Random generators.
    private Random random;
    private Shuffle shuffle;
    //The player number of the AI.
    private int playerNumber;
    //Game Information
    private int playerCount;
    private IntFunction<Integer> playerIncrementor;
    private BiFunction<List<Card>, Card, Boolean> validCardFunction;
    private Map<String, Integer> suitOrder;
    StringBuilder trumpSuit;
    //Values for the POMDP procedure.
    private double epsilon = 0.001;
    private double gamma = 1;
    private final double c = 0.5;
    long timeout;

    public CardPOMDP(BiFunction<List<Card>, Card, Boolean> validCards, long timeout, int playerNumber, int playerCount) {
        random = new Random();
        shuffle = new Shuffle(0); //TODO update seed
        this.validCardFunction = validCards;
        this.timeout = timeout;
        this.playerNumber = playerNumber;
        this.playerCount = playerCount;
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
        TrickSimulator trickSimulator = new TrickSimulator(suitOrder, trumpSuit.toString());
        //Find the player who started the trick.
        int currentPlayer = history.getTrickStartedBy();
        //Add the cards that have already been played.
        for (Card card : history.getCurrentTrick()) {
            trickSimulator.addCard(currentPlayer, card);
            //Then go onto the next player.
            currentPlayer = playerIncrementor.apply(currentPlayer);
        }
        //It should now be the player for this AI.
        assert currentPlayer == playerNumber;
        //Play a random action for each player until the trick is complete
        while (trickSimulator.getTrick().size() < playerCount) {
            //Make a random action for the current player.
            List<Card> playerCards = state.getPlayerHands().get(currentPlayer);
            List<Card> validCards = playerCards.stream().filter((card -> validCardFunction.apply(history.getCurrentTrick(), card))).collect(Collectors.toList());
            if (validCards.size() == 0) {
                validCards = playerCards;
            }
            trickSimulator.addCard(currentPlayer, validCards.get(random.nextInt(validCards.size())));
            //Go to the next player
            currentPlayer = playerIncrementor.apply(currentPlayer);
        }
        int winningPlayer = trickSimulator.evaluateWinner();
        //TODO generate new observation for next part of trick and update the state.
        //See if this player won the trick.
        int r = winningPlayer == playerNumber ? 1 : 0;
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
