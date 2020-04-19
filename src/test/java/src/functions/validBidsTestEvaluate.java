package src.functions;

import jdk.nashorn.internal.parser.JSONParser;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONTokener;
import org.junit.jupiter.api.Test;
import src.bid.Bid;
import src.bid.ContractBid;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiFunction;

public class validBidsTestEvaluate {


    @Test
    void evaluateBidNoSpecialBid() {
        JSONObject bidObject = new JSONObject();
        bidObject.put("pointsPerBid", 10);
        bidObject.put("overtrickPoints", 1);
        bidObject.put("penaltyPoints", 5);
        BiFunction<Bid, Integer, Integer> bidEvaluator = validBids.evaluateBid(bidObject);
        Bid bid1 = new Bid(false, null, 6, false, false);
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
        blindNilBid.put("bonusPoints", 200);
        blindNilBid.put("penalty", 100);
        blindNilBid.put("blind", true);
        //Special bid for nil bidding
        JSONObject nilBid = new JSONObject();
        nilBid.put("bidValue", 0);
        nilBid.put("bonusPoints", 100);
        nilBid.put("penalty", 50);
        nilBid.put("blind", false);
        specialBids.put(blindNilBid);
        specialBids.put(nilBid);
        bidObject.put("specialBids", specialBids);
        BiFunction<Bid, Integer, Integer> bidEvaluator = validBids.evaluateBid(bidObject);
        //Bids to check.
        Bid bidNil = new Bid(false, null, 0, false, false);
        Bid bidBlindNil = new Bid(false, null, 0, true, false);
        //checks if they evaluate correctly.
        assertEquals(100, bidEvaluator.apply(bidNil, 0).intValue());
        assertEquals(-50, bidEvaluator.apply(bidNil, 1).intValue());
        assertEquals(200, bidEvaluator.apply(bidBlindNil, 0).intValue());
        assertEquals(-100, bidEvaluator.apply(bidBlindNil, 1).intValue());

    }

    @Test
    void evaluateBridgeBid1() {
        BiFunction<Bid, Integer, Integer> bidEvaluator = importBidFunction();
        ContractBid bid = new ContractBid(false, "CLUBS", 7, false, false, null);
        assertEquals(650, (int) bidEvaluator.apply(bid, 0));
        assertEquals(600, (int) bidEvaluator.apply(bid, 1));
        assertEquals(550, (int) bidEvaluator.apply(bid, 2));
        assertEquals(500, (int) bidEvaluator.apply(bid, 3));
        assertEquals(450, (int) bidEvaluator.apply(bid, 4));
        assertEquals(400, (int) bidEvaluator.apply(bid, 5));
        assertEquals(350, (int) bidEvaluator.apply(bid, 6));
        assertEquals(300, (int) bidEvaluator.apply(bid, 7));
        assertEquals(250, (int) bidEvaluator.apply(bid, 8));
        assertEquals(200, (int) bidEvaluator.apply(bid, 9));
        assertEquals(150, (int) bidEvaluator.apply(bid, 10));
        assertEquals(100, (int) bidEvaluator.apply(bid, 11));
        assertEquals(50, (int) bidEvaluator.apply(bid, 12));
        assertEquals(140, (int) bidEvaluator.apply(bid, 13));
    }

    @Test
    void evaluateBridgeBid2() {
        BiFunction<Bid, Integer, Integer> bidEvaluator = importBidFunction();
        ContractBid bid = new ContractBid(true, "CLUBS", 7, false, false, null);
        assertEquals(3400, (int) bidEvaluator.apply(bid, 0));
        assertEquals(3100, (int) bidEvaluator.apply(bid, 1));
        assertEquals(2900, (int) bidEvaluator.apply(bid, 2));
        assertEquals(2600, (int) bidEvaluator.apply(bid, 3));
        assertEquals(2300, (int) bidEvaluator.apply(bid, 4));
        assertEquals(2000, (int) bidEvaluator.apply(bid, 5));
        assertEquals(1700, (int) bidEvaluator.apply(bid, 6));
        assertEquals(1400, (int) bidEvaluator.apply(bid, 7));
        assertEquals(1100, (int) bidEvaluator.apply(bid, 8));
        assertEquals(800, (int) bidEvaluator.apply(bid, 9));
        assertEquals(500, (int) bidEvaluator.apply(bid, 10));
        assertEquals(300, (int) bidEvaluator.apply(bid, 11));
        assertEquals(100, (int) bidEvaluator.apply(bid, 12));
        assertEquals(280, (int) bidEvaluator.apply(bid, 13));
    }

    @Test
    void evaluateBridgeBid3() {
        BiFunction<Bid, Integer, Integer> bidEvaluator = importBidFunction();
        ContractBid bid = new ContractBid(true, "CLUBS", 3, false, false, null);
        assertEquals(2300, (int) bidEvaluator.apply(bid, 0));
        assertEquals(2000, (int) bidEvaluator.apply(bid, 1));
        assertEquals(1700, (int) bidEvaluator.apply(bid, 2));
        assertEquals(1400, (int) bidEvaluator.apply(bid, 3));
        assertEquals(1100, (int) bidEvaluator.apply(bid, 4));
        assertEquals(800, (int) bidEvaluator.apply(bid, 5));
        assertEquals(500, (int) bidEvaluator.apply(bid, 6));
        assertEquals(300, (int) bidEvaluator.apply(bid, 7));
        assertEquals(100, (int) bidEvaluator.apply(bid, 8));
        assertEquals(120, (int) bidEvaluator.apply(bid, 9));
        assertEquals(220, (int) bidEvaluator.apply(bid, 10));
        assertEquals(320, (int) bidEvaluator.apply(bid, 11));
        assertEquals(420, (int) bidEvaluator.apply(bid, 12));
        assertEquals(520, (int) bidEvaluator.apply(bid, 13));
    }

    @Test
    void evaluateBridgeBid4() {
        BiFunction<Bid, Integer, Integer> bidEvaluator = importBidFunction();
        ContractBid bid = new ContractBid(false, "NO TRUMP", 5, false, false, null);
        assertEquals(550, (int) bidEvaluator.apply(bid, 0));
        assertEquals(500, (int) bidEvaluator.apply(bid, 1));
        assertEquals(450, (int) bidEvaluator.apply(bid, 2));
        assertEquals(400, (int) bidEvaluator.apply(bid, 3));
        assertEquals(350, (int) bidEvaluator.apply(bid, 4));
        assertEquals(300, (int) bidEvaluator.apply(bid, 5));
        assertEquals(250, (int) bidEvaluator.apply(bid, 6));
        assertEquals(200, (int) bidEvaluator.apply(bid, 7));
        assertEquals(150, (int) bidEvaluator.apply(bid, 8));
        assertEquals(100, (int) bidEvaluator.apply(bid, 9));
        assertEquals(50, (int) bidEvaluator.apply(bid, 10));
        assertEquals(150, (int) bidEvaluator.apply(bid, 11));
        assertEquals(180, (int) bidEvaluator.apply(bid, 12));
        assertEquals(210, (int) bidEvaluator.apply(bid, 13));
    }

    BiFunction<Bid, Integer, Integer> importBidFunction() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("bridgeBid.json")) {
            if (inputStream == null) {
                throw new IOException();
            }
            JSONObject bidObject = new JSONObject(new JSONTokener(inputStream));
            return validBids.evaluateBidContract(bidObject);
        } catch (IOException e) {
            return null;
        }
    }
}
