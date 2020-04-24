package src.player;

import src.ai.CardPOMDP;
import src.ai.GameObservation;
import src.card.Card;
import src.deck.Deck;
import src.bid.Bid;
import src.bid.ContractBid;
import src.gameEngine.Hand;
import src.bid.PotentialBid;
import src.parser.GameDesc;
import src.rdmEvents.Swap;

import java.util.function.Predicate;

public class POMDPPlayer extends Player {
    private GameObservation observation;
    private CardPOMDP cardPOMDP;
    private static final long timeout = 5000;
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
        if(currentTrick.getHandSize() == 0){
            //Signify that this player starts the trick
            observation.setTrickStartedBy(getPlayerNumber());
        }
        Card card = cardPOMDP.search(observation);
        assert super.getCanBePlayed().test(card);
        return super.getHand().giveCard(card);
    }

    @Override
    public void broadcastPlay(Card card, int playerNumber) {
        observation.updateGameState(playerNumber, card);
    }

    @Override
    public Bid makeBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, boolean bidNo, boolean canBidBlind) {
        throw new UnsupportedOperationException();
    }

    public Swap getSwap(Player strongPlayer){
        throw new UnsupportedOperationException();
    }

    public void broadcastSwap(Swap swap){
        throw new UnsupportedOperationException();
    }

    @Override
    public void broadcastBid(Bid bid, int playerNumber, ContractBid adjustedHighestBid) {
        throw new UnsupportedOperationException();
    }
}
