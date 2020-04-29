package src.functions;


import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import src.bid.Bid;
import src.bid.ContractBid;
import src.player.LocalPlayer;
import src.player.Player;
import src.team.Team;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiFunction;

public class validBidsTestEvaluate {

    static Team team1;

    @BeforeAll
    static void setup() {
        team1 = new Team(new Player[] {new LocalPlayer(), new LocalPlayer(), new LocalPlayer(), new LocalPlayer()}, 0);
    }


    @Test
    void evaluateBidNoSpecialBid() {
        JSONObject bidObject = new JSONObject();
        bidObject.put("pointsPerBid", 10);
        bidObject.put("overtrickPoints", 1);
        bidObject.put("penaltyPoints", 5);
        BiFunction<Bid, Integer, Integer> bidEvaluator = validBids.evaluateBid(bidObject, 0);
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
        BiFunction<Bid, Integer, Integer> bidEvaluator = validBids.evaluateBid(bidObject, 0);
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
        ContractBid bid = new ContractBid(false, "CLUBS", 7, false, false, false,null, team1);
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
        assertEquals(1440, (int) bidEvaluator.apply(bid, 13));
    }

    @Test
    void evaluateBridgeBidDouble() {
        BiFunction<Bid, Integer, Integer> bidEvaluator = importBidFunction();
        ContractBid bid = new ContractBid(true, "CLUBS", 7, false, false, false, null, team1);
        assertEquals(3500, (int) bidEvaluator.apply(bid, 0));
        assertEquals(3200, (int) bidEvaluator.apply(bid, 1));
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
        assertEquals(1630, (int) bidEvaluator.apply(bid, 13));
    }

    @Test
    void evaluateBridgeBidDouble2() {
        BiFunction<Bid, Integer, Integer> bidEvaluator = importBidFunction();
        ContractBid bid = new ContractBid(true, "CLUBS", 3, false, false,false, null, team1);
        assertEquals(2300, (int) bidEvaluator.apply(bid, 0));
        assertEquals(2000, (int) bidEvaluator.apply(bid, 1));
        assertEquals(1700, (int) bidEvaluator.apply(bid, 2));
        assertEquals(1400, (int) bidEvaluator.apply(bid, 3));
        assertEquals(1100, (int) bidEvaluator.apply(bid, 4));
        assertEquals(800, (int) bidEvaluator.apply(bid, 5));
        assertEquals(500, (int) bidEvaluator.apply(bid, 6));
        assertEquals(300, (int) bidEvaluator.apply(bid, 7));
        assertEquals(100, (int) bidEvaluator.apply(bid, 8));
        assertEquals(470, (int) bidEvaluator.apply(bid, 9));
        assertEquals(570, (int) bidEvaluator.apply(bid, 10));
        assertEquals(670, (int) bidEvaluator.apply(bid, 11));
        assertEquals(770, (int) bidEvaluator.apply(bid, 12));
        assertEquals(870, (int) bidEvaluator.apply(bid, 13));
    }

    @Test
    void evaluateBridgeBidNoTrump() {
        BiFunction<Bid, Integer, Integer> bidEvaluator = importBidFunction();
        ContractBid bid = new ContractBid(false, "NO TRUMP", 5, false, false, false, null, team1);
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
        assertEquals(460, (int) bidEvaluator.apply(bid, 11));
        assertEquals(490, (int) bidEvaluator.apply(bid, 12));
        assertEquals(520, (int) bidEvaluator.apply(bid, 13));
    }

    @Test
    void evaluateBridgeBidRedouble() {
        BiFunction<Bid, Integer, Integer> bidEvaluator = importBidFunction();
        ContractBid bid = new ContractBid(true, "DIAMONDS", 6, false, true, false, null, team1);
        assertEquals(6400, (int) bidEvaluator.apply(bid, 0));
        assertEquals(5800, (int) bidEvaluator.apply(bid, 1));
        assertEquals(5200, (int) bidEvaluator.apply(bid, 2));
        assertEquals(4600, (int) bidEvaluator.apply(bid, 3));
        assertEquals(4000, (int) bidEvaluator.apply(bid, 4));
        assertEquals(3400, (int) bidEvaluator.apply(bid, 5));
        assertEquals(2800, (int) bidEvaluator.apply(bid, 6));
        assertEquals(2200, (int) bidEvaluator.apply(bid, 7));
        assertEquals(1600, (int) bidEvaluator.apply(bid, 8));
        assertEquals(1000, (int) bidEvaluator.apply(bid, 9));
        assertEquals(600, (int) bidEvaluator.apply(bid, 10));
        assertEquals(200, (int) bidEvaluator.apply(bid, 11));
        assertEquals(1380, (int) bidEvaluator.apply(bid, 12));
        //assertEquals(1580, (int) bidEvaluator.apply(bid, 13));
    }

