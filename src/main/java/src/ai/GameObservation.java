package src.ai;

import src.card.Card;
import src.gameEngine.Hand;

/**
 * Class to encapsulate the game state at a particular turn
 */
public class GameObservation implements Cloneable{
    private Hand deck;
    private Hand currentTrick;
    private PlayerState[] playerStates;

    public GameObservation(Hand deck, int playerCount, int initialHandSize) {
        this.deck = deck;
        currentTrick = new Hand();
        playerStates = new PlayerState[playerCount];
        for (int i = 0; i < playerStates.length; i++) {
            playerStates[i] = new PlayerState(i, initialHandSize);
        }
    }

    public GameObservation(Hand deck, Hand currentTrick, PlayerState[] playerStates) {
        this.deck = deck;
        this.currentTrick = currentTrick;
        this.playerStates = playerStates;
    }

    public Hand getDeck() {
        return deck;
    }

    public Hand getCurrentTrick() {
        return currentTrick;
    }

    public PlayerState[] getPlayerStates() {
        return playerStates;
    }

    public GameObservation updateGameState(int playernumber, Card card){
        GameObservation newGameObservation = this.clone();
        newGameObservation.currentTrick.getCard(card);
        newGameObservation.getPlayerStates()[playernumber].addCard(card);
        return newGameObservation;
    }

    @Override
    public GameObservation clone(){
        return new GameObservation(deck.clone(), currentTrick.clone(), playerStates.clone());
    }


}
