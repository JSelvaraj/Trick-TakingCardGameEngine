package src.functions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import src.bid.ContractBid;
import src.bid.PotentialBid;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class validBidsBridgeTest {

    static Predicate<PotentialBid> bidValidator;

    @BeforeAll
    static void setup() {
        //Setup bidObject with bridge values
        JSONObject bidObject = new JSONObject();
        bidObject.put("trumpSuitBid", true);
        bidObject.put("ascendingBid", true);
        bidObject.put("minBid", 1);
        bidObject.put("maxBid", 7);
        bidObject.put("suitBidRank", new JSONArray(new String[]{"CLUBS", "DIAMONDS", "HEARTS", "SPADES", "NO TRUMP"}));
        bidObject.put("canPass", true);
        bidObject.put("canDouble", true);
        bidObject.put("canRedouble", true);
        bidValidator = validBids.isValidBidValue(bidObject);
    }

    @Test
    void validateBidFirstBidPass() {
        assertFalse(bidValidator.test(new PotentialBid(null, Integer.toString(-1), null)));
    }

    @Test
    void doubleFirstBid() {
        assertFalse(bidValidator.test(new PotentialBid(null, "d", null)));
    }

    @Test
    void doubleABidInBounds() {
        ContractBid adjustedHighestBid = new ContractBid(false, null, 1, false, false,false, null);
        assertTrue(bidValidator.test(new PotentialBid(null, "d", adjustedHighestBid)));
    }

    @Test
    void doubleABidOutBounds() {
        ContractBid adjustedHighestBid = new ContractBid(false, null, 4, false, false,false, null);
        assertFalse(bidValidator.test(new PotentialBid(null, "d", adjustedHighestBid)));
    }

    @Test
    void reDoubleADoubleInBounds() {
        ContractBid adjustedHighestBid = new ContractBid(true, null, 3, false, false,false, null);
        assertTrue(bidValidator.test(new PotentialBid(null, "d", adjustedHighestBid)));
    }

    @Test
    void reDoubleADoubleOutBounds() {
        ContractBid adjustedHighestBid = new ContractBid(true, null, 4, false, false,false, null);
        assertFalse(bidValidator.test(new PotentialBid(null, "d", adjustedHighestBid)));
    }

    @Test
    void reDoubleAReDouble() {
        ContractBid adjustedHighestBid = new ContractBid(false, null, 4, false, true, false,null);
        assertFalse(bidValidator.test(new PotentialBid(null, "d", adjustedHighestBid)));
    }

    @Test
    void passAfterDouble() {
        ContractBid adjustedHighestBid = new ContractBid(true, null, 4, false, false, false,null);
        assertFalse(bidValidator.test(new PotentialBid(null, "d", adjustedHighestBid)));
    }

    @Test
    void raiseBidIncreaseValue() {
        ContractBid adjustedHighestBid = new ContractBid(false, "NO TRUMP", 4, false, false, false,null);
        assertTrue(bidValidator.test(new PotentialBid("NO TRUMP", Integer.toString(5), adjustedHighestBid)));
    }

    @Test
    void raiseBidIncreaseSuit() {
        ContractBid adjustedHighestBid = new ContractBid(false, "CLUBS", 4, false, false, false,null);
        assertTrue(bidValidator.test(new PotentialBid("SPADES", Integer.toString(4), adjustedHighestBid)));
    }

    @Test
    void badRaiseBidIncreaseSuit1() {
        ContractBid adjustedHighestBid = new ContractBid(false, "SPADES", 4, false, false, false,null);
        assertFalse(bidValidator.test(new PotentialBid("SPADES", Integer.toString(4), adjustedHighestBid)));
    }

    @Test
    void badRaiseBidIncreaseValue() {
        ContractBid adjustedHighestBid = new ContractBid(false, "SPADES", 4, false, false, false,null);
        assertFalse(bidValidator.test(new PotentialBid("SPADES", Integer.toString(3), adjustedHighestBid)));
    }



}
