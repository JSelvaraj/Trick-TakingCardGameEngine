package src.functions;

import src.card.Card;
import src.gameEngine.Hand;

import java.util.function.Predicate;

public class validCards {

    @SuppressWarnings("unused")
    public static Predicate<Card> getValidCardPredicate(String nextLegalCardMode, String trumpSuit, Hand currentTrick) {
        //TODO work for more than just standard.
        return (c) -> c.getSUIT().equals(currentTrick.get(0).getSUIT());
    }

    public static Predicate<Card> getValidLeadingCardPredicate(String leadingCardForTrick, String trumpSuit) {
        switch (leadingCardForTrick) {
            case "any":
                return (c) -> true;
            case "trump":
                return (c) -> c.getSUIT().equals(trumpSuit);
            case "notTrump":
                return (c) -> !c.getSUIT().equals(trumpSuit);
//                case "break":
//                    TODO
        }
        throw new IllegalArgumentException("Not a valid leading card picking mode");
    }

    public static Predicate<Card> getIsCardValidPredicate(Hand playerHand, Predicate<Card> validCard){
        return (card) -> playerHand.getHand().stream().filter(validCard).anyMatch(card::equals) || playerHand.getHand().stream().noneMatch(validCard);
    }
}
