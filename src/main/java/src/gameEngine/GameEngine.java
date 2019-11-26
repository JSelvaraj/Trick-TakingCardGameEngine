package src.gameEngine;

import org.apache.commons.lang3.ArrayUtils;
import src.card.Card;
import src.card.CardComparator;
import src.deck.Deck;
import src.deck.Shuffle;
import src.functions.validCards;
import src.parser.GameDesc;
import src.player.LocalPlayer;
import src.player.Player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.function.Predicate;

public class GameEngine {


    private GameDesc desc;
    private String trumpSuit;
    private Hand currentTrick = new Hand(); //functionally the trick is just a hand visible to the entire table
    private boolean breakFlag = false; // if the trump/hearts are broken
    private int handsPlayed = 0;
    HashMap<int[], Integer> tricksWonTable;
    HashMap<int[], Integer> scoreTable;

    private Predicate<Card> validCard;
    private Predicate<Card> validLeadingCard;

    public GameEngine(GameDesc desc) {
        this.desc = desc;
        this.validLeadingCard = validCards.getValidLeadingCardPredicate(desc.getLeadingCardForEachTrick(), this.trumpSuit);
        this.validCard = validCards.getValidCardPredicate("trick", this.trumpSuit, this.currentTrick, this.validLeadingCard);
    }


    public static void main(String[] args) {
        String[] suits = {"HEARTS", "CLUBS", "DIAMONDS", "SPADES"};
        String[] ranks = {"ACE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "JACK", "QUEEN", "KING"};
        String[] rankOrder = {"TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "JACK", "QUEEN", "KING", "ACE"};
        int[][] teams = {{1, 3}, {2, 4}};
        Player[] playerArray = {new LocalPlayer(0), new LocalPlayer(1), new LocalPlayer(2), new LocalPlayer(3)};

        GameDesc desc = new GameDesc(4,
                teams,
                696969,
                suits, ranks,
                rankOrder,
                false,
                "tricksWon",
                "lastDealt",
                null,
                "any",
                "tricksWon",
                3,
                5,
                "trick",
                "standard",
                "prevWinner");

        main(desc, 0, playerArray);
    }

    public static void main(GameDesc gameDesc, int dealer , Player[] playerArray) {
        GameEngine game = new GameEngine(gameDesc);


        /* initialize each players hands */

        game.tricksWonTable = new HashMap<>();
        game.scoreTable = new HashMap<>();
        for (Player player : playerArray) {
            player.initCanBePlayed(game.getValidCard());
        }
        for (int[] team : gameDesc.getTeams()) {
            game.tricksWonTable.put(team, 0);
            game.scoreTable.put(team, 0);
        }
        Deck deck = new Deck(gameDesc.getDECK()); // make standard deck from a linked list of Cards
        Shuffle.seedGenerator((int) gameDesc.getSEED()); // TODO remove cast to int
        game.printScore();
        do {
            int currentPlayer = dealer;
            Shuffle.shuffle(deck.cards); //shuffle deck according to the given seed
            game.dealCards(playerArray, deck, currentPlayer);
            if (gameDesc.isDEALCARDSCLOCKWISE())
                currentPlayer = (currentPlayer + 1) % playerArray.length; //ensures that first card played is from dealer's left
            else
                currentPlayer = Math.floorMod((currentPlayer - 1), playerArray.length); //ensures that first card played is from dealers right

            do {
                for (int i = 0; i < playerArray.length; i++) {
                    System.out.println("Current Trick: " + game.currentTrick.toString());
                    System.out.println("-------------------------------------");
                    System.out.println("-------------------------------------");
                    System.out.println("Player " + (currentPlayer + 1));
                    System.out.println("-------------------------------------");
                    System.out.println("-------------------------------------");
                    game.currentTrick.getCard(playerArray[currentPlayer].playCard(game.trumpSuit, game.currentTrick));

                    if (gameDesc.isDEALCARDSCLOCKWISE()) currentPlayer = (currentPlayer + 1) % playerArray.length;
                    else currentPlayer = Math.floorMod((currentPlayer - 1), playerArray.length);
                }

                Card winningCard = game.winningCard();

                /* works out who played the winning card */ //0971
                //Roll back player to the person who last played a card.
                if (gameDesc.isDEALCARDSCLOCKWISE()) {
                    currentPlayer = Math.floorMod((currentPlayer - 1), playerArray.length);
                } else {
                    currentPlayer = (currentPlayer + 1) % playerArray.length;
                }

                for (int i = playerArray.length - 1; i >= 0; i--) {
                    if (game.currentTrick.get(i).equals(winningCard)) {
                        break;
                    } else {
                        if (gameDesc.isDEALCARDSCLOCKWISE()) {
                            currentPlayer = Math.floorMod((currentPlayer - 1), playerArray.length);
                        } else {
                            currentPlayer = (currentPlayer + 1) % playerArray.length;
                        }
                    }
                }

                for (int[] team : gameDesc.getTeams()) {
                    if (ArrayUtils.contains(team, currentPlayer)) {
                        game.tricksWonTable.put(team, (game.tricksWonTable.get(team) + 1));
                        System.out.println("Player " + (currentPlayer + 1) + " was the winner of the trick with the " + winningCard.toString());
                        System.out.println("Tricks won: " + game.tricksWonTable.get(team));
                        break;
                    }
                }
                game.currentTrick.dropHand();
            } while (playerArray[0].getHand().getHandSize() > 0);
            game.handsPlayed++;
            if (gameDesc.getCalculateScore().equals("tricksWon")) {
                for (int[] team : gameDesc.getTeams()) {
                    int score = game.tricksWonTable.get(team);
                    if (score > gameDesc.getTrickThreshold()) { // if score greater than trick threshold
                        game.scoreTable.put(team, (game.scoreTable.get(team) + (score - gameDesc.getTrickThreshold()))); // add score to team's running total
                    }
                    game.tricksWonTable.put(team, 0);
                }
            }
            game.printScore();
        } while (game.gameEnd());





        /* one trick */


    }

    private boolean gameEnd() {
        switch (desc.getGameEnd()) {
            case "scoreThreshold":
                for (int[] team : desc.getTeams()) {
                    if (scoreTable.get(team) >= desc.getScoreThreshold())
                        return true;
                }
                break;
            case "handsPlayed":
                if (handsPlayed >= desc.getScoreThreshold()) {
                    return true;
                }
                break;
        }
        return false;
    }


    public void dealCards(Player[] players, Deck deck, int dealerIndex) {
        if (desc.isDEALCARDSCLOCKWISE())
            dealerIndex = (dealerIndex + 1) % players.length; // start dealing from dealer's left
        else dealerIndex = Math.floorMod((dealerIndex - 1), players.length); // start dealing from dealers right
        while (deck.getDeckSize() > 0) {
            players[dealerIndex].getHand().getCard(deck.drawCard());

            if (desc.isDEALCARDSCLOCKWISE()) dealerIndex = (dealerIndex + 1) % players.length; //turn order is clockwise
            else dealerIndex = Math.floorMod((dealerIndex - 1), players.length); //turn order is anticlockwise

            if (desc.getTrumpPickingMode().compareTo("lastDealt") == 0 && deck.getDeckSize() == 1) {
                Card lastCard = deck.drawCard();
                System.out.println();
                System.out.println("The last card dealt is " + lastCard.toString());
                System.out.println("The Trump suit is " + lastCard.getSUIT());
                System.out.println();
                trumpSuit = lastCard.getSUIT();
                players[dealerIndex].getHand().getCard(lastCard);
            }
        }
    }

    public Card playCard(Hand playerHand) {

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
                    System.out.println("Current Hand: " + playerHand.toString());
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
                    System.out.println("Current Hand: " + playerHand.toString());
                    System.out.println();
                    Scanner scanner = new Scanner(System.in);
                    do {
                        System.out.println("Choose your card: ");
                        cardNumber = scanner.nextInt();
                    } while (cardNumber < 0 || cardNumber >= playerHand.getHandSize() || !isCardValid(playerHand, playerHand.get(cardNumber)));
                    return playerHand.giveCard(cardNumber);
            }

        }
    }

