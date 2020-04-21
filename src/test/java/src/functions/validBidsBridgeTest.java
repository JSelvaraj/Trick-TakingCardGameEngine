package src.functions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import src.bid.ContractBid;
import src.bid.PotentialBid;
import src.player.LocalPlayer;
import src.player.Player;
import src.team.Team;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class validBidsBridgeTest {

    static Predicate<PotentialBid> bidValidator;
    static Team team1;
    static Player player1;

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
        bidValidator = validBids.isValidBidValue(bidObject, 4);
        player1 = new LocalPlayer(1);
        team1 = new Team(new Player[] {player1, new LocalPlayer()}, 1);
    }

    @Test
    void validateBidFirstBidsPass() {
        assertTrue(bidValidator.test(new PotentialBid(null, Integer.toString(-2), null, new LocalPlayer(), true)));
        assertTrue(bidValidator.test(new PotentialBid(null, Integer.toString(-2), null, new LocalPlayer(), true)));
        assertTrue(bidValidator.test(new PotentialBid(null, Integer.toString(-2), null, new LocalPlayer(), true)));
        assertFalse(bidValidator.test(new PotentialBid(null, Integer.toString(-2), null, new LocalPlayer(), false)));
    }

    @Test
    void doubleFirstBid() {
        assertFalse(bidValidator.test(new PotentialBid(null, "d", null, new LocalPlayer(), false)));
    }

    @Test
    void doubleABidInBounds() {
        ContractBid adjustedHighestBid = new ContractBid(false, "CLUBS", 1, false,
                false,false, null, team1);
        assertTrue(bidValidator.test(new PotentialBid(null, "d", adjustedHighestBid, new LocalPlayer(3), false)));
    }

    @Test
    void doubleABidOutBounds() {
        ContractBid adjustedHighestBid = new ContractBid(false, null, 4, false, false,false, null, team1);
        assertFalse(bidValidator.test(new PotentialBid(null, "d", adjustedHighestBid, new LocalPlayer(), false)));
    }

    @Test
    void reDoubleADoubleInBounds() {
        ContractBid adjustedHighestBid = new ContractBid(true, null, 3, false, false,false, null, team1);
        assertTrue(bidValidator.test(new PotentialBid(null, "d", adjustedHighestBid, new LocalPlayer(3), false)));
    }

    @Test
    void reDoubleADoubleOutBounds() {
        ContractBid adjustedHighestBid = new ContractBid(true, null, 4, false, false,false, null, team1);
        assertFalse(bidValidator.test(new PotentialBid(null, "d", adjustedHighestBid, new LocalPlayer(), false)));
    }

    @Test
    void reDoubleAReDouble() {
        ContractBid adjustedHighestBid = new ContractBid(false, null, 4, false, true, false,null, team1);
        assertFalse(bidValidator.test(new PotentialBid(null, "d", adjustedHighestBid, new LocalPlayer(), false)));
    }

    @Test
    void passAfterDouble() {
        ContractBid adjustedHighestBid = new ContractBid(true, null, 4, false, false, false,null, team1);
        assertFalse(bidValidator.test(new PotentialBid(null, "d", adjustedHighestBid, new LocalPlayer(), false)));
    }

    @Test
    void raiseBidIncreaseValue() {
        ContractBid adjustedHighestBid = new ContractBid(false, "NO TRUMP", 4, false, false, false,null, team1);
        assertTrue(bidValidator.test(new PotentialBid("NO TRUMP", Integer.toString(5), adjustedHighestBid, new LocalPlayer(), false)));
    }

    @Test
    void raiseBidIncreaseSuit() {
        ContractBid adjustedHighestBid = new ContractBid(false, "CLUBS", 4, false, false, false,null, team1);
        assertTrue(bidValidator.test(new PotentialBid("SPADES", Integer.toString(4), adjustedHighestBid, new LocalPlayer(), false)));
    }

    @Test
    void badRaiseBidIncreaseSuit1() {
        ContractBid adjustedHighestBid = new ContractBid(false, "SPADES", 4, false, false, false,null, team1);
        assertFalse(bidValidator.test(new PotentialBid("SPADES", Integer.toString(4), adjustedHighestBid, new LocalPlayer(), false)));
    }

    @Test
    void badRaiseBidIncreaseValue() {
        ContractBid adjustedHighestBid = new ContractBid(false, "SPADES", 4, false, false, false,null, team1);
        assertFalse(bidValidator.test(new PotentialBid("SPADES", Integer.toString(3), adjustedHighestBid, new LocalPlayer(), false)));
    }



}
