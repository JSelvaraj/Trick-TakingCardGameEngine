package src.player;

import src.card.Card;
import src.bid.Bid;
import src.bid.ContractBid;
import src.gameEngine.Hand;
import src.bid.PotentialBid;
import src.rdmEvents.Swap;

import java.util.Random;
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
        System.out.println(card + " played by Player " + (playerNumber + 1));
    }

    @Override
    public Bid makeBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid) {
        int handSize = super.getHand().getHandSize();
        int bid;
        String suit = null;
        do {
            bid = random.nextInt(handSize);
            if (trumpSuitBid) {
                suit = "SPADES";
            }
        } while (!validBid.test(new PotentialBid(null, Integer.toString(bid), adjustedHighestBid)));
        return new Bid(false, suit, bid, true);
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
