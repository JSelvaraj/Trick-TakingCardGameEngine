package src.functions;

import org.json.JSONArray;
import org.json.JSONObject;
import src.gameEngine.Bid;
import src.gameEngine.ContractBid;
import src.gameEngine.PotentialBid;
import src.gameEngine.SpecialBid;
import src.player.Player;
import src.team.Team;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Creates custom functions based on the game description rules that describe how bids work - validity and scoring.
 */
public class validBids {
    /**
     * Creates a Predicate that checks if a given bid is valid.
     *
     *
     * @return Predicate that will return true if the given bid is in [minBid, maxBid]
     */
    public static Predicate<PotentialBid> isValidBidValue(JSONObject bidObject) {
        int minBid = bidObject.getInt("minBid");
        int maxBid = bidObject.getInt("maxBid");
        boolean trumpSuitBid;
        boolean ascendingBid;
        boolean canPass;
        boolean canDouble;
        boolean canRedouble;
        JSONArray suitBidRank;
        HashMap<String, Integer> suitBidRankStr = new HashMap<>();
        trumpSuitBid = bidObject.optBoolean("trumpSuitBid", false);
        ascendingBid = bidObject.optBoolean("ascendingBid", false);
        if (!bidObject.isNull("suitBidRank")) {
            suitBidRank = bidObject.getJSONArray("suitBidRank");
            for (int i = 0; i < suitBidRank.length(); i++) {
                suitBidRankStr.put((String) suitBidRank.get(i), i);
            }
        }
        canPass = bidObject.optBoolean("canPass", false);
        canDouble = bidObject.optBoolean("canDouble", false);
        canRedouble = bidObject.optBoolean("canRedouble", false);
        if (minBid > maxBid) {
            throw new IllegalArgumentException("Minimum bid can't be greater than maximum bid");
        }
        boolean finalTrumpSuitBid = trumpSuitBid;
        boolean finalAscendingBid = ascendingBid;
        boolean finalCanDouble = canDouble;
        boolean finalCanRedouble = canRedouble;
        boolean finalCanPass = canPass;
        return( (potentialBid) -> {
            String bidValue = potentialBid.getBidInput();
            String bidSuit = potentialBid.getBidSuit();
            ContractBid adjustedHighestBid = potentialBid.getAdjustedHighestBid();

            if (bidValue.equals("d")) {
                //Check if doubling is allowed
                if (finalCanDouble) {
                    if (adjustedHighestBid == null || adjustedHighestBid.isRedoubling()) {
                        return false;
                    }
                    //Check for an existing double
                    if (adjustedHighestBid.isDoubling()) {
                        if (finalCanRedouble) {
                            return adjustedHighestBid.getBidValue() * 2 <= maxBid;
                        }
                        else {
                            //Redoubling not allowed - invalid bid
                            return false;
                        }
                    }
                    //Check if there is an existing bid to double
                    //Check if a doubled bid is in bounds
                    return adjustedHighestBid.getBidValue() * 2 <= maxBid;
                }
            }
            try {
                Integer.parseInt(bidValue);
            }
            catch( Exception e ) {
                //Input is not a 'd' or an integer
                return false;
            }
            int bidValueInt = Integer.parseInt(bidValue);
            //Check for input is pass
            if (bidValueInt < 0) {
                //Check if pass allowed
                return finalCanPass;
            }
            //Check if bids contain suits
            if (finalTrumpSuitBid) {
                if (finalAscendingBid) {
                    //Case that no bids exist yet
                    if (adjustedHighestBid == null) {
                        return bidValueInt <= maxBid && bidValueInt >= minBid && suitBidRankStr.containsKey(bidSuit);
                    }
                    else {
                        //Check bid value is higher
                        if (bidValueInt > adjustedHighestBid.getBidValue()) {
                            return true;
                        }
                        else return bidValueInt == adjustedHighestBid.getBidValue() && suitBidRankStr.get(adjustedHighestBid.getSuit()) < suitBidRankStr.get(bidSuit);
                    }
                }
                //If not ascending bid
                else {
                    return bidValueInt <= maxBid && bidValueInt >= minBid && suitBidRankStr.containsKey(bidSuit);
                }
            }
            else {
                if (finalAscendingBid) {
                    //Case that no bids exist yet
                    if (adjustedHighestBid == null) {
                        return bidValueInt <= maxBid && bidValueInt >= minBid;
                    }
                    //Check bid value is higher
                    else {
                        return bidValueInt <= maxBid && bidValueInt > adjustedHighestBid.getBidValue();
                    }
                }
                //If not ascending bid - only bounds
                else {
                    return bidValueInt <= maxBid && bidValueInt >= minBid;
                }
            }
        });
    }

    /**
     * Creates a bifuction that calculates how many points you get for a bid.
     *
     * @param bidObject JSON object from the game description that describes the bid/
     * @return A function taking a Bid object, and the number of tricks won, and returns how many points are gained.
     */
    public static BiFunction<Bid, Integer, Integer> evaluateBid(JSONObject bidObject) {
        //Get the bid specifications.
        int pointsPerBid = bidObject.getInt("pointsPerBid");
        int overTrickPoints = bidObject.getInt("overtrickPoints");
        int penaltyPoints = bidObject.getInt("penaltyPoints");
        int points_for_matching = bidObject.optInt("pointsForMatch", 0); //TODO add to spec
        //Create list for special bids rules
        List<SpecialBid> specialBidList = new LinkedList<>();/*
        if (bidObject.has("specialBids") && !bidObject.isNull("specialBids")) {
            JSONArray specialBids = bidObject.getJSONArray("specialBids");
            for (int i = 0; i < specialBids.length(); i++) {
                JSONObject specialBid = specialBids.getJSONObject(i);
                specialBidList.add(new SpecialBid(specialBid.getInt("bidValue"),
                        specialBid.getInt("pointsGained"),
                        specialBid.getInt("penalty"),
                        specialBid.getBoolean("blind")));
            }
        }*/
        return ((bid, value) -> {
            //First finds if the bid matches a special bid that was defined in the game decription
            Optional<SpecialBid> matchingSpecialBid = specialBidList.stream()
                    .filter((specialBid) -> specialBid.isBlind() == bid.isBlind() && specialBid.getBidValue() == bid.getBidValue())
                    .findFirst();
            //If there is a matching special bid
            if (matchingSpecialBid.isPresent()) {
                //If the
                if (bid.getBidValue() == value) {
                    return matchingSpecialBid.get().getPointsGained();
                } else {
                    return -matchingSpecialBid.get().getPenalty();
                }
            } else { //Otherwise just evaluate the bid normally.
                if (value >= bid.getBidValue()) {
                    return (value == bid.getBidValue() ? points_for_matching : 0) + bid.getBidValue() * pointsPerBid + (value - bid.getBidValue()) * overTrickPoints;
                } else {
                    return value * -penaltyPoints;
                }
            }
        });
    }
}
