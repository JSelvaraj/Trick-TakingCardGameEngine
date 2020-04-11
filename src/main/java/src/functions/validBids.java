package src.functions;

import org.json.JSONArray;
import org.json.JSONObject;
import src.gameEngine.Bid;
import src.gameEngine.PotentialBid;
import src.gameEngine.SpecialBid;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.IntPredicate;
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
        boolean trumpSuitBid = false;
        boolean ascendingBid = false;
        boolean canPass = false;
        boolean canDouble = false;
        JSONArray suitBidRank;
        String[] suitBidRankStr;
        if (!bidObject.isNull("trumpSuitBid")) {
            trumpSuitBid = bidObject.getBoolean("trumpSuitBid");
        }
        if (!bidObject.isNull("ascendingBid")) {
            ascendingBid = bidObject.getBoolean("ascendingBid");
        }
        if (!bidObject.isNull("suitBidRank")) {
            suitBidRank = bidObject.getJSONArray("suitBidRank");
            suitBidRankStr = new String[suitBidRank.length()];
            Iterator<Object> iterator = suitBidRank.iterator();
            int counter = 0;
            while(iterator.hasNext()) {
                suitBidRankStr[counter] = (String) iterator.next();
                counter++;
            }
        }
        if (!bidObject.isNull("canPass")) {
            trumpSuitBid = bidObject.getBoolean("canPass");
        }
        if (!bidObject.isNull("canDouble")) {
            ascendingBid = bidObject.getBoolean("canDouble");
        }
        if (minBid > maxBid) {
            throw new IllegalArgumentException("Minimum bid can't be greater than maximum bid");
        }

        boolean finalTrumpSuitBid = trumpSuitBid;
        return( (potentialBid) -> {
            String bidValue = potentialBid.getBidInput();
            String bidSuit = potentialBid.getBidSuit();
            if (bidValue.equals("d")) {
                if (canDouble) {
                    
                }
                else {
                    return false;
                }
            }
            if (finalTrumpSuitBid) {

                return true;
            }
            else {
                int bidValueInt = Integer.parseInt(bidValue);
                return minBid <= bidValueInt && bidValueInt <= maxBid;
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
        List<SpecialBid> specialBidList = new LinkedList<>();
        if (bidObject.has("specialBids") && !bidObject.isNull("specialBids")) {
            JSONArray specialBids = bidObject.getJSONArray("specialBids");
            for (int i = 0; i < specialBids.length(); i++) {
                JSONObject specialBid = specialBids.getJSONObject(i);
                specialBidList.add(new SpecialBid(specialBid.getInt("bidValue"),
                        specialBid.getInt("pointsGained"),
                        specialBid.getInt("penalty"),
                        specialBid.getBoolean("blind")));
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
