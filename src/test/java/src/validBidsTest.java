package src;
import src.functions.validBids;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class validBidsTest {
    @Test
    void testValidBid() {
        int minBid = 0;
        int maxBid = 10;
        IntPredicate validBid = validBids.isValidBidValue(minBid, maxBid);
        assertTrue(validBid.test(minBid));
        assertTrue(validBid.test(maxBid));
        assertTrue(validBid.test(5));
        assertFalse(validBid.test(minBid - 1));
        assertFalse(validBid.test(maxBid + 1));
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testValidBidExceptions() {
        int minBid = 4;
        int maxBid = 1;
        assertThrows(IllegalArgumentException.class, () -> validBids.isValidBidValue(minBid, maxBid));
    }
}
