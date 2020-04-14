package src.ai;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import src.card.Card;
import src.deck.Shuffle;

import java.util.Comparator;
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
        do {
            State gameState = State.generateBeliefState(history, shuffle);
            simulate(gameState, history, 0);
        } while (System.nanoTime() - startTime < timeout);
        POMCPTreeNode bestNode = root.getChildren().stream().max(Comparator.comparing(POMCPTreeNode::getValue)).orElse(null);
        assert bestNode != null;
        //Return the action associated with the observation.
        return bestNode.getObservation().getCardSequence().get(bestNode.getObservation().getCardSequence().size());
    }

    private Triple<State, GameObservation, Integer> BlackBoxSimulator(State state, GameObservation observation, Card action) {
        TrickSimulator trickSimulator = new TrickSimulator(suitOrder, trumpSuit.toString());
        //Create new observation and state.
        GameObservation newObservation = new GameObservation(observation);
        State newState = new State(state);
        //Find the player who started the trick.
        int currentPlayer = observation.getTrickStartedBy();
        //Add the cards that have already been played.
        for (Card card : newObservation.getCurrentTrick()) {
            trickSimulator.addCard(currentPlayer, card);
            //Then go onto the next player.
            currentPlayer = playerIncrementor.apply(currentPlayer);
        }
        assert currentPlayer == playerNumber;
        trickSimulator.addCard(playerNumber, action);
        currentPlayer = playerIncrementor.apply(currentPlayer);
        //Keep going till all players have played.
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
        int r = (winningPlayer == playerNumber) ? 1 : 0; //TODO add check for teams, as it is still a good move if your teammate wins it.
        currentPlayer = winningPlayer;
        newObservation.getCurrentTrick().clear();
        newObservation.setTrickStartedBy(currentPlayer);
        //TODO change for minimum hand size.
        //If the player has no more cards, i.e reaches the end point.
        if (newState.getPlayerHands().get(currentPlayer).size() == 0) {
            newObservation.setDone(true);
        } else {
            while (currentPlayer != playerNumber) {
                //Make a random action for the current player.
                Card card = makeRandomMove(currentPlayer, newState, newObservation);
                //Go to the next player
                currentPlayer = playerIncrementor.apply(currentPlayer);
            }
        }
        return new ImmutableTriple<>(newState, newObservation, r);
    }

    private double rollout(final State state, final GameObservation history, final int depth) {
        if (Math.pow(gamma, depth) < epsilon) {
            return 0;
        }
        Card playerAction = makeRandomMove(playerNumber, state, history);
        Triple<State, GameObservation, Integer> simulationOutcome = BlackBoxSimulator(state, history, playerAction);
        //Unpack the result.
        State newState = simulationOutcome.getLeft();
        GameObservation newObservation = simulationOutcome.getMiddle();
        int r = simulationOutcome.getRight();
        //If the game is done.
        if (newObservation.isDone()) {
            return r;
        }
        return r + gamma * rollout(newState, newObservation, depth + 1);
    }

    private double simulate(final State state, final GameObservation observation, final int depth) {
        if (Math.pow(gamma, depth) < epsilon) {
            return 0;
        }
        POMCPTreeNode closestNode = root.findClosestNode(observation);
        POMCPTreeNode observationNode;
        //Should not be null.
        assert closestNode != null;
        //If this history isn't in the tree already.
        if (!closestNode.getObservation().equals(observation)) {
            observationNode = new POMCPTreeNode(observation);
            closestNode.getChildren().add(observationNode);
            for (Card validMove : validMoves(playerNumber, observation, state)) {
                GameObservation newObservation = new GameObservation(observation);
                newObservation.updateGameState(playerNumber, validMove);
                //Add the new node to the tree.
                assert observationNode.addNode(newObservation);
            }
            return rollout(state, observation, depth);
        }
        int r = 0;
        observationNode = closestNode;
        //Get the node that seems most promising
        POMCPTreeNode mostPromising = observationNode.getChildren().stream().max(Comparator.comparing((node -> node.getValue() + c * Math.sqrt(Math.log(observationNode.getVisit() / Math.log(node.getVisit())))))).orElse(null);
        //Get the action of that observation.
        assert mostPromising != null;
        Card mostPromisingAction = mostPromising.getObservation().getCardSequence().get(mostPromising.getObservation().getCardSequence().size());
        //Simulate the outcome.
        Triple<State, GameObservation, Integer> simulationOutcome = BlackBoxSimulator(state, observation, mostPromisingAction);
        //Unpack the result.
        State newState = simulationOutcome.getLeft();
        GameObservation newObservation = simulationOutcome.getMiddle();
        r = simulationOutcome.getRight();
        //If the hand isn't finished, then continue the simulation.
        if (!newObservation.isDone()) {
            r += gamma * simulate(newState, newObservation, depth + 1);
        }
        observationNode.incrementVisit();
        mostPromising.incrementVisit();
        mostPromising.increaseValue((r - mostPromising.getValue()) / mostPromising.getVisit());
        return r;
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
