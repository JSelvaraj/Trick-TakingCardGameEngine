package src.functions;

import jdk.nashorn.internal.parser.JSONParser;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONTokener;
import org.junit.jupiter.api.Test;
import src.bid.Bid;

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
    void evaluateBridgeBid() {
        BiFunction<Bid, Integer, Integer> bidEvaluator = importBidFunction();
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
