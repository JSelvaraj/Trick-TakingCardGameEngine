package src.player;

import src.card.Card;
import src.gameEngine.Bid;
import src.gameEngine.ContractBid;
import src.gameEngine.Hand;
import src.gameEngine.PotentialBid;
import src.rdmEvents.RdmEvent;
import src.rdmEvents.Swap;

import java.util.Random;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class RandomPlayer extends Player {
    Random random;

    public RandomPlayer(int playerNumber, Predicate<Card> canBePlayed) {
        super(playerNumber, canBePlayed);
        random = new Random();
    }

    public RandomPlayer(int playerNumber) {
        super(playerNumber);
        random = new Random();
    }

    public RandomPlayer() {
        random = new Random();
    }

    @Override
    public Card playCard(String trumpSuit, Hand currentTrick) {
        Object[] validCards = super.getHand().getHand()
                .stream()
                .filter(getCanBePlayed())
                .toArray();
        return super.getHand().giveCard((Card) validCards[random.nextInt(validCards.length)]);
    }

    @Override
    public void broadcastPlay(Card card, int playerNumber) {
    }

    @Override
    public Bid makeBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, Player[] players, ContractBid adjustedHighestBid) {
        int handSize = super.getHand().getHandSize();
        int bid;
        do {
            bid = random.nextInt(handSize);
        } while (!validBid.test(new PotentialBid(null, Integer.toString(bid), this.getPlayerNumber(), players, adjustedHighestBid)));
        return new Bid(false, null, bid, true);
    }

    @Override
    public void broadcastBid(Bid bid, int playerNumber) {
    }

    @Override
    public Swap getSwap(Player rdmStrongPlayer) {
        return new Swap(0, 0, 0,0, "dead");
    }

    @Override
    public void broadcastSwap(Swap swap) {

    }
}
