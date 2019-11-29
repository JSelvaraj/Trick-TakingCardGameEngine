package src.player;

import src.card.Card;
import src.gameEngine.Bid;
import src.gameEngine.Hand;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class LocalPlayer extends Player {
    //Text colours.
    static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String[] text_colours = {ANSI_RED, ANSI_YELLOW, ANSI_BLUE, ANSI_PURPLE, ANSI_CYAN, ANSI_GREEN}; //TODO add more colours
    //background colours
    private static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    private static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    private static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    private static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    private static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    private static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    private static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    private static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
    private static final String[] background_colours = {ANSI_BLACK_BACKGROUND, ANSI_RED_BACKGROUND, ANSI_GREEN_BACKGROUND, ANSI_YELLOW_BACKGROUND, ANSI_BLUE_BACKGROUND, ANSI_PURPLE_BACKGROUND, ANSI_CYAN_BACKGROUND, ANSI_WHITE_BACKGROUND};

    private static Iterator<String> colours = Arrays.asList(text_colours).iterator();

    private String colour;
    //Determines if this move has already been printed for local players.
    private static boolean localPrinted;

    public LocalPlayer(int playerNumber, Predicate<Card> validCard) {
        super(playerNumber, validCard);
        this.colour = colours.next();
    }

    public LocalPlayer(int playerNumber) {
        super(playerNumber);
        this.colour = colours.next();
    }

    @Override
    public Card playCard(String trumpSuit, Hand currentTrick) {
        System.out.println("Current Trick: " + currentTrick.toString());
        System.out.print(this.colour);
        System.out.println("-------------------------------------");
        System.out.println("-------------------------------------");
        System.out.println("Player " + (super.getPlayerNumber() + 1));
        System.out.println("Current Hand: " + super.getHand().toString());
        System.out.println("-------------------------------------");
        System.out.println("-------------------------------------");
        int cardNumber = -1;
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.println("Choose your card: ");
            cardNumber = scanner.nextInt();
        } while (cardNumber < 0 || cardNumber >= super.getHand().getHandSize() || !super.getCanBePlayed().test(super.getHand().get(cardNumber)));
        System.out.print(ANSI_RESET);
        return super.getHand().giveCard(cardNumber);
    }


    @Override
    public void broadcastPlay(Card card, int playerNumber) {
        if (!localPrinted) {
            System.out.println("Player " + (playerNumber + 1) + " played " + card.toString());
        }
        localPrinted = true;
    }
    @Override
    public void broadcastBid(Bid bid, int playerNumber){
        if(!localPrinted){
            System.out.println("Player " + (playerNumber + 1) + " bid " + bid.getBidValue() + (bid.isBlind() ? " blind" : ""));
        }
    }

    public static void resetLocalPrinted() {
        localPrinted = false;
    }

    @Override
    public Bid makeBid(IntPredicate validBid) {
        System.out.print(this.colour);
        System.out.println("-------------------------------------");
        System.out.println("-------------------------------------");
        System.out.println("Player " + (super.getPlayerNumber() + 1));
        System.out.println("-------------------------------------");
        System.out.println("-------------------------------------");
        int option = -1;
        int bidNumber = 0;
        boolean bidBlind = true;
        System.out.println("Select Option:");
        System.out.println("    1. Bid with seeing cards");
        System.out.println("    2. Bid blind");
        while (option > 2 || option < 1) {
            Scanner scanner = new Scanner(System.in);
            option = scanner.nextInt();
        }
        switch (option) {
            case 1:
                System.out.println("Current Hand: " + super.getHand().toString());
                bidBlind = false;
            case 2:
                Scanner scanner = new Scanner(System.in);
                do {
                    System.out.println("Enter your bid:");
                    bidNumber = scanner.nextInt();
                } while (!validBid.test(bidNumber));
                break;
        }
        System.out.println(ANSI_RESET);
        return new Bid(bidNumber, bidBlind);

    }

}
