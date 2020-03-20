package src.ai;

import src.gameEngine.Hand;

/**
 * Class to encapsulate the game state at a particular turn
 */
public class GameState {
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

    public Hand getDeck() {
        return deck;
    }

    public Hand getCurrentTrick() {
        return currentTrick;
    }

    public PlayerState[] getPlayerStates() {
        return playerStates;
    }
}
