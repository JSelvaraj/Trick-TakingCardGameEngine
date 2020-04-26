package src.player;

import javafx.util.Pair;
import src.ai.CardPOMDP;
import src.ai.GameObservation;
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

    public POMDPPlayer() {
//        super(playerNumber);
    }

    @Override
    public void initPlayer(Predicate<Card> validCard, GameDesc desc, StringBuilder trumpSuit) {
        super.initPlayer(validCard, desc, trumpSuit);
        this.desc = desc;
        this.trumpSuit = trumpSuit;
    }

    @Override
    public void startHand(StringBuilder trumpSuit) {
        this.trumpSuit = trumpSuit;
        Deck deck = new Deck(); //TODO work for more than standard deck.
        observation = new GameObservation(deck.cards, desc.getNUMBEROFPLAYERS(), desc.getInitialHandSize());
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
            //See is passing is a valid bid here.
            if (validBid.test(new PotentialBid(null, "-2", adjustedHighestBid, this, firstRound))) {
                return canPassBid(validBid, trumpSuitBid, adjustedHighestBid, firstRound);
            } else {
                return nonPassingBid(validBid, trumpSuitBid, adjustedHighestBid);
            }
        }
    }

    private Bid canPassBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, boolean firstRound) {
        int hcp = highCardPoints();
        StringBuilder tempTrumpSuit = new StringBuilder();
        CardPOMDP tempPOMDP = new CardPOMDP(desc, shortTimout, getPlayerNumber(), tempTrumpSuit);
        if (adjustedHighestBid == null) {
            //If you don't meet the threshold and can pass, then pass.
            if (hcp < openBidThresh && validBid.test(new PotentialBid(null, "-2", adjustedHighestBid, this, firstRound))) {
                return passingBid();
            } else {
                return openingBid(validBid, trumpSuitBid, tempTrumpSuit, tempPOMDP);
            }
        } else if (adjustedHighestBid.getDeclarer().getTeam().containsPlayer(this)) { //Our teammate has the highest contract so far.
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
        } else {//TODO check double opponent.
            return raiseOrPass(validBid, trumpSuitBid, adjustedHighestBid, tempTrumpSuit, tempPOMDP, firstRound);
        }
    }

    private Bid raiseOrPass(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, StringBuilder tempTrumpSuit, CardPOMDP tempCardPOMDP, boolean firstRound) {
        if (!trumpSuitBid) {
            throw new UnsupportedOperationException();
        }
        //If it is your teammate
        if (adjustedHighestBid.getDeclarer().getTeam().containsPlayer(this)) {
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

    private Bid nonPassingBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, StringBuilder tempTrumpSuit, CardPOMDP tempPOMDP, boolean firstRound) {
        //If this is the opening bid.
        if (adjustedHighestBid == null) {
            return openingBid(validBid, trumpSuitBid, null, null);
        } else {
            return raise(validBid, trumpSuitBid, adjustedHighestBid, tempTrumpSuit, tempPOMDP, firstRound);
        }
    }

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
        return new Pair<>(bestSuit, bestResult);
    }


    private Bid openingBid(Predicate<PotentialBid> validbid, boolean trumpSuitBid, StringBuilder tempTrumpSuit, CardPOMDP tempCardPOMDP) {
        assert trumpSuitBid;
        List<String> potentialSuits = desc.getBidSuits();
        Pair<String, Integer> bestSuitResult = getBestSuitBid(potentialSuits, tempTrumpSuit, tempCardPOMDP);
        String bestSuit = bestSuitResult.getKey();
        int bestResult = bestSuitResult.getValue();
        if (!validbid.test(new PotentialBid(bestSuit, "" + bestResult, null, this, true))) {
            throw new UnsupportedOperationException();
        }
        return new Bid(false, bestSuit, bestResult, false, false);
    }

    private int highCardPoints() {
        String[] highCards = Arrays.copyOfRange(desc.getRANKS(), desc.getRANKS().length - 5, desc.getRANKS().length - 1);
        int hcp = 0;
        for (int i = 0; i < highCards.length; i++) {
            int finalI = i;
            int matching = (int) super.getHand().getHand().stream().filter((c) -> c.getSUIT().equals(highCards[finalI])).count();
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
        //throw new UnsupportedOperationException();
    }
}