    @Test
    void evaluateBridgeBidRedoubleVulnerable() {
        BiFunction<Bid, Integer, Integer> bidEvaluator = importBidFunction();
        ContractBid bid = new ContractBid(true, "DIAMONDS", 4, false, true, true, null, team1);
        assertEquals(5800, (int) bidEvaluator.apply(bid, 0));
        assertEquals(5200, (int) bidEvaluator.apply(bid, 1));
        assertEquals(4600, (int) bidEvaluator.apply(bid, 2));
        assertEquals(4000, (int) bidEvaluator.apply(bid, 3));
        assertEquals(3400, (int) bidEvaluator.apply(bid, 4));
        assertEquals(2800, (int) bidEvaluator.apply(bid, 5));
        assertEquals(2200, (int) bidEvaluator.apply(bid, 6));
        assertEquals(1600, (int) bidEvaluator.apply(bid, 7));
        assertEquals(1000, (int) bidEvaluator.apply(bid, 8));
        assertEquals(400, (int) bidEvaluator.apply(bid, 9));
        assertEquals(920, (int) bidEvaluator.apply(bid, 10));
        assertEquals(1320, (int) bidEvaluator.apply(bid, 11));
        assertEquals(1720, (int) bidEvaluator.apply(bid, 12));
        assertEquals(2120, (int) bidEvaluator.apply(bid, 13));
    }

    @Test
    void evaluateBridgeBidVulnerable() {
        BiFunction<Bid, Integer, Integer> bidEvaluator = importBidFunction();
        ContractBid bid = new ContractBid(false, "SPADES", 4, false, false, true, null, team1);
        assertEquals(1000, (int) bidEvaluator.apply(bid, 0));
        assertEquals(900, (int) bidEvaluator.apply(bid, 1));
        assertEquals(800, (int) bidEvaluator.apply(bid, 2));
        assertEquals(700, (int) bidEvaluator.apply(bid, 3));
        assertEquals(600, (int) bidEvaluator.apply(bid, 4));
        assertEquals(500, (int) bidEvaluator.apply(bid, 5));
        assertEquals(400, (int) bidEvaluator.apply(bid, 6));
        assertEquals(300, (int) bidEvaluator.apply(bid, 7));
        assertEquals(200, (int) bidEvaluator.apply(bid, 8));
        assertEquals(100, (int) bidEvaluator.apply(bid, 9));
        assertEquals(620, (int) bidEvaluator.apply(bid, 10));
        assertEquals(650, (int) bidEvaluator.apply(bid, 11));
        assertEquals(680, (int) bidEvaluator.apply(bid, 12));
        assertEquals(710, (int) bidEvaluator.apply(bid, 13));
    }

    @Test
    void evaluateBridgeBidDoubleVulnerable() {
        BiFunction<Bid, Integer, Integer> bidEvaluator = importBidFunction();
        ContractBid bid = new ContractBid(true, "DIAMONDS", 6, false, false, true, null, team1);
        assertEquals(3500, (int) bidEvaluator.apply(bid, 0));
        assertEquals(3200, (int) bidEvaluator.apply(bid, 1));
        assertEquals(2900, (int) bidEvaluator.apply(bid, 2));
        assertEquals(2600, (int) bidEvaluator.apply(bid, 3));
        assertEquals(2300, (int) bidEvaluator.apply(bid, 4));
        assertEquals(2000, (int) bidEvaluator.apply(bid, 5));
        assertEquals(1700, (int) bidEvaluator.apply(bid, 6));
        assertEquals(1400, (int) bidEvaluator.apply(bid, 7));
        assertEquals(1100, (int) bidEvaluator.apply(bid, 8));
        assertEquals(800, (int) bidEvaluator.apply(bid, 9));
        assertEquals(500, (int) bidEvaluator.apply(bid, 10));
        assertEquals(200, (int) bidEvaluator.apply(bid, 11));
        assertEquals(1540, (int) bidEvaluator.apply(bid, 12));
       // assertEquals(1740, (int) bidEvaluator.apply(bid, 13));
    }

    @Test
    void evaluateBridgeBidDoubleE() {
        BiFunction<Bid, Integer, Integer> bidEvaluator = importBidFunction();
        ContractBid bid = new ContractBid(true, "NO TRUMP", 1, false, false, true, null, team1);
        assertEquals(380, (int) bidEvaluator.apply(bid, 8));
        // assertEquals(1740, (int) bidEvaluator.apply(bid, 13));
    }



    BiFunction<Bid, Integer, Integer> importBidFunction() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("bridgeBid.json")) {
            if (inputStream == null) {
                throw new IOException();
            }
            JSONObject bidObject = new JSONObject(new JSONTokener(inputStream));
            return validBids.evaluateBid(bidObject, 6);
        } catch (IOException e) {
            return null;
        }
    }
}