    public Card winningCard() {
        HashMap<String, Integer> suitMap = generateSuitOrder();

        CardComparator comparator = new CardComparator(suitMap);

        Card currentWinner = currentTrick.get(0);
        for (Card card : currentTrick.getHand()) {
            if (comparator.compare(card, currentWinner) > 0) currentWinner = card;
        }

        return currentWinner;
    }


    private HashMap<String, Integer> generateSuitOrder() {
        HashMap<String, Integer> suitMap = new HashMap<>();
        for (String suit : desc.getSUITS()) {
            suitMap.put(suit, 4);
        }
        switch (desc.getTrumpPickingMode()) {
            case "lastDealt":
                suitMap.put(trumpSuit, 1);
                if (!currentTrick.get(0).getSUIT().equals(trumpSuit)) suitMap.put(currentTrick.get(0).getSUIT(), 2);
                break;
            case "fixed":
                suitMap.put(desc.getTrumpSuit(), 1);
                if (!currentTrick.get(0).getSUIT().equals(desc.getTrumpSuit()))
                    suitMap.put(currentTrick.get(0).getSUIT(), 2);
                break;
            case "none":
                break;
        }
        return suitMap;
    }

    private LinkedList<Card> validCards(Hand playerHand) {
        LinkedList<Card> validCards = new LinkedList<>();
        for (Card card : playerHand.getHand()) {
            if (card.getSUIT().equals(currentTrick.getHand().get(0).getSUIT())) {
                validCards.add(card);
            }
        }
        if (validCards.size() > 0) return validCards;
        return playerHand.getHand();
    }

    private boolean isCardValid(Hand playerHand, Card card) {
        if (currentTrick.getHandSize() == 0) {
            switch (desc.getLeadingCardForEachTrick()) {
                case "any":
                    return true;
                case "trump":
                    if (card.getSUIT().equals(trumpSuit) && playerHand.getHand().contains(card)) {
                        return true;
                    }
                case "notTrump":
                    if (!card.getSUIT().equals(trumpSuit) && playerHand.getHand().contains(card)) {
                        return true;
                    }
//                case "break":
//                    TODO
            }
            return false;
        }
        if (validCards(playerHand).indexOf(card) != -1) {
            return true;
        } else return false;
    }

    private void printScore() {
        System.out.println("CURRENT SCORETABLE");
        System.out.println("______________________________________________________________________________________");
        System.out.println("‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾");
        for (int i = 0; i < desc.getTeams().length; i++) {
            System.out.print("    Team: (");
            for (int j = 0; j < desc.getTeams()[0].length; j++) {
                System.out.print(desc.getTeams()[i][j]);
                if ((j + 1) < desc.getTeams()[0].length) System.out.print(", ");
            }
            System.out.print(")     ");
            System.out.println(scoreTable.get(desc.getTeams()[i]));
            if ((i + 1) < desc.getTeams().length) {
                System.out.println("-------------------------------------------------------------------------------------");
            }
        }
        System.out.println("______________________________________________________________________________________");
        System.out.println("‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾");
    }

    public Predicate<Card> getValidCard() {
        return validCard;
    }
}
