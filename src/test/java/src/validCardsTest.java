package src;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.card.Card;
import src.functions.validCards;
import src.gameEngine.Hand;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class validCardsTest {

    private Hand currentTrick;

    @BeforeEach
    void setUp() {
        currentTrick = new Hand();
    }

    @Test
    void testValidCardLeadingAny() {
        Card card1 = new Card("CLUBS", "TEN");
        Card card2 = new Card("HEARTS", "FIVE");
        Card card3 = new Card("SPADES", "ACE");
        Card card4 = new Card("DIAMONDS", "TWO");
        Predicate<Card> validLeadingCard = validCards.getValidLeadingCardPredicate("any", null);
        assertTrue(validLeadingCard.test(card1));
        assertTrue(validLeadingCard.test(card2));
        assertTrue(validLeadingCard.test(card3));
        assertTrue(validLeadingCard.test(card4));
    }

    @Test
    void testValidCardLeadingTrump() {
        Card card1 = new Card("CLUBS", "TEN");
        Card card2 = new Card("HEARTS", "FIVE");
        Card card3 = new Card("SPADES", "ACE");
        Card card4 = new Card("DIAMONDS", "TWO");
        Predicate<Card> validLeadingCard = validCards.getValidLeadingCardPredicate("trump", "SPADES");
        assertFalse(validLeadingCard.test(card1));
        assertFalse(validLeadingCard.test(card2));
        assertTrue(validLeadingCard.test(card3));
        assertFalse(validLeadingCard.test(card4));
    }

    @Test
    void testValidCardLeadingNotTrump() {
        Card card1 = new Card("CLUBS", "TEN");
        Card card2 = new Card("HEARTS", "FIVE");
        Card card3 = new Card("SPADES", "ACE");
        Card card4 = new Card("DIAMONDS", "TWO");
        Predicate<Card> validLeadingCard = validCards.getValidLeadingCardPredicate("notTrump", "HEARTS");
        assertTrue(validLeadingCard.test(card1));
        assertFalse(validLeadingCard.test(card2));
        assertTrue(validLeadingCard.test(card3));
        assertTrue(validLeadingCard.test(card4));
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testValidLeadingCardNotValidType() {
        assertThrows(IllegalArgumentException.class, () -> validCards.getValidLeadingCardPredicate("tramp", null));
    }

    @Test
    void validCardEmptyTrick() {
        Card card1 = new Card("CLUBS", "TEN");
        Card card2 = new Card("HEARTS", "FIVE");
        Card card3 = new Card("SPADES", "ACE");
        Card card4 = new Card("DIAMONDS", "TWO");
        Predicate<Card> validLeadingCard = validCards.getValidLeadingCardPredicate("any", null);
        Predicate<Card> validCard = validCards.getValidCardPredicate("trick", null, this.currentTrick, validLeadingCard);
        assertTrue(validCard.test(card1));
        assertTrue(validCard.test(card2));
        assertTrue(validCard.test(card3));
        assertTrue(validCard.test(card4));
    }

    @Test
    void validCardNonEmptyTrick() {
        Card trickCard = new Card("CLUBS", "TWO");
        Card card1 = new Card("CLUBS", "TEN");
        Card card2 = new Card("HEARTS", "FIVE");
        Card card3 = new Card("SPADES", "ACE");
        Card card4 = new Card("DIAMONDS", "TWO");
        this.currentTrick.getCard(trickCard);
        Predicate<Card> validLeadingCard = validCards.getValidLeadingCardPredicate("any", null);
        Predicate<Card> validCard = validCards.getValidCardPredicate("trick", null, this.currentTrick, validLeadingCard);
        assertTrue(validCard.test(card1));
        assertFalse(validCard.test(card2));
        assertFalse(validCard.test(card3));
        assertFalse(validCard.test(card4));
    }

    @Test
    void canBePlayed() {
        Card trickCard = new Card("CLUBS", "TWO");
        Card card1 = new Card("CLUBS", "TEN");
        Card card2 = new Card("HEARTS", "FIVE");
        Card card3 = new Card("SPADES", "ACE");
        Card card4 = new Card("DIAMONDS", "TWO");
        Predicate<Card> validLeadingCard = validCards.getValidLeadingCardPredicate("any", null);
        Predicate<Card> validCard = validCards.getValidCardPredicate("trick", null, this.currentTrick, validLeadingCard);
        Hand playerHand = new Hand();
        Predicate<Card> canBePlayed = validCards.getCanBePlayedPredicate(playerHand, validCard);
        this.currentTrick.getCard(trickCard);
        playerHand.getCard(card1);
        playerHand.getCard(card2);
        playerHand.getCard(card3);
        playerHand.getCard(card4);
        assertTrue(canBePlayed.test(card1));
        assertFalse(canBePlayed.test(card2));
        assertFalse(canBePlayed.test(card3));
        assertFalse(canBePlayed.test(card4));
    }

    @Test
    void canBePlayedEmptyTrick() {
        Card card1 = new Card("CLUBS", "TEN");
        Card card2 = new Card("HEARTS", "FIVE");
        Card card3 = new Card("SPADES", "ACE");
        Card card4 = new Card("DIAMONDS", "TWO");
        Predicate<Card> validLeadingCard = validCards.getValidLeadingCardPredicate("any", null);
        Predicate<Card> validCard = validCards.getValidCardPredicate("trick", null, this.currentTrick, validLeadingCard);
        Hand playerHand = new Hand();
        Predicate<Card> canBePlayed = validCards.getCanBePlayedPredicate(playerHand, validCard);
        playerHand.getCard(card1);
        playerHand.getCard(card2);
        playerHand.getCard(card3);
        playerHand.getCard(card4);
        assertTrue(canBePlayed.test(card1));
        assertTrue(canBePlayed.test(card2));
        assertTrue(canBePlayed.test(card3));
        assertTrue(canBePlayed.test(card4));
    }

    @Test
    void canBePlayedNotInHand() {
        Card card1 = new Card("CLUBS", "TEN");
        Card card2 = new Card("HEARTS", "FIVE");
        Card card3 = new Card("SPADES", "ACE");
        Card card4 = new Card("DIAMONDS", "TWO");
        Predicate<Card> validLeadingCard = validCards.getValidLeadingCardPredicate("any", null);
        Predicate<Card> validCard = validCards.getValidCardPredicate("trick", null, this.currentTrick, validLeadingCard);
        Hand playerHand = new Hand();
        Predicate<Card> canBePlayed = validCards.getCanBePlayedPredicate(playerHand, validCard);
        playerHand.getCard(card2);
        playerHand.getCard(card3);
        playerHand.getCard(card4);
        assertFalse(playerHand.getHand().contains(card1));
        assertFalse(canBePlayed.test(card1));
    }

    @Test
    void canBePlayedNoValidCards() {
        Card trickCard = new Card("CLUBS", "TEN");
        Card card2 = new Card("HEARTS", "FIVE");
        Card card3 = new Card("SPADES", "ACE");
        Card card4 = new Card("DIAMONDS", "TWO");
        Predicate<Card> validLeadingCard = validCards.getValidLeadingCardPredicate("any", null);
        Predicate<Card> validCard = validCards.getValidCardPredicate("trick", null, this.currentTrick, validLeadingCard);
        Hand playerHand = new Hand();
        Predicate<Card> canBePlayed = validCards.getCanBePlayedPredicate(playerHand, validCard);
        this.currentTrick.getCard(trickCard);
        playerHand.getCard(card2);
        playerHand.getCard(card3);
        playerHand.getCard(card4);
        assertTrue(canBePlayed.test(card2));
        assertTrue(canBePlayed.test(card3));
        assertTrue(canBePlayed.test(card4));
        playerHand.getCard(new Card("CLUBS", "FIVE"));
        assertFalse(canBePlayed.test(card2));
        assertFalse(canBePlayed.test(card3));
        assertFalse(canBePlayed.test(card4));
    }
}
