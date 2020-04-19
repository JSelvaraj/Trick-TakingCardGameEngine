package src.functions;

import org.json.JSONArray;
import org.json.JSONObject;
import src.bid.Bid;
import src.bid.ContractBid;
import src.bid.PotentialBid;
import src.bid.SpecialBid;

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
        return ((potentialBid) -> {
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
                        } else {
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
            } catch (Exception e) {
                //Input is not a 'd' or an integer
                return false;
            }
            int bidValueInt = Integer.parseInt(bidValue);
            //Check for input is pass
            if (bidValueInt == -2) {
                //Check if pass allowed
                if (finalCanPass) {
                    return adjustedHighestBid != null;
                } else {
                    return false;
                }
            } else if (bidValueInt >= minBid) {
                //Check if bids contain suits
                if (finalTrumpSuitBid) {
                    if (finalAscendingBid) {
                        //Case that no bids exist yet
                        if (adjustedHighestBid == null) {
                            return bidValueInt <= maxBid && suitBidRankStr.containsKey(bidSuit);
                        } else {
                            //Check bid value is higher
                            if (bidValueInt > adjustedHighestBid.getBidValue()) {
                                return true;
                            } else
                                return bidValueInt == adjustedHighestBid.getBidValue() && suitBidRankStr.get(adjustedHighestBid.getSuit()) < suitBidRankStr.get(bidSuit);
                        }
                    }
                    //If not ascending bid
                    else {
                        return bidValueInt <= maxBid && suitBidRankStr.containsKey(bidSuit);
                    }
                } else {
                    if (finalAscendingBid) {
                        //Case that no bids exist yet
                        if (adjustedHighestBid == null) {
                            return bidValueInt <= maxBid;
                        }
                        //Check bid value is higher
                        else {
                            return bidValueInt <= maxBid && bidValueInt > adjustedHighestBid.getBidValue();
                        }
                    }
                    //If not ascending bid - only bounds
                    else {
                        return bidValueInt <= maxBid;
                    }
                }
            } else {
                return false;
            }
        });
    }

    public static BiFunction<Bid, Integer, Integer> evaluateBidContract(JSONObject bidObject) {
        //Get the bid specifications.
        int pointsPerBid = bidObject.getInt("pointsPerBid");
        int overTrickPoints = bidObject.getInt("overtrickPoints");
        int penaltyPoints = bidObject.getInt("penaltyPoints");
        int points_for_matching = bidObject.optInt("pointsForMatch", 0); //TODO add to spec
        //Create list for special bids rules
        List<SpecialBid> specialBidList = new LinkedList<>();
        if (bidObject.has("specialBids") && !bidObject.isNull("specialBids")) {
            JSONArray specialBids = bidObject.getJSONArray("specialBids");
            for (int i = 0; i < specialBids.length(); i++) {
                JSONObject specialBid = specialBids.getJSONObject(i);
                int bidValue = specialBid.optInt("bidValue");
                String trumpSuit = specialBid.optString("trumpSuit");
                int overtrickPoints = specialBid.optInt("overtrickPoints");
                int pointsGained = specialBid.optInt("bonusPoints");
                int penalty = specialBid.optInt("penalty");
                int undertrickPoints = specialBid.optInt("undertrickPoints");
                JSONArray undertrickIncrementJSON = specialBid.has("undertrickIncrement") ? specialBid.getJSONArray("undertrickIncrement") : null;
                int[] undertrickIncrement = null;
                if (undertrickIncrementJSON != null) {
                    undertrickIncrement = new int[undertrickIncrementJSON.length()];
                    for (int j = 0; j < undertrickIncrementJSON.length(); j++) {
                        undertrickIncrement[j] = undertrickIncrementJSON.getInt(j);
                    }
                }
                boolean blind = specialBid.optBoolean("blind");
                boolean doubled = specialBid.optBoolean("doubled");
                boolean vulnerable = specialBid.optBoolean("vulnerable");
                int contractPoints = specialBid.optInt("contractPoints");
                specialBidList.add(new SpecialBid(doubled, trumpSuit, bidValue, blind, vulnerable, pointsGained, penalty, trumpSuit, contractPoints, overtrickPoints, undertrickPoints, undertrickIncrement));
            }

        }
        return (((bid, value) -> {
            if (bid instanceof ContractBid) {
                ContractBid contractBid = (ContractBid) bid;
                Optional<SpecialBid> matchingBid = specialBidList.stream()
                        .filter((specialBid -> specialBid.bidMatches(contractBid))).findFirst();
                if (!matchingBid.isPresent()) {
                    throw new IllegalArgumentException();
                }
                SpecialBid specialBid = matchingBid.get();
                int score = 0;
                //If they don't meet their bid.
                if (value >= bid.getBidValue()) {
                    score += bid.getBidValue() * specialBid.getContractPoints();
                    if (value > bid.getBidValue()) {
                        score += (value - bid.getBidValue()) * specialBid.getOvertrickPoints();
                    }
                } else {
                    int tricksUnder = (bid.getBidValue() - value);
                    //The default cause if no increment is provided.
                    if (specialBid.getUndertrickIncrement() == null) {
                        return tricksUnder * specialBid.getUndertrickPoints();
                    } else {
                        //Add the initial undertrick points.
                        score += specialBid.getUndertrickIncrement()[tricksUnder];
                    }
                }
                return score;
            } else { //Evaluate as a regular bid;
                //First finds if the bid matches a special bid that was defined in the game decription
                Optional<SpecialBid> matchingSpecialBid = specialBidList.stream()
                        .filter((specialBid) -> specialBid.isBlind() == bid.isBlind() && specialBid.getBidValue() == bid.getBidValue())
                        .findFirst();
                //If there is a matching special bid
                if (matchingSpecialBid.isPresent()) {
                    //If the
                    if (bid.getBidValue() == value) {
                        return matchingSpecialBid.get().getBonusPoints();
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
            }
        }));
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
        List<SpecialBid> specialBidList = new LinkedList<>();
        if (bidObject.has("specialBids") && !bidObject.isNull("specialBids")) {
            JSONArray specialBids = bidObject.getJSONArray("specialBids");
            for (int i = 0; i < specialBids.length(); i++) {
                JSONObject specialBid = specialBids.getJSONObject(i);
                specialBidList.add(new SpecialBid(specialBid.optInt("bidValue"),
                        specialBid.optInt("bonusPoints"),
                        specialBid.optInt("penalty"),
                        specialBid.optBoolean("blind")));
            }
        }
        return ((bid, value) -> {
            //First finds if the bid matches a special bid that was defined in the game decription
            Optional<SpecialBid> matchingSpecialBid = specialBidList.stream()
                    .filter((specialBid) -> specialBid.isBlind() == bid.isBlind() && specialBid.getBidValue() == bid.getBidValue())
                    .findFirst();
            //If there is a matching special bid
            if (matchingSpecialBid.isPresent()) {
                //If the
                if (bid.getBidValue() == value) {
                    return matchingSpecialBid.get().getBonusPoints();
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
