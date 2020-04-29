package src.functions;

import src.ai.GameObservation;
import src.card.Card;
import src.gameEngine.Hand;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class validCards {

    /**
     * @param nextLegalCardMode         Mode for if a card is legal
     * @param trumpSuit                 The trump suit. Currently unused, as this doesn't factor in.
     * @param currentTrick              The current trick being played.
     * @param validLeadingCardPredicate Predicate that checks if a card is a valid leading card. Will be used if the current trick has size 0.
     * @return Predicate that checks if the card is valid to play.
     */
    @SuppressWarnings("unused")
    public static Predicate<Card> getValidCardPredicate(String nextLegalCardMode, StringBuilder trumpSuit, Hand currentTrick, Predicate<Card> validLeadingCardPredicate) {
        //TODO work for more than just standard.
        return (c) -> currentTrick.getHand().size() == 0 ? validLeadingCardPredicate.test(c) : c.getSUIT().equals(currentTrick.get(0).getSUIT());
    }

    /**
     * @param leadingCardForTrick String containing the criteria for a leading card to be valid
     * @param trumpSuit           The trump suit, can be null.
     * @return Predicate that checks if a given card is a valid leading card.
     */
    public static Predicate<Card> getValidLeadingCardPredicate(String leadingCardForTrick, StringBuilder trumpSuit, AtomicBoolean breakFlag) {
        switch (leadingCardForTrick) {
            case "break":
                return (c) -> breakFlag.get() || !c.getSUIT().equals(trumpSuit.toString());
            case "any":
                return (c) -> true;
            case "trump":
                return (c) -> c.getSUIT().equals(trumpSuit.toString());
            case "notTrump":
                return (c) -> !c.getSUIT().equals(trumpSuit.toString());
//                case "break":
//                    TODO
        }
        throw new IllegalArgumentException("Not a valid leading card picking mode");
    }

    /**
     * @param playerHand The hand of the player that this predicate will check against. This will closure this.
     * @param validCard  Predicate that checks that a card is valid
     * @return Predicate that returns true if a card is in the player hand, and either the card is valid or the player has no valid cards (and so can play anything).
     */
    public static Predicate<Card> getCanBePlayedPredicate(Hand playerHand, Predicate<Card> validCard) {
        return (card) -> playerHand.getHand().contains(card) && (validCard.test(card) || (playerHand.getHand().stream().noneMatch(validCard)));
    }

    /**
     * Create a function that takes a trick and a function, and tests if that card is valid in the current trick.
     *
     * @param validLeadingCardFunction A function checking if a card is a valid leading card.
     *
     * @return BiFunction taking a hand and a card, which tests if that card can be played.
     */
    public static BiFunction<GameObservation, Card, Boolean> getValidCardFunction(BiFunction<Boolean, Card, Boolean> validLeadingCardFunction){
        return (observation, card) -> observation.getCurrentTrick().size() == 0 ? validLeadingCardFunction.apply(observation.isBreakFlag(), card) : card.getSUIT().equals(observation.getCurrentTrick().get(0).getSUIT());
    }

    public static BiFunction<Boolean, Card, Boolean> getValidLeadingCardFunction(String leadingCardForTrick, StringBuilder trumpSuit){
        switch (leadingCardForTrick) {
            case "break":
                return (b,c) -> b || !c.getSUIT().equals(trumpSuit.toString());
            case "any":
                return (b, c) -> true;
            case "trump":
                return (b, c) -> c.getSUIT().equals(trumpSuit.toString());
            case "notTrump":
                return (b, c) -> !c.getSUIT().equals(trumpSuit.toString());
//                case "break":
//                    TODO
        }
        throw new IllegalArgumentException("Not a valid leading card picking mode");
    }



}
