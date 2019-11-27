package src;
import org.json.JSONArray;
import org.json.JSONObject;
import src.functions.validBids;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import src.gameEngine.Bid;

import java.util.function.BiFunction;
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

    @Test
    void evaluateBidNoSpecialBid() {
        JSONObject bidObject = new JSONObject();
        bidObject.put("pointsPerBid", 10);
        bidObject.put("overtrickPoints", 1);
        bidObject.put("penaltyPoints", 5);
        BiFunction<Bid, Integer, Integer> bidEvaluator = validBids.evaluateBid(bidObject);
        Bid bid1 = new Bid(6, false);
        assertEquals(60, bidEvaluator.apply(bid1, 6).intValue());
        assertEquals(64, bidEvaluator.apply(bid1, 10).intValue());
        assertEquals(-25, bidEvaluator.apply(bid1, 5).intValue());
    }

    @Test
    void evaluateBidSpecialBid() {
        JSONObject bidObject = new JSONObject();
        bidObject.put("pointsPerBid", 10);
        bidObject.put("overtrickPoints", 1);
        bidObject.put("penaltyPoints", 5);
        JSONArray specialBids = new JSONArray();
        //Special bid for blind nil bid
        JSONObject blindNilBid = new JSONObject();
        blindNilBid.put("bidValue", 0);
        blindNilBid.put("pointsGained", 200);
        blindNilBid.put("penalty", 100);
        blindNilBid.put("blindBid", true);
        //Special bid for nil bidding
        JSONObject nilBid = new JSONObject();
        nilBid.put("bidValue", 0);
        nilBid.put("pointsGained", 100);
        nilBid.put("penalty", 50);
        nilBid.put("blindBid", false);
        specialBids.put(blindNilBid);
        specialBids.put(nilBid);
        bidObject.put("specialBids", specialBids);
        BiFunction<Bid, Integer, Integer> bidEvaluator = validBids.evaluateBid(bidObject);
        //Bids to check.
        Bid bidNil = new Bid(0, false);
        Bid bidBlindNil = new Bid(0, true);
        //checks if they evaluate correctly.
        assertEquals(100, bidEvaluator.apply(bidNil, 0).intValue());
        assertEquals(-50, bidEvaluator.apply(bidNil, 1).intValue());
        assertEquals(200, bidEvaluator.apply(bidBlindNil, 0).intValue());
        assertEquals(-100, bidEvaluator.apply(bidBlindNil, 1).intValue());

    }
}
