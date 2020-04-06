package src.ai;

import src.card.Card;
import src.gameEngine.Hand;

/**
 * Class to encapsulate the game state at a particular turn
 */
public class GameState implements Cloneable{
    private Hand deck;
    private Hand currentTrick;
    private PlayerState[] playerStates;

    public GameState(Hand deck, int playerCount, int initialHandSize) {
        this.deck = deck;
        currentTrick = new Hand();
        playerStates = new PlayerState[playerCount];
        for (int i = 0; i < playerStates.length; i++) {
            playerStates[i] = new PlayerState(i, initialHandSize);
        }
    }

    public GameState(Hand deck, Hand currentTrick, PlayerState[] playerStates) {
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

    public GameState updateGameState(int playernumber, Card card){
        GameState newGameState = this.clone();
        newGameState.currentTrick.getCard(card);
        newGameState.getPlayerStates()[playernumber].addCard(card);
        return newGameState;
    }

    @Override
    public GameState clone(){
        return new GameState(deck.clone(), currentTrick.clone(), playerStates.clone());
    }


}
