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
    //Search Tree
    private POMCPTreeNode root;

    public CardPOMDP(BiFunction<List<Card>, Card, Boolean> validCards, long timeout, int playerNumber, int playerCount) {
        random = new Random();
        shuffle = new Shuffle(0); //TODO update seed
        this.validCardFunction = validCards;
        this.timeout = timeout;
        this.playerNumber = playerNumber;
        this.playerCount = playerCount;
    }

    private Card search(GameObservation history) {
        //Initialise the search tree if it hasn't already.
        if (root == null) {
            root = new POMCPTreeNode(history);
        }
        long startTime = System.nanoTime();
        int bestScore = -1;
        Card bestAction = null;
        do {
            State gameState = State.generateBeliefState(history, shuffle);
            simulate(gameState, history, 0);
        } while (System.nanoTime() - startTime < timeout);
        return bestAction;
    }

    private double rollout(final State state, final GameObservation history, final int depth) {
        if (Math.pow(gamma, depth) < epsilon) {
            return 0;
        }
        TrickSimulator trickSimulator = new TrickSimulator(suitOrder, trumpSuit.toString());
        //Create new observation and state.
        GameObservation newObservation = new GameObservation(history);
        State newState = new State(state);
        //Find the player who started the trick.
        int currentPlayer = history.getTrickStartedBy();
        //Add the cards that have already been played.
        for (Card card : newObservation.getCurrentTrick()) {
            trickSimulator.addCard(currentPlayer, card);
            //Then go onto the next player.
            currentPlayer = playerIncrementor.apply(currentPlayer);
        }
        //It should now be the player for this AI.
        assert currentPlayer == playerNumber;
        //Play a random action for each player until the trick is complete
        while (trickSimulator.getTrick().size() < playerCount) {
            //Make a random move.
            Card playedCard = makeRandomMove(currentPlayer, newState, newObservation);
            //Add that card to the simulated trick.
            trickSimulator.addCard(currentPlayer, playedCard);
            //Go to the next player
            currentPlayer = playerIncrementor.apply(currentPlayer);
        }
        int winningPlayer = trickSimulator.evaluateWinner();
        //See if this player won the trick.
        int r = winningPlayer == playerNumber ? 1 : 0;
        //Then create an observation for the next trick.
        currentPlayer = winningPlayer;
        //Reset the trick
        newObservation.getCurrentTrick().clear();
        //TODO change for minimum hand size.
        //If the player has no more cards, i.e reaches the end point.
        if (newState.getPlayerHands().get(currentPlayer).size() == 0) {
            return r;
        }
        //Then play out the trick till we reach the next turn of the AI player.
        while (currentPlayer != playerNumber) {
            //Make a random action for the current player.
            Card card = makeRandomMove(currentPlayer, newState, newObservation);
            //Go to the next player
            currentPlayer = playerIncrementor.apply(currentPlayer);
        }
        return r + gamma * rollout(newState, newObservation, depth + 1);
    }

    private double simulate(final State state, final GameObservation observation, final int depth) {
        if (Math.pow(gamma, depth) < epsilon) {
            return 0;
        }
        POMCPTreeNode closestNode = root.findClosestNode(observation);
        //If this history isn't in the tree already.
        if (!closestNode.getObservation().equals(observation)) {
            List<Card> validAction = observation.
        }
        return rollout(state, observation, depth);
    }

    private Card makeRandomMove(int currentPlayer, State state, GameObservation observation) {
        //Make a random action for the current player.
        List<Card> validCards = validMoves(currentPlayer, observation, state);
        Card playedCard = validCards.get(random.nextInt(validCards.size()));
        //Update the state and observation.
        state.getPlayerHands().get(currentPlayer).remove(playedCard);
        observation.updateGameState(playerNumber, playedCard);
        return playedCard;
    }

    private List<Card> validMoves(int playerNumber, final GameObservation observation, final State state) {
        //The cards the player has.
        List<Card> playerCards = state.getPlayerHands().get(playerNumber);
        List<Card> validCards = playerCards.stream().filter((card -> validCardFunction.apply(observation.getCurrentTrick(), card))).collect(Collectors.toList());
        if (validCards.size() == 0) {
            validCards = playerCards;
        }
        return validCards;
    }

}
