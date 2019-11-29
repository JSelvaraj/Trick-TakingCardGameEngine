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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class GameEngine {


    private GameDesc desc;
    private StringBuilder trumpSuit;
    private Hand currentTrick = new Hand(); //functionally the trick is just a hand visible to the entire table
    private AtomicBoolean breakFlag; // if the trump/hearts are broken
    private int handsPlayed = 0;
    HashMap<int[], Integer> tricksWonTable;
    private Bid[] bidTable;
    HashMap<int[], Integer> scoreTable;

    private Predicate<Card> validCard;
    private Predicate<Card> validLeadingCard;

    public GameEngine(GameDesc desc) {
        this.desc = desc;
        this.trumpSuit = new StringBuilder();
        if(desc.getTrumpPickingMode().equals("fixed")){
            this.trumpSuit.append(desc.getTrumpSuit());
        }
        this.breakFlag = new AtomicBoolean(false);
        this.validLeadingCard = validCards.getValidLeadingCardPredicate(desc.getLeadingCardForEachTrick(), this.trumpSuit, breakFlag);
        this.validCard = validCards.getValidCardPredicate("trick", this.trumpSuit, this.currentTrick, this.validLeadingCard);
        if(desc.isBidding()){
            bidTable = new Bid[this.desc.getNUMBEROFPLAYERS()];
        }
    }


    public static void main(GameDesc gameDesc, int dealer, Player[] playerArray, int seed) {
        GameEngine game = new GameEngine(gameDesc);

        assert playerArray.length == gameDesc.getNUMBEROFPLAYERS(); //TODO remove

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
        Shuffle.seedGenerator(seed); // TODO remove cast to int
        game.printScore();
        do {
            int currentPlayer = dealer;
            Shuffle.shuffle(deck.cards); //shuffle deck according to the given seed
            game.dealCards(playerArray, deck, currentPlayer);
            if (gameDesc.isDEALCARDSCLOCKWISE())
                currentPlayer = (currentPlayer + 1) % playerArray.length; //ensures that first card played is from dealer's left
            else
                currentPlayer = Math.floorMod((currentPlayer - 1), playerArray.length); //ensures that first card played is from dealers right
            if(gameDesc.isBidding()){
                game.getBids(currentPlayer, playerArray);
            }
            System.out.println("-----------------------------------");
            System.out.println("----------------PLAY---------------");
            System.out.println("-----------------------------------");
            do {
                for (int i = 0; i < playerArray.length; i++) {
                    game.currentTrick.getCard(playerArray[currentPlayer].playCard(game.trumpSuit.toString(), game.currentTrick));
                    game.broadcastMoves(game.currentTrick.get(i), currentPlayer, playerArray);
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
                    //Check
                    if(game.currentTrick.getHand().stream().anyMatch(card -> card.getSUIT().equals(game.trumpSuit.toString()))){
                        game.breakFlag.set(true);
                    }
                }
                game.currentTrick.dropHand();
            } while (playerArray[0].getHand().getHandSize() > gameDesc.getMinHandSize());
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
            if(gameDesc.getCalculateScore().equals("bid")) { //TODO handle special bids.
                for (int[] team : gameDesc.getTeams()){
                    int teamBid = 0;
                    for (int playerNumber : team){
                        teamBid += game.bidTable[playerNumber].getBidValue();
                    }
                    Bid bid = new Bid(teamBid, false);
                    game.scoreTable.put(team, game.scoreTable.get(team) + gameDesc.getEvaluateBid().apply(bid, game.tricksWonTable.get(team)));
                    //Reset tricks won for next round.
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
                        return false;
                }
                break;
            case "handsPlayed":
                if (handsPlayed >= desc.getScoreThreshold()) {
                    return false;
                }
                break;
        }
        return true;
    }

    public void getBids(int currentPlayer, Player[] players){
        System.out.println("-----------------------------------");
        System.out.println("--------------BIDDING--------------");
        System.out.println("-----------------------------------");
        for (int i = 0; i < players.length; i++){
            bidTable[currentPlayer] = players[currentPlayer].makeBid(this.desc.getValidBid());
            broadcastBids(bidTable[currentPlayer], currentPlayer, players);
            if (this.desc.isDEALCARDSCLOCKWISE()) currentPlayer = (currentPlayer + 1) % players.length;
            else currentPlayer = Math.floorMod((currentPlayer - 1), players.length);
        }
    }


    public void dealCards(Player[] players, Deck deck, int dealerIndex) {
        if (desc.isDEALCARDSCLOCKWISE())
            dealerIndex = (dealerIndex + 1) % players.length; // start dealing from dealer's left
        else dealerIndex = Math.floorMod((dealerIndex - 1), players.length); // start dealing from dealers right
        int cardsLeft = deck.getDeckSize() - (players.length * this.desc.getInitialHandSize());
        while (deck.getDeckSize() > cardsLeft) {
            players[dealerIndex].getHand().getCard(deck.drawCard());

            if (desc.isDEALCARDSCLOCKWISE()) dealerIndex = (dealerIndex + 1) % players.length; //turn order is clockwise
            else dealerIndex = Math.floorMod((dealerIndex - 1), players.length); //turn order is anticlockwise

            if (desc.getTrumpPickingMode().compareTo("lastDealt") == 0 && deck.getDeckSize() == cardsLeft + 1) {
                Card lastCard = deck.drawCard();
                System.out.println();
                System.out.println("The last card dealt is " + lastCard.toString());
                System.out.println("The Trump suit is " + lastCard.getSUIT());
                System.out.println();
                trumpSuit.replace(0, trumpSuit.length(), lastCard.getSUIT());
                players[dealerIndex].getHand().getCard(lastCard);
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
            case "fixed":
                suitMap.put(trumpSuit.toString(), 1);
                if (!currentTrick.get(0).getSUIT().equals(trumpSuit.toString())) suitMap.put(currentTrick.get(0).getSUIT(), 2);
                break;
            case "none":
                break;
        }
        return suitMap;
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

    private void broadcastBids(Bid bid, int playerNumber, Player[] playerArray){
        //Only need to broadcast moves from local players to network players
        if(playerArray[playerNumber].getClass() == LocalPlayer.class){
            for (Player player : playerArray) {
                player.broadcastBid(bid, playerNumber);
            }
        } else { //Only need to print out network moves to local players
            for(Player player : playerArray){
                if(player.getClass() == LocalPlayer.class){
                    player.broadcastBid(bid, playerNumber);
                }
            }
        }
        //Resets the printed for local players.
//        LocalPlayer.resetLocalPrinted();
    }

    private void broadcastMoves(Card card, int playerNumber, Player[] playerArray){
        //Only need to broadcast moves from local players to network players
        if(playerArray[playerNumber].getClass() == LocalPlayer.class){
            for (Player player : playerArray) {
                player.broadcastPlay(card, playerNumber);
            }
        } else { //Only need to print out network moves to local players
            for(Player player : playerArray){
                if(player.getClass() == LocalPlayer.class){
                    player.broadcastPlay(card, playerNumber);
                }
            }
        }
        //Resets the printed for local players.
//        LocalPlayer.resetLocalPrinted();
    }

    private Predicate<Card> getValidCard() {
        return validCard;
    }

    public Bid[] getBidTable() {
        return bidTable;
    }
}
