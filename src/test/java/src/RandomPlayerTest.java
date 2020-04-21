package src;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import src.card.Card;
import src.functions.validBids;
import src.functions.validCards;
import src.bid.Bid;
import src.gameEngine.Hand;
import src.bid.PotentialBid;
import src.player.LocalPlayer;
import src.player.Player;
import src.player.RandomPlayer;
import src.team.Team;

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
        for (int i = 0; i < 100; i++){
            randomPlayer.getHand().getCard(card1);
            randomPlayer.getHand().getCard(card2);
            randomPlayer.getHand().getCard(card3);
            randomPlayer.getHand().getCard(card4);
            assertTrue(validCard.test(randomPlayer.playCard(null, null)));
            randomPlayer.getHand().dropHand();
        }
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
        //Adds leading card
        currentTrick.getCard(card5);
        for (int i = 0; i < 100; i++){
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
            assertTrue(validCard.test(randomPlayer.playCard(null, currentTrick)));
            randomPlayer.getHand().dropHand();
        }
    }

    @Test
    void makeBid() {
        JSONObject bidObject = new JSONObject();
        bidObject.put("minBid", 0);
        bidObject.put("maxBid", 10);
        Predicate<PotentialBid> validBid = validBids.isValidBidValue(bidObject, 4);
        RandomPlayer randomPlayer = new RandomPlayer(0, null);
        for (int i = 0; i < 10; i++) randomPlayer.getHand().getCard(new Card("", ""));
        for (int i = 0; i < 1000; i++) {
            Bid randomBid = randomPlayer.makeBid(validBid, false, null, 0);
            assertTrue(validBid.test(new PotentialBid(randomBid.getSuit(), Integer.toString(randomBid.getBidValue()),
                    null, randomPlayer, 0)));
        }
    }
}