package src.player;

import src.card.Card;
import src.gameEngine.Bid;
import src.gameEngine.Hand;

import java.util.Random;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class RandomPlayer extends Player {
    Random random;
    RandomPlayer(int playerNumber, Predicate<Card> canBePlayed) {
        super(playerNumber, canBePlayed);
        random = new Random();
    }

    @Override
    public Card playCard(String trumpSuit, Hand currentTrick) {
        Card[] validCards = (Card[]) super.getHand().getHand()
                .stream()
                .filter(getCanBePlayed())
                .toArray();
        return validCards[random.nextInt(validCards.length)];
    }

    @Override
    public void broadcastPlay(Card card, int playerNumber) {
    }

    @Override
    public Bid makeBid(IntPredicate validBid) {
        int handSize = super.getHand().getHandSize();
        int bid;
        do {
            bid = random.nextInt(handSize);
        } while (!validBid.test(bid));
    }

    @Override
    public void broadcastBid(Bid bid, int playerNumber) {
    }
}
