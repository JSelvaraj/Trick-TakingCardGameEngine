package src.functions;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import src.bid.*;
import src.player.Player;
import src.team.Team;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Creates custom functions based on the game description rules that describe how bids work - validity and scoring.
 */
public class validBids {
    /**
     * Creates a Predicate that checks if a given bid is valid.
     *
     * @return Predicate that will return true if the given bid is in [minBid, maxBid]
     */
    public static Predicate<PotentialBid> isValidBidValue(JSONObject bidObject, int initialHandSize) {
        int minBid = bidObject.optInt("minBid", 0);
        int maxBid = bidObject.optInt("maxBid", initialHandSize);
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
                if (suitBidRank.get(i) == JSONObject.NULL) {
                    suitBidRankStr.put("NO TRUMP", i);
                }
                else {
                    suitBidRankStr.put((String) suitBidRank.get(i), i);
                }
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
            boolean firstRound = potentialBid.isFirstRound();
            ContractBid adjustedHighestBid = potentialBid.getAdjustedHighestBid();
            if (bidValue.equals("d")) {
                //Check if doubling is allowed
                if (finalCanDouble) {
                    if (adjustedHighestBid == null || adjustedHighestBid.isRedoubling()) {
                        return false;
                    }
                    Team prevBidTeam = adjustedHighestBid.getTeam();
                    Player playerWhoBid = potentialBid.getPlayer();
                    //Check for an existing double
                    if (adjustedHighestBid.isDoubling()) {
                        //Redoubling not allowed - invalid bid
                        return finalCanRedouble && prevBidTeam.findPlayer(playerWhoBid);
                    }
                    //Check if there is an existing bid to double
                    //Check if a doubled bid is in bounds
                    return !prevBidTeam.findPlayer(playerWhoBid);
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
                    if (firstRound) {
                        return true;
                    } else {
                        return adjustedHighestBid != null;
                    }
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

    public static BiFunction<Bid, Integer, Pair<Integer, Integer>> evaluateBid(JSONObject bidObject, int trickThreshold) {
        //Get the bid specifications.
        int pointsPerBid = bidObject.getInt("pointsPerBid");
        int overTrickPoints = bidObject.getInt("overtrickPoints");
        int penaltyPoints = bidObject.getInt("penaltyPoints");
        int points_for_matching = bidObject.optInt("pointsForMatch", 0);
        //Create list for special bids rules
        List<SpecialBid> specialBidList = new LinkedList<>();
        if (bidObject.has("specialBids") && !bidObject.isNull("specialBids")) {
            JSONArray specialBids = bidObject.getJSONArray("specialBids");
            for (int i = 0; i < specialBids.length(); i++) {
                JSONObject specialBid = specialBids.getJSONObject(i);
                int bidValue = specialBid.optInt("bidValue");
                String trumpSuit = null;
                if(specialBid.has("trumpSuit")){
                    if(specialBid.isNull("trumpSuit")){
                        trumpSuit = "NO TRUMP";
                    } else {
                        trumpSuit = specialBid.optString("trumpSuit");
                    }
                }
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
                boolean undertrickAwardedToOpponent = false;
                if(specialBid.has("undertrickAwardedTo") && specialBid.getString("undertrickAwardedTo").equals("opponent")){
                    undertrickAwardedToOpponent = true;
                }
                specialBidList.add(new SpecialBid(doubled, trumpSuit, bidValue, blind, vulnerable, pointsGained, penalty, trumpSuit, contractPoints, overtrickPoints, undertrickPoints, undertrickIncrement, undertrickAwardedToOpponent));
            }

        }
        List<BonusScore> bonusScoresList = new LinkedList<>();
        if (bidObject.has("bonusScores") && !bidObject.isNull("bonusScores")) {
            JSONArray bonusScores = bidObject.getJSONArray("bonusScores");
            for (int i = 0; i < bonusScores.length(); i++) {
                JSONObject bonusScore = bonusScores.getJSONObject(i);
                BonusScore toAdd = null;
                int bonus = bonusScore.getInt("bonusPoints");
                boolean vulnerable = bonusScore.optBoolean("vulnerable");
//                if (bonusScore.has("handScoreMin") && bonusScore.has("trickTotal")) {
//                    throw new IllegalArgumentException("Can't have both keys.");
//                }
                if (!bonusScore.has("trickTotal")) {
                    toAdd = new handScore(bonus, vulnerable, bonusScore.getInt("handScoreMin"), bonusScore.getInt("handScoreMax"));
                } else {
                    toAdd = new SlamBonus(bonus, vulnerable, bonusScore.getInt("trickTotal"));
                }
                bonusScoresList.add(toAdd);
            }
        }
        return (bid, value) -> {
            int score = 0;
            int opponentScore = 0;
            if (bid instanceof ContractBid) {
                int adjustedValue = value - trickThreshold;
                ContractBid contractBid = (ContractBid) bid;
                Optional<SpecialBid> matchingBid = specialBidList.stream()
                        .filter((specialBid -> specialBid.bidMatches(contractBid))).findFirst();
                if (!matchingBid.isPresent()) {
                    throw new IllegalArgumentException();
                }
                SpecialBid specialBid = matchingBid.get();
                //If they don't meet their bid.
                if (adjustedValue >= bid.getBidValue()) {
                    final int handScore = bid.getBidValue() * specialBid.getContractPoints();
                    score += handScore;
                    score += specialBid.getBonusPoints();
                    if (adjustedValue > bid.getBidValue()) {
                        score += (adjustedValue - bid.getBidValue()) * specialBid.getOvertrickPoints();
                    }
                    //Double if it is redoubling
                    if (contractBid.isRedoubling()) {
                        if (!contractBid.isDoubling()) {
                            throw new IllegalArgumentException("Can't redouble without doubling");
                        }
                        score *= 2;
                    }
                    //Find any score bonuses to apply. Need to half to account for doubling
                    score += bonusScoresList.stream().filter(b -> b.matches(bid, handScore, bid.getBidValue())).mapToInt(BonusScore::getBonusScore).sum();
                } else {
                    int tricksUnder = bid.getBidValue() + trickThreshold - value;
                    //The default cause if no increment is provided.
                    int undertrick;
                    if (specialBid.getUndertrickIncrement() == null) {
                        undertrick = tricksUnder * specialBid.getUndertrickPoints();
                    } else {
                        //Add the initial undertrick points.
                        undertrick = specialBid.getUndertrickIncrement()[tricksUnder - 1];
                    }
                    score -= specialBid.getPenalty();
                    //Double if it is redoubling
                    if (contractBid.isRedoubling()) {
                        if (!contractBid.isDoubling()) {
                            throw new IllegalArgumentException("Can't redouble without doubling");
                        }
                        undertrick *= 2;
                    }
                    if(specialBid.isUndertrickAwardedToOpponent()){
                        opponentScore += undertrick;
                    } else {
                        score += undertrick;
                    }
                }
            } else { //Evaluate as a regular bid;
                //First finds if the bid matches a special bid that was defined in the game decription
                Optional<SpecialBid> matchingSpecialBid = specialBidList.stream()
                        .filter((specialBid) -> specialBid.isBlind() == bid.isBlind() && specialBid.getBidValue() == bid.getBidValue())
                        .findFirst();
                //If there is a matching special bid
                if (matchingSpecialBid.isPresent()) {
                    //If the
                    if (bid.getBidValue() == value) {
                        score = matchingSpecialBid.get().getBonusPoints();
                    } else {
                        score = -matchingSpecialBid.get().getPenalty();
                    }
                } else { //Otherwise just evaluate the bid normally.
                    if (value >= bid.getBidValue()) {
                        score =  (value == bid.getBidValue() ? points_for_matching : 0) + bid.getBidValue() * pointsPerBid + (value - bid.getBidValue()) * overTrickPoints;
                    } else {
                        score = value * -penaltyPoints;
                    }
                }
            }
            return new ImmutablePair<>(score, opponentScore);
        };
    }

}
