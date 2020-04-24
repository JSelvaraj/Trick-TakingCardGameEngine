package src.player;

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
    public Bid makeBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid) {
        //Need to initialise for if we started the trick.
        if (observation.getCurrentTrick().size() == 0) {
            observation.setTrickStartedBy(getPlayerNumber());
        }
        //If it isn't contract bidding
        if (!desc.isAscendingBidding()) {
            assert trumpSuit != null;
            int bidValue = cardPOMDP.searchBid(observation);
            PotentialBid bid = new PotentialBid(null, Integer.toString(bidValue), adjustedHighestBid);
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
            if (desc.isCanPass()) {
                return canPassBid(validBid, trumpSuitBid, adjustedHighestBid);
            } else {
                return nonPassingBid(validBid, trumpSuitBid, adjustedHighestBid);
            }
        }
    }

    private Bid canPassBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid) {
        int hcp = highCardPoints();
        StringBuilder tempTrumpSuit = new StringBuilder();
        CardPOMDP tempPOMDP = new CardPOMDP(desc, shortTimout, getPlayerNumber(), tempTrumpSuit);
        if (adjustedHighestBid == null) {
            if (hcp > openBidThresh) {
                return openingBid(validBid, trumpSuitBid);
            } else {
                PotentialBid bid = new PotentialBid(null, "-2", adjustedHighestBid);
                if (!validBid.test(bid)) { //TODO handle if you can't bid.
                    throw new IllegalArgumentException();
                }
                return new Bid(false, null, -2, false, false);
            }
        } else if (adjustedHighestBid.getDeclarer().getTeam().containsPlayer(this)) { //Our teammate has the highest contract so far.
            if (adjustedHighestBid.isDoubling()) {
                tempTrumpSuit.setLength(0);
                tempTrumpSuit.append(adjustedHighestBid.getSuit());
                //Get an estimate of how many tricks will be won by this partnership.
                int estimatedTricksWon = (int) tempPOMDP.search(this.observation).getValue();
                //If it thinks that we can win this many tricks, then reodouble the bid. Otherwise pass it.
                if (estimatedTricksWon - desc.getTrickThreshold() >= adjustedHighestBid.getBidValue()) {
                    //TODO return redoubled bid.
                } else {
                    return new Bid(false, null, -2, false, false);
                }
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private Bid nonPassingBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid) {

    }


    private Bid openingBid(Predicate<PotentialBid> validbid, boolean trumpSuitBid) {
        return null;
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
    public void broadcastBid(Bid bid, int playerNumber) {
        //throw new UnsupportedOperationException();
    }
}
