package src.player;

import src.card.Card;
import src.bid.Bid;
import src.bid.ContractBid;
import src.gameEngine.Hand;
import src.bid.PotentialBid;
import src.rdmEvents.Swap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Object to represent a player playing on the same machine as the one the game engine is being run on.
 */
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

//    private static Iterator<String> colours = Arrays.asList(text_colours).iterator();

    private String colour;

    public LocalPlayer(int playerNumber, Predicate<Card> validCard) {
        super(playerNumber, validCard);
        this.colour = text_colours[playerNumber];
    }

    public LocalPlayer(int playerNumber) {
        super(playerNumber);
        this.colour = text_colours[playerNumber];
    }

    public LocalPlayer() {
        this.colour = text_colours[0];
    }


    /**
     * @param trumpSuit    current trump suit
     * @param currentTrick current trick
     *                     Presents local user with series of options to get information on the state of the game, and then to play a card
     *                     from their hand.
     * @return the selected card by the player
     */
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
        System.out.println("Player " + (playerNumber + 1) + " played " + card.toString());
    }

    //Indicate the swap that was performed
    @Override
    public void broadcastSwap(Swap swap) {
        System.out.println("Player " + (swap.getOriginalPlayerIndex() + 1) + " swapped a card with Player " + (swap.getOtherPlayerCardNumber() + 1)) ;
    }

    //Asks if a player would like to swap a card with a random opponent
    @Override
    public Swap getSwap(Player rdmStrongPlayer) {
        int currentPlayerIndex = this.getPlayerNumber();
        int rdmStrongPlayerIndex = rdmStrongPlayer.getPlayerNumber();
        System.out.println("Player " + (currentPlayerIndex + 1) + ", you have been offered a card swap - you have the ability to swap one of your cards" +
                " with one of Player " + (rdmStrongPlayerIndex + 1) + "'s");
        System.out.println("Your Cards: " + getHand().toString());
        System.out.println("Their Cards: " + rdmStrongPlayer.getHand().toString());
        System.out.println("Would you like to swap a card? (y/n)");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.next();
        if (answer.equals("y")) {
            int currentPlayerCardNumber = -1;
            int rdmStrongPlayerCardNumber = -1;
            do {
                System.out.println("Choose your card: ");
                currentPlayerCardNumber = scanner.nextInt();
            } while (currentPlayerCardNumber < 0 || currentPlayerCardNumber >= getHand().getHandSize());
            System.out.println("Card chosen: " + getHand().get(currentPlayerCardNumber));
            do {
                System.out.println("Choose a card from your opponent: ");
                rdmStrongPlayerCardNumber = scanner.nextInt();
            } while (rdmStrongPlayerCardNumber < 0 || rdmStrongPlayerCardNumber >= rdmStrongPlayer.getHand().getHandSize());
            System.out.println("Card chosen: " + rdmStrongPlayer.getHand().get(rdmStrongPlayerCardNumber));
            //Return a live swap
            return new Swap(currentPlayerIndex, currentPlayerCardNumber, rdmStrongPlayerIndex, rdmStrongPlayerCardNumber, "live");
        }
        //Signal swap offer was rejected
        else {
            return new Swap(0,0,0,0, "dead");
        }
    }

    @Override
    public void broadcastBid(Bid bid, int playerNumber, ContractBid adjustedHighestBid) {
        StringBuilder output = new StringBuilder();
        output.append("Player ").append(playerNumber + 1);
        if (bid.isDoubling()) {
            if (adjustedHighestBid.isRedoubling()) {
                output.append(" redoubled");
            }
            else {
                output.append(" doubled");
            }
        }
        else{
            output.append(" bid ").append(bid.getBidValue()).append(bid.getSuit() != null ? (" " + bid.getSuit()) : "").append(bid.isBlind() ? " blind" : "");
        }
        System.out.println(output);
    }

    /**
     * @param validBid function that determines of bid is valid
     * @return new bid
     */
    @Override
    public Bid makeBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, boolean firstRound, boolean canBidBlind) {
        System.out.print(this.colour);
        System.out.println("-------------------------------------");
        System.out.println("-------------------------------------");
        System.out.println("Player " + (super.getPlayerNumber() + 1));
        System.out.println("-------------------------------------");
        System.out.println("-------------------------------------");

        int option = -1;

        String bidInput = null;
        String bidSuit = null;
        boolean doubling = false;
        boolean bidBlind = false;

        InputStreamReader r = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(r);
        if (canBidBlind) {
            System.out.println("Select Option:");
            System.out.println("    1. Bid with seeing cards");
            System.out.println("    2. Bid blind");
            while (option > 2 || option < 1) {
                Scanner scan = new Scanner(System.in);
                option =  scan.nextInt();
            }
            if (option == 2) {
                bidBlind = true;
            }
        }

        if (!bidBlind) {
            System.out.println("Current Hand: " + super.getHand().toString());
        }

        try {
            do {
                System.out.println("Enter your bid: (enter '-2' to pass, 'd' to double/redouble - if these are valid options)");
                bidSuit = null;
                bidInput = br.readLine();

                if (trumpSuitBid && bidInput.matches("\\d+")) {
                    System.out.println("Enter your trump suit ('NO TRUMP' for no trump)");
                    bidSuit = br.readLine();
                }
            } while (!validBid.test(new PotentialBid(bidSuit, bidInput, adjustedHighestBid, this, firstRound)));
        }
        catch (IOException e) {
            System.out.println(e.getStackTrace());
            System.exit(0);
        }

        System.out.println(ANSI_RESET);
        int finalBidInput = 0;
        if (bidInput.equals("d")) {
            doubling = true;
        }
        else {
            finalBidInput = Integer.parseInt(bidInput);
        }
        return new Bid(doubling, bidSuit, finalBidInput, bidBlind, false);
    }

    @Override
    public void broadcastDummyHand(int playerNumber, List<Card> dummyHand) {
        System.out.println("Dummy hand of Player " + (playerNumber + 1) + ": " + dummyHand);
    }
}
