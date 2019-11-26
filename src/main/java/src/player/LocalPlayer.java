package src.player;

import src.card.Card;
import src.gameEngine.Hand;

import java.util.Scanner;
import java.util.function.Predicate;

public class LocalPlayer extends Player {

    public LocalPlayer(int playerNumber, Predicate<Card> validCard) {
        super(playerNumber, validCard);
    }

    public LocalPlayer(int playerNumber){
        super(playerNumber);
    }

    @Override
    public Card playCard(String trumpSuit, Hand currentTrick) {
        while (true) {
            System.out.println();
            System.out.println("Select Option:");
            System.out.println("    1. View hand");
            System.out.println("    2. View current trick");
            System.out.println("    3. Current Trump Suit");
            System.out.println("    4. Play card");
            int option = -1;
            int cardNumber = -1;

            while (option > 4 || option < 1) {
                Scanner scanner = new Scanner(System.in);
                option = scanner.nextInt();
            }
            switch (option) {
                case 1:
                    System.out.println();
                    System.out.println("Current Hand: " + super.getHand().toString());
                    System.out.println();
                    break;
                case 2:
                    System.out.println();
                    System.out.println("Current Trick: " + currentTrick.toString());
                    System.out.println();
                    break;
                case 3:
                    System.out.println();
                    System.out.println("Current Trump: " + trumpSuit);
                    System.out.println();
                    break;
                case 4:
                    System.out.println();
                    System.out.println("Current Hand: " + super.getHand().toString());
                    System.out.println();
                    Scanner scanner = new Scanner(System.in);
                    do {
                        System.out.println("Choose your card: ");
                        cardNumber = scanner.nextInt();
                    } while (cardNumber < 0 || cardNumber >= super.getHand().getHandSize() || !super.getCanBePlayed().test(super.getHand().get(cardNumber)));
                    return super.getHand().giveCard(cardNumber);
            }

        }
    }

    @Override
    public int makeBid() {
        throw new UnsupportedOperationException();
    }

}
