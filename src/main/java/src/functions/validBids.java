package src.functions;

import org.json.JSONArray;
import org.json.JSONObject;
import src.gameEngine.Bid;
import src.gameEngine.SpecialBid;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.IntPredicate;

public class validBids {
    public IntPredicate isValidBidValue(int minBid, int maxBid) {
        return (bid) -> minBid <= bid && bid <= maxBid;
    }

    public BiFunction<Bid, Integer, Integer> evaluateBid(JSONObject bidObject) {
        //Get the bid specifications.
        int pointsPerBid = bidObject.getInt("pointsPerBid");
        int overTrickPoints = bidObject.getInt("overtrickPoints");
        int penaltyPoints = bidObject.getInt("penaltyPoints");
        List<SpecialBid> specialBidList = new LinkedList<>();
        if (bidObject.has("specialBids")) {
            JSONArray specialBids = bidObject.getJSONArray("specialBids");
            for (int i = 0; i < specialBids.length(); i++) {
                JSONObject specialBid = specialBids.getJSONObject(i);
                specialBidList.add(new SpecialBid(specialBid.getInt("bidValue"),
                        specialBid.getInt("pointsGained"),
                        specialBid.getInt("penalty"),
                        specialBid.getBoolean("blindBid")));
            }
        }
        return ((bid, value) -> {
            //First finds if the bid matches a special bid
            Optional<SpecialBid> matchingSpecialBid = specialBidList.stream()
                    .filter((specialBid) -> specialBid.isBlind() && bid.isBlind() && specialBid.getBidValue() == bid.getBidValue())
                    .findFirst();
            //if there is a matching special bid
            if (matchingSpecialBid.isPresent()) {
                if (bid.getBidValue() == value) {
                    return matchingSpecialBid.get().getPointsGained();
                } else {
                    return matchingSpecialBid.get().getPenalty();
                }
            } else { //Otherwise just evaluate the bid normally.
                if (value >= bid.getBidValue()) {
                    return bid.getBidValue() * pointsPerBid + (bid.getBidValue() - value) * overTrickPoints;
                } else {
                    return value * penaltyPoints;
                }
            }
        });
    }
}
