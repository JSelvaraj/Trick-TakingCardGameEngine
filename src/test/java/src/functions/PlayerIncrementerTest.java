package src.functions;

import org.junit.jupiter.api.Test;

import java.util.function.IntFunction;


import static org.junit.jupiter.api.Assertions.*;

class PlayerIncrementerTest {

    @Test
    void generateNextPlayerFunctionAscending() {
        IntFunction<Integer> nextPlayer = PlayerIncrementer.generateNextPlayerFunction(true, 4);
        assertEquals(1, (int) nextPlayer.apply(0));
        assertEquals(2, (int) nextPlayer.apply(1));
        assertEquals(3, (int) nextPlayer.apply(2));
        assertEquals(0, (int) nextPlayer.apply(3));
    }

    @Test
    void generateNextPlayerFunctionDescending() {
        IntFunction<Integer> nextPlayer = PlayerIncrementer.generateNextPlayerFunction(false, 4);
        assertEquals(0, (int) nextPlayer.apply(1));
        assertEquals(1, (int) nextPlayer.apply(2));
        assertEquals(2, (int) nextPlayer.apply(3));
        assertEquals(3, (int) nextPlayer.apply(0));
    }
}