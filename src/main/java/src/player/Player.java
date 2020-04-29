package src.player;

import src.card.Card;
import src.functions.validCards;
import src.bid.Bid;
import src.bid.ContractBid;
import src.gameEngine.Hand;
import src.bid.PotentialBid;
import src.rdmEvents.Swap;
import src.team.Team;
import src.parser.GameDesc;

import java.util.List;
import java.util.function.Predicate;

/**
 * Abstract class to represent a player
 */
public abstract class Player {
    private int playerNumber;
    private Hand hand = null;
    private Bid bid;
    private Predicate<Card> canBePlayed;
    private Team team;

    Player(int playerNumber, Predicate<Card> canBePlayed) {
        this.playerNumber = playerNumber;
        this.hand = new Hand();
        this.canBePlayed = validCards.getCanBePlayedPredicate(this.hand, canBePlayed);
    }

    Player(int playerNumber) {
        this.playerNumber = playerNumber;
        this.hand = new Hand();
        this.canBePlayed = null;
    }

    Player() {
        this.hand = new Hand();
        this.canBePlayed = null;
    }

    public void startHand(StringBuilder trumpSuit, int handSize) {

    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    /**
     * Initialises the player..
     *
     * @param validCard Predicate that checks if a card is valid.
     */
    public void initPlayer(Predicate<Card> validCard, GameDesc desc, StringBuilder trumpSuit) {
        this.canBePlayed = validCards.getCanBePlayedPredicate(this.hand, validCard);
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public Hand getHand() {
        return hand;
    }

    public abstract Card playCard(String trumpSuit, Hand currentTrick);

    public abstract void broadcastPlay(Card card, int playerNumber);

    public abstract Bid makeBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid,
                                boolean firstRound, boolean canBidBlind);

    public Predicate<Card> getCanBePlayed() {
        return canBePlayed;
    }

    public void setCanBePlayed(Predicate<Card> canBePlayed) {
        this.canBePlayed = canBePlayed;
    }

    public abstract void broadcastBid(Bid bid, int playerNumber, ContractBid adjustedHighestBid);

    public abstract Swap getSwap(Player strongPlayer);

    public abstract void broadcastSwap(Swap swap);

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public Bid getBid() {
        return bid;
    }

    public void setBid(Bid bid) {
        this.bid = bid;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void broadcastDummyHand(int playerNumber, List<Card> dummyHand){

    }
}
