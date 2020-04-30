package src.player;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;
import src.ai.CardPOMDP;
import src.ai.GameObservation;
import src.ai.POMCPTreeNode;
import src.card.Card;
import src.deck.Deck;
import src.bid.Bid;
import src.bid.ContractBid;
import src.exceptions.InvalidBidException;
import src.gameEngine.Hand;
import src.bid.PotentialBid;
import src.parser.GameDesc;
import src.rdmEvents.Swap;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class POMDPPlayer extends Player {
    private GameObservation observation;
    private CardPOMDP cardPOMDP;
    private static final long timeout = 5000;
    private static final long shortTimout = 1000;
    private static final int openBidThresh = 12;
    private StringBuilder trumpSuit;
    private GameDesc desc;
    private boolean addedDummyHand;

    public POMDPPlayer() {
//        super(playerNumber);
    }

    @Override
    public void initPlayer(Predicate<Card> validCard, GameDesc desc, StringBuilder trumpSuit) {
        super.initPlayer(validCard, desc, trumpSuit);
        this.desc = desc;
        this.trumpSuit = trumpSuit;
        addedDummyHand = false;
    }

    @Override
    public void startHand(StringBuilder trumpSuit, int handSize) {
        this.trumpSuit = trumpSuit;
        Deck deck = new Deck((LinkedList<Card>) desc.getDECK());
        observation = new GameObservation(deck.cards, desc.getNUMBEROFPLAYERS(), handSize);
        //Add the cards that this player has.
        observation.addKnownCards(getPlayerNumber(), getHand().getHand());
        cardPOMDP = new CardPOMDP(desc, timeout, getPlayerNumber(), trumpSuit);
    }

    @Override
    public Card playCard(String trumpSuit, Hand currentTrick) {
        //If the trick is empty
        if (currentTrick.getHandSize() == 0) {
            //Signify that this player starts the trick
            observation.setTrickStartedBy(getPlayerNumber());
        }
        Card card = cardPOMDP.searchCard(observation);
        //Set the trump to broken if it has been broken.
        if (card.getSUIT().equals(trumpSuit)) {
            observation.setBreakFlag();
        }
        assert super.getCanBePlayed().test(card);
        return super.getHand().giveCard(card);
    }

    @Override
    public void broadcastPlay(Card card, int playerNumber) {
        observation.updateGameState(playerNumber, card);
        //Set the trump to be broken if it has been broken.
        if (card.getSUIT().equals(trumpSuit.toString())) {
            observation.setBreakFlag();
        }
    }

    @Override
    public Bid makeBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, boolean firstRound, boolean canBidBlind) {
        //Need to initialise for if we started the trick.
        if (observation.getCurrentTrick().size() == 0) {
            observation.setTrickStartedBy(getPlayerNumber());
        }
        //If it isn't contract bidding
        if (!desc.isAscendingBid()) {
            assert trumpSuit != null;
            int bidValue = cardPOMDP.searchBid(observation);
            PotentialBid bid = new PotentialBid(null, Integer.toString(bidValue), adjustedHighestBid, this, firstRound);
            if (!validBid.test(bid)) {
                //See if the calculated bid falls outside of what is allowed.
                if (bidValue < desc.getMinBid()) {
                    bidValue = desc.getMinBid();
                } else if (bidValue > desc.getMaxBid()) {
                    bidValue = desc.getMaxBid();
                } else {
                    throw new InvalidBidException();
                }
            }
            return new Bid(false, null, bidValue, false, false);
        } else {
            StringBuilder tempTrumpSuit;
            if (trumpSuitBid) {
                tempTrumpSuit = new StringBuilder("");
            } else {
                tempTrumpSuit = new StringBuilder(trumpSuit.toString());
            }
            CardPOMDP tempPOMDP = new CardPOMDP(desc, shortTimout, getPlayerNumber(), tempTrumpSuit);
            //See is passing is a valid bid here.
            if (validBid.test(new PotentialBid(null, "-2", adjustedHighestBid, this, firstRound))) {
                return canPassBid(validBid, trumpSuitBid, adjustedHighestBid, firstRound, tempTrumpSuit, tempPOMDP);
            } else {
                return nonPassingBid(validBid, trumpSuitBid, adjustedHighestBid, tempTrumpSuit, tempPOMDP, firstRound);
            }
        }
    }

    private Bid canPassBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, boolean firstRound, StringBuilder tempTrumpSuit, CardPOMDP tempPOMDP) {
        if (adjustedHighestBid == null) {
            int hcp = highCardPoints();
            //If you don't meet the threshold and can pass, then pass.
            if (hcp < (getHand().getHandSize() - 1) && validBid.test(new PotentialBid(null, "-2", adjustedHighestBid, this, firstRound))) {
                return passingBid();
            } else {
                return openingBid(validBid, trumpSuitBid, tempTrumpSuit, tempPOMDP);
            }
        } else if (adjustedHighestBid.getDeclarer().getTeam().findPlayer(this) && adjustedHighestBid.getDeclarer() != this) { //Our teammate has the highest contract so far.
            if (adjustedHighestBid.isDoubling()) {
                tempTrumpSuit.setLength(0);
                tempTrumpSuit.append(adjustedHighestBid.getSuit());
                //Get an estimate of how many tricks will be won by this partnership.
                int estimatedTricksWon = (int) tempPOMDP.search(this.observation).getValue();
                //If it thinks that we can win this many tricks, then reodouble the bid. Otherwise pass it.
                if (estimatedTricksWon - desc.getTrickThreshold() >= adjustedHighestBid.getBidValue()) {
                    return new Bid(true, null, 0, false, false);
                }
                //Else will fall through to raise or pass.
            }
            return raiseOrPass(validBid, trumpSuitBid, adjustedHighestBid, tempTrumpSuit, tempPOMDP, firstRound);
        } else if (adjustedHighestBid.getDeclarer() != this) {
            //If you think you should double the opponent.
            if (checkDouble(validBid, trumpSuitBid, adjustedHighestBid, tempTrumpSuit, tempPOMDP, firstRound)) {
                return new Bid(true, null, 0, false, false);
            }
            return raiseOrPass(validBid, trumpSuitBid, adjustedHighestBid, tempTrumpSuit, tempPOMDP, firstRound);
        } else {
            return passingBid();
        }
    }

    /**
     * Perform a check to see if you think they won't meet their contract, and so double it.
     *
     * @param validBid
     * @param trumpSuitBid
     * @param adjustedHighestBid
     * @param tempTrumpSuit
     * @param tempCardPOMDP
     * @param firstRound
     * @return True if the agent should double the contract, false otherwise.
     */
    private boolean checkDouble(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, StringBuilder tempTrumpSuit, CardPOMDP tempCardPOMDP, boolean firstRound) {
        tempTrumpSuit.setLength(0);
        tempTrumpSuit.append(adjustedHighestBid.getSuit());
        POMCPTreeNode bestNode = tempCardPOMDP.search(this.observation);
        int ourTrickswon = (int) Math.floor(bestNode.getValue());
        int theirTricksWon = desc.getInitialHandSize() - ourTrickswon;
        //Return a double if you don't think they'll meet their tricks won and you can bid a double.
        if (adjustedHighestBid.getBidValue() > theirTricksWon - desc.getTrickThreshold() && validBid.test(new PotentialBid(null, "d", adjustedHighestBid, this, firstRound))) {
            return true;
        }
        return false;
    }

    private Bid raiseOrPass(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, StringBuilder tempTrumpSuit, CardPOMDP tempCardPOMDP, boolean firstRound) {
        if (!trumpSuitBid) {
            int bidValue = tempCardPOMDP.searchBid(observation);
            //Check for out of bounds.
            if (bidValue < desc.getMinBid()) {
                bidValue = desc.getMinBid();
            } else if (bidValue > desc.getMaxBid()) {
                bidValue = desc.getMaxBid();
            }
            if (bidValue <= adjustedHighestBid.getBidValue()) {
                return passingBid();
            } else {
                return new Bid(false, null, bidValue, false, false);
            }
        }
        //If it is your teammate
        if (adjustedHighestBid.getDeclarer().getTeam().findPlayer(this)) {
            tempTrumpSuit.setLength(0);
            tempTrumpSuit.append(tempCardPOMDP);
            int bidValue = tempCardPOMDP.searchBid(this.observation);
            //If you think you can win more than this bid with this suit.
            if (bidValue > adjustedHighestBid.getBidValue()) {
                assert validBid.test(new PotentialBid(adjustedHighestBid.getSuit(), "" + bidValue, adjustedHighestBid, this, firstRound));
                return new Bid(false, adjustedHighestBid.getSuit(), bidValue, false, false);
            } else { //Otherwise look for a suit that you can do better with.
                return raiseWithPass(validBid, trumpSuitBid, adjustedHighestBid, tempTrumpSuit, tempCardPOMDP, firstRound);
            }
        }
        return raiseWithPass(validBid, trumpSuitBid, adjustedHighestBid, tempTrumpSuit, tempCardPOMDP, firstRound);
    }

    private Bid raiseWithPass(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, StringBuilder tempTrumpSuit, CardPOMDP tempCardPOMDP, boolean firstRound) {
        List<String> potentialSuits = desc.getBidSuits().subList(desc.getBidSuits().indexOf(adjustedHighestBid.getSuit()), desc.getBidSuits().size());
        Pair<String, Integer> bestResult = getBestSuitBid(potentialSuits, tempTrumpSuit, tempCardPOMDP);
        String suit = bestResult.getKey();
        int bidValue = bestResult.getValue();
        //If you don't have something better, then pass.
        if (!validBid.test(new PotentialBid(suit, "" + bidValue, adjustedHighestBid, this, firstRound))) {
            return passingBid();
        }
        return new Bid(false, suit, bidValue, false, false);
    }

    private Bid raise(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, StringBuilder tempTrumpSuit, CardPOMDP tempCardPOMDP, boolean firstRound) {
        if (!trumpSuitBid) {
            throw new UnsupportedOperationException();
        }
        List<String> potentialSuits = desc.getBidSuits().subList(desc.getBidSuits().indexOf(adjustedHighestBid.getSuit()), desc.getBidSuits().size());
        Pair<String, Integer> bestResult = getBestSuitBid(potentialSuits, tempTrumpSuit, tempCardPOMDP);
        String suit = bestResult.getKey();
        int bidValue = bestResult.getValue();
        //If you don't have something better, then pass.
        if (!validBid.test(new PotentialBid(suit, "" + bidValue, adjustedHighestBid, this, firstRound))) {
            throw new UnsupportedOperationException();
        }
        return new Bid(false, suit, bidValue, false, false);
    }

    private Bid passingBid() {
        return new Bid(false, null, -2, false, false);
    }

    /**
     * Make a bid where you aren't allowed to pass.
     *
     * @param validBid
     * @param trumpSuitBid
     * @param adjustedHighestBid
     * @param tempTrumpSuit
     * @param tempPOMDP
     * @param firstRound
     * @return
     */
    private Bid nonPassingBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, StringBuilder tempTrumpSuit, CardPOMDP tempPOMDP, boolean firstRound) {
        //If this is the opening bid.
        if (adjustedHighestBid == null) {
            return openingBid(validBid, trumpSuitBid, tempTrumpSuit, tempPOMDP);
        } else {
            return raise(validBid, trumpSuitBid, adjustedHighestBid, tempTrumpSuit, tempPOMDP, firstRound);
        }
    }

    /**
     * Find the suit from the list of potential suits that would give you the best result.
     *
     * @param potentialSuits The suits that can be bid.
     * @param tempTrumpSuit  The trump suit of the POMDP
     * @param tempCardPOMDP  The card POMDP to use.
     * @return A pair of the suit and the value it should bid.
     */
    private Pair<String, Integer> getBestSuitBid(List<String> potentialSuits, StringBuilder tempTrumpSuit, CardPOMDP tempCardPOMDP) {
        String bestSuit = null;
        int bestResult = -1;
        //See which suit gives the best result.
        for (String bidSuit : potentialSuits) {
            tempTrumpSuit.setLength(0);
            tempTrumpSuit.append(bidSuit);
            int bidValue = tempCardPOMDP.searchBid(this.observation);
            if (bidValue > bestResult) {
                bestResult = bidValue;
                bestSuit = bidSuit;
            }
        }
        return new ImmutablePair<>(bestSuit, bestResult);
    }


    /**
     * Make an opening bid.
     *
     * @param validbid
     * @param trumpSuitBid
     * @param tempTrumpSuit
     * @param tempCardPOMDP
     * @return
     */
    private Bid openingBid(Predicate<PotentialBid> validbid, boolean trumpSuitBid, StringBuilder tempTrumpSuit, CardPOMDP tempCardPOMDP) {
        if (!trumpSuitBid) {
            int bidValue = tempCardPOMDP.searchBid(observation);
            //Check for out of bounds.
            if (bidValue < desc.getMinBid()) {
                bidValue = desc.getMinBid();
            } else if (bidValue > desc.getMaxBid()) {
                bidValue = desc.getMaxBid();
            }
            return new Bid(false, null, bidValue, false, false);
        }
        List<String> potentialSuits = desc.getBidSuits();
        Pair<String, Integer> bestSuitResult = getBestSuitBid(potentialSuits, tempTrumpSuit, tempCardPOMDP);
        String bestSuit = bestSuitResult.getKey();
        int bestResult = bestSuitResult.getValue();
        if (bestResult < desc.getMinBid()) {
            bestResult = desc.getMinBid();
        } else if (bestResult > desc.getMaxBid()) {
            bestResult = desc.getMaxBid();
        }
        if (!validbid.test(new PotentialBid(bestSuit, "" + bestResult, null, this, true))) {
            throw new UnsupportedOperationException();
        }
        return new Bid(false, bestSuit, bestResult, false, false);
    }

    private int highCardPoints() {
        String[] highCards = Arrays.copyOfRange(desc.getRANKS(), 0, 4);
        int hcp = 0;
        for (int i = 0; i < highCards.length; i++) {
            int finalI = i;
            int matching = (int) super.getHand().getHand().stream().filter((c) -> c.getRANK().equals(highCards[finalI])).count();
            hcp += matching * (i + 1);
        }
        return hcp;
    }

    public Swap getSwap(Player strongPlayer) {
        throw new UnsupportedOperationException();
    }

    public void broadcastSwap(Swap swap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void broadcastBid(Bid bid, int playerNumber, ContractBid adjustedHighestBid) {
        System.out.println(bid);
    }

    @Override
    public void broadcastDummyHand(int playerNumber, List<Card> dummyHand) {
        if (playerNumber != getPlayerNumber() && !addedDummyHand) {
            this.observation.addKnownCards(playerNumber, dummyHand);
        }
        addedDummyHand = true;
    }
}
