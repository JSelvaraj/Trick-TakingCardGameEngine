package src.functions;

import java.util.function.IntFunction;

public class PlayerIncrementer {

    /**
     * Generate a function that will increment player index correctly, depending on the game specification.
     *
     * @param ascending   If the player count is incremented in an ascending/descending order
     * @param playerCount
     * @return The function to increment the player index.
     */
    public static IntFunction<Integer> generateNextPlayerFunction(boolean ascending, int playerCount) {
        if (ascending) {
            return incrementPlayerAscending(playerCount);
        } else {
            return decrementPlayerAscending(playerCount);
        }
    }

    /**
     * Create a function that will increment players correctly in an ascending order..
     *
     * @param playerCount the total number of players.
     * @return A function that will increment players.
     */
    private static IntFunction<Integer> incrementPlayerAscending(int playerCount) {
        return (currentPlayer) -> (currentPlayer + 1) % playerCount;
    }

    /**
     * Create a function that will increment the players in descending order.
     *
     * @param playerCount the total number of players.
     * @return A function that will decrement players.
     */
    private static IntFunction<Integer> decrementPlayerAscending(int playerCount) {
        return (currentPlayer) -> Math.floorMod(currentPlayer - 1, playerCount);
    }
}
