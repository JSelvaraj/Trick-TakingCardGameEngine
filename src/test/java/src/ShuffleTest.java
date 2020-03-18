package src;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import src.deck.Shuffle;

import static org.junit.jupiter.api.Assertions.*;

class ShuffleTest {

    private List<Integer> list;
    private int seed = 0xDEADBEEF;
    private Shuffle shuffle;

    @BeforeEach
    void setUp() {
        list = new ArrayList<>();
        shuffle = new Shuffle(seed);
    }

    /**
     * Tests that the shuffle works for a one element list.
     */
    @Test
    void shuffle_size_one() {
        list.add(1);
        assertEquals(1, list.size());
        assertEquals(1, (int) list.get(0));
        shuffle.shuffle(list);
        assertEquals(1, list.size());
        assertEquals(1, (int)list.get(0));
    }


    /**
     * Tests that the shuffle works for an empty list.
     */
    @Test
    void shuffle_empty() {
        assertEquals(0, list.size());
        shuffle.shuffle(list);
        assertEquals(0, list.size());
    }

    @Test
    void shuffle() {
        list.add(1);
        list.add(1);
        list.add(1);
        list.add(1);
        list.add(1);
        assertEquals(5, list.size());
        shuffle.shuffle(list);
        assertEquals(5, list.size());
    }
}