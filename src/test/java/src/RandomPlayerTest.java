package src;

import org.junit.jupiter.api.Test;
import src.card.Card;
import src.functions.validBids;
import src.functions.validCards;
import src.gameEngine.Hand;
import src.player.RandomPlayer;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomPlayerTest {

    @Test
    void playCardEmptyTrick() {
        Card card1 = new Card("CLUBS", "TEN");
        Card card2 = new Card("HEARTS", "FIVE");
        Card card3 = new Card("SPADES", "ACE");
        Card card4 = new Card("DIAMONDS", "TWO");
        Hand currentTrick = new Hand();
        Predicate<Card> validLeadingCard = validCards.getValidLeadingCardPredicate("any", null, null);
        Predicate<Card> validCard = validCards.getValidCardPredicate("trick", null, currentTrick, validLeadingCard);
        RandomPlayer randomPlayer = new RandomPlayer(0, validCard);
        randomPlayer.getHand().getCard(card1);
        randomPlayer.getHand().getCard(card2);
        randomPlayer.getHand().getCard(card3);
        randomPlayer.getHand().getCard(card4);
        for (int i = 0; i < 100; i++) assertTrue(validCard.test(randomPlayer.playCard(null, null)));
    }

    @Test
    void playCardNonEmptyTrick() {
        Card card1 = new Card("CLUBS", "TEN");
        Card card2 = new Card("HEARTS", "FIVE");
        Card card3 = new Card("SPADES", "ACE");
        Card card4 = new Card("DIAMONDS", "TWO");
        Card card5 = new Card("CLUBS", "FIVE");
        Card card6 = new Card("HEARTS", "TWO");
        Card card7 = new Card("SPADES", "JACK");
        Card card8 = new Card("DIAMONDS", "SEVEN");
        Card card9 = new Card("CLUBS", "FOUR");
        Card card10 = new Card("HEARTS", "SIX");
        Card card11 = new Card("SPADES", "ACE");
        Card card12 = new Card("DIAMONDS", "QUEEN.");
        Hand currentTrick = new Hand();
        Predicate<Card> validLeadingCard = validCards.getValidLeadingCardPredicate("any", null, null);
        Predicate<Card> validCard = validCards.getValidCardPredicate("trick", null, currentTrick, validLeadingCard);
        RandomPlayer randomPlayer = new RandomPlayer(0, validCard);
        //Fills the players hand.
        randomPlayer.getHand().getCard(card1);
        randomPlayer.getHand().getCard(card2);
        randomPlayer.getHand().getCard(card3);
        randomPlayer.getHand().getCard(card4);
        randomPlayer.getHand().getCard(card6);
        randomPlayer.getHand().getCard(card7);
        randomPlayer.getHand().getCard(card8);
        randomPlayer.getHand().getCard(card9);
        randomPlayer.getHand().getCard(card10);
        randomPlayer.getHand().getCard(card11);
        randomPlayer.getHand().getCard(card12);
        //Adds leading card
        currentTrick.getCard(card5);
        for (int i = 0; i < 100; i++) assertTrue(validCard.test(randomPlayer.playCard(null, currentTrick)));
    }

    @Test
    void makeBid() {
        int minBid = 0;
        int maxBid = 10;
        IntPredicate validBid = validBids.isValidBidValue(minBid, maxBid);
        RandomPlayer randomPlayer = new RandomPlayer(0, null);
        for (int i = 0; i < 10; i++) randomPlayer.getHand().getCard(new Card("", ""));
        for (int i = 0; i < 1000; i++) assertTrue(validBid.test(randomPlayer.makeBid(validBid).getBidValue()));
    }
}