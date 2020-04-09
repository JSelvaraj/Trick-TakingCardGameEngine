package src.ai;

import src.card.Card;
import src.gameEngine.Hand;

/**
 * Class to encapsulate the game state at a particular turn
 */
public class GameObservation implements Cloneable {
    private int round;
    private Hand deck;
    private Hand cardsRemaining;
    private Hand currentTrick;
    private PlayerObservation[] playerObservations;

    public GameObservation(Hand deck, int playerCount, int initialHandSize) {
        round = 0;
        this.deck = deck;
        currentTrick = new Hand();
        cardsRemaining = deck.clone();
        playerObservations = new PlayerObservation[playerCount];
        for (int i = 0; i < playerObservations.length; i++) {
            playerObservations[i] = new PlayerObservation(i, initialHandSize);
        }
    }

    public GameObservation(Hand deck, Hand currentTrick, Hand cardsRemaining, PlayerObservation[] playerObservations, int round) {
        this.deck = deck;
        this.currentTrick = currentTrick;
        this.cardsRemaining = cardsRemaining;
        this.playerObservations = playerObservations;
        this.round = round;
    }

    public Hand getDeck() {
        return deck;
    }

    public Hand getCurrentTrick() {
        return currentTrick;
    }

    public PlayerObservation[] getPlayerObservations() {
        return playerObservations;
    }

    public GameObservation updateGameState(int playernumber, Card card) {
        GameObservation newGameObservation = this.clone();
        newGameObservation.currentTrick.getCard(card);
        newGameObservation.getPlayerObservations()[playernumber].addCard(card);
        return newGameObservation;
    }

    public void incrementRound(){
        round++;
    }

    public int getRound() {
        return round;
    }

    public Hand getCardsRemaining() {
        return cardsRemaining;
    }

    @Override
    public GameObservation clone() {
        return new GameObservation(deck.clone(), currentTrick.clone(), cardsRemaining.clone(), playerObservations.clone(), round);
    }


}
