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

/**
 * Main class that runs the game based of on a provided game description.
 */
public class GameEngine {

    private GameDesc desc;
    private StringBuilder trumpSuit;
    private Hand currentTrick = new Hand(); //functionally the trick is just a hand visible to the entire table
    private AtomicBoolean breakFlag; // if the trump/hearts are broken
    //Stores the scores/metrics of the game
    private int handsPlayed = 0;
    HashMap<int[], Integer> tricksWonTable;
    private Bid[] bidTable;
    HashMap<int[], Integer> scoreTable;

    //Predicate functions used in determining if card moves are valid
    private Predicate<Card> validCard;
    private Predicate<Card> validLeadingCard;


    /**
     * Set up game engine
     *
     * @param desc game description
     */
    public GameEngine(GameDesc desc) {
        this.desc = desc;
        this.trumpSuit = new StringBuilder();
        //Set fixed trump suit if specified
        if (desc.getTrumpPickingMode().equals("fixed")) {
            this.trumpSuit.append(desc.getTrumpSuit());
        }
        if (desc.getTrumpPickingMode().equals("predefined")) {
            this.trumpSuit.append(desc.getTrumpIterator().next());
        }
        //Flags if the trump suit has been broken in the hand
        this.breakFlag = new AtomicBoolean(false);
        this.validLeadingCard = validCards.getValidLeadingCardPredicate(desc.getLeadingCardForEachTrick(), this.trumpSuit, breakFlag);
        this.validCard = validCards.getValidCardPredicate("trick", this.trumpSuit, this.currentTrick, this.validLeadingCard);
        if (desc.isBidding()) {
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
        Deck deck; // make standard deck from a linked list of Cards
        Shuffle.seedGenerator(seed); // TODO remove cast to int
        game.printScore();
        //Loop until game winning condition has been met
        do {
            int currentPlayer = dealer;
            deck = new Deck(gameDesc.getDECK());
            Shuffle.shuffle(deck.cards); //shuffle deck according to the given seed
            game.dealCards(playerArray, deck, currentPlayer);

            if (gameDesc.isDEALCARDSCLOCKWISE())
                currentPlayer = (currentPlayer + 1) % playerArray.length; //ensures that first card played is from dealer's left
            else
                currentPlayer = Math.floorMod((currentPlayer - 1), playerArray.length); //ensures that first card played is from dealers right

            if (gameDesc.isBidding()) {
                game.getBids(currentPlayer, playerArray);
            }
            System.out.println("-----------------------------------");
            System.out.println("----------------PLAY---------------");
            System.out.println("-----------------------------------");
            //Loop until trick has completed (all cards have been played)
            do {
                System.out.println("Trump is " + game.trumpSuit.toString());
                //Each player plays a card
                for (int i = 0; i < playerArray.length; i++) {
                    game.currentTrick.getCard(playerArray[currentPlayer].playCard(game.trumpSuit.toString(), game.currentTrick));
                    game.broadcastMoves(game.currentTrick.get(i), currentPlayer, playerArray);
                    if (gameDesc.isDEALCARDSCLOCKWISE()) currentPlayer = (currentPlayer + 1) % playerArray.length;
                    else currentPlayer = Math.floorMod((currentPlayer - 1), playerArray.length);
                }
                //Determine winning card
                Card winningCard = game.winningCard();

                //Works out who played the winning card
                //Roll back player to the person who last played a card.
                if (gameDesc.isDEALCARDSCLOCKWISE()) {
                    currentPlayer = Math.floorMod((currentPlayer - 1), playerArray.length);
                } else {
                    currentPlayer = (currentPlayer + 1) % playerArray.length;
                }

                //Find player who played winning card
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

                //Find the team with the winning player and increment their tricks score
                for (int[] team : gameDesc.getTeams()) {
                    if (ArrayUtils.contains(team, currentPlayer)) {
                        game.tricksWonTable.put(team, (game.tricksWonTable.get(team) + 1));
                        System.out.println("Player " + (currentPlayer + 1) + " was the winner of the trick with the " + winningCard.toString());
                        System.out.println("Tricks won: " + game.tricksWonTable.get(team));
                        break;
                    }
                    //Signal that trump suit was broken -> can now be played
                    if (game.currentTrick.getHand().stream().anyMatch(card -> card.getSUIT().equals(game.trumpSuit.toString()))) {
                        game.breakFlag.set(true);
                    }
                }
                //Reset trick hand
                game.currentTrick.dropHand();
            } while (playerArray[0].getHand().getHandSize() > gameDesc.getMinHandSize());

            game.handsPlayed++;
            //Calculate the score of the hand
            if (gameDesc.getCalculateScore().equals("tricksWon")) {
                for (int[] team : gameDesc.getTeams()) {
                    int score = game.tricksWonTable.get(team);
                    if (score > gameDesc.getTrickThreshold()) { // if score greater than trick threshold
                        game.scoreTable.put(team, (game.scoreTable.get(team) + (score - gameDesc.getTrickThreshold()))); // add score to team's running total
                    }
                    game.tricksWonTable.put(team, 0);
                }
            }
            //
            if (gameDesc.getCalculateScore().equals("bid")) { //TODO handle special bids.
                for (int[] team : gameDesc.getTeams()) {
                    int teamBid = 0;
                    //Get collective team bids
                    for (int playerNumber : team) {
                        teamBid += game.bidTable[playerNumber].getBidValue();
                    }
                    Bid bid = new Bid(teamBid, false);
                    //Increase score of winning team based on bid scoring system (See validBids.java)
                    game.scoreTable.put(team, game.scoreTable.get(team) + gameDesc.getEvaluateBid().apply(bid, game.tricksWonTable.get(team)));
                    //Reset tricks won for next round.
                    game.tricksWonTable.put(team, 0);
                }
            }
            if (gameDesc.getTrumpPickingMode().equals("predefined")) {
                game.trumpSuit.replace(0, game.trumpSuit.length(), gameDesc.getTrumpIterator().next());
            }
            game.printScore();
        } while (game.gameEnd());

    }


    /**
     * @return flag to signal game should end based on game description
     */
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

    /**
     * Gets the bids from the players
     *
     * @param currentPlayer
     * @param players
     */
    public void getBids(int currentPlayer, Player[] players) {
        System.out.println("-----------------------------------");
        System.out.println("--------------BIDDING--------------");
        System.out.println("-----------------------------------");
        for (int i = 0; i < players.length; i++) {
            //Adds the bids (checks they are valid in other class)
            bidTable[currentPlayer] = players[currentPlayer].makeBid(this.desc.getValidBid());
            broadcastBids(bidTable[currentPlayer], currentPlayer, players);
            if (this.desc.isDEALCARDSCLOCKWISE()) currentPlayer = (currentPlayer + 1) % players.length;
            else currentPlayer = Math.floorMod((currentPlayer - 1), players.length);
        }
    }


    /**
     * Distributes cards from the deck starting from the dealer +/- 1
     *
     * @param players
     * @param deck
     * @param dealerIndex
     */
    public void dealCards(Player[] players, Deck deck, int dealerIndex) {
        if (desc.isDEALCARDSCLOCKWISE())
            dealerIndex = (dealerIndex + 1) % players.length; // start dealing from dealer's left
        else dealerIndex = Math.floorMod((dealerIndex - 1), players.length); // start dealing from dealers right
        int cardsLeft = deck.getDeckSize() - (players.length * this.desc.getHandSize());
        //Deal until the deck is empty
        while (deck.getDeckSize() > cardsLeft) {
            //Deal card to player by adding to their hand and removing from the deck
            players[dealerIndex].getHand().getCard(deck.drawCard());

            if (desc.isDEALCARDSCLOCKWISE()) dealerIndex = (dealerIndex + 1) % players.length; //turn order is clockwise
            else dealerIndex = Math.floorMod((dealerIndex - 1), players.length); //turn order is anticlockwise

            //Sets the trump suit based on the last card if defined by game desc
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

    /**
     * Finds the winning card of a trick
     *
     * @return winning card
     */
    public Card winningCard() {
        //Generate suit ranking
        HashMap<String, Integer> suitMap = generateSuitOrder();
        //Get comparator for comparing cards based on the suit ranking
        CardComparator comparator = new CardComparator(suitMap);

        //Find the card with the highest ranking/value
        Card currentWinner = currentTrick.get(0);
        for (Card card : currentTrick.getHand()) {
            if (comparator.compare(card, currentWinner) > 0) currentWinner = card;
        }
        return currentWinner;
    }


    /**
     * @return suit-value hashmap where the value is its rank based on how the game ranks suits
     * Note: lower map value = higher rank
     */
    private HashMap<String, Integer> generateSuitOrder() {
        HashMap<String, Integer> suitMap = new HashMap<>();
        //Set default value for suits
        for (String suit : desc.getSUITS()) {
            suitMap.put(suit, 4);
        }
        //Refine ranking based on how the game chooses the trump
        switch (desc.getTrumpPickingMode()) {
            case "lastDealt": //follows through to 'fixed' case
            case "fixed":
                suitMap.put(trumpSuit.toString(), 1);
                if (!currentTrick.get(0).getSUIT().equals(trumpSuit.toString()))
                    suitMap.put(currentTrick.get(0).getSUIT(), 2);
                break;
            case "none":
                break;
        }
        return suitMap;
    }


    /**
     * Prints the current score of the game
     */
    private void printScore() {
        System.out.println("CURRENT SCORETABLE");
        System.out.println("______________________________________________________________________________________");
        System.out.println("‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾");
        //Iterate teams
        for (int i = 0; i < desc.getTeams().length; i++) {
            System.out.print("    Team: (");
            //Print team names
            for (int j = 0; j < desc.getTeams()[0].length; j++) {
                System.out.print(desc.getTeams()[i][j]);
                if ((j + 1) < desc.getTeams()[0].length) System.out.print(", ");
            }
            //Print score of team
            System.out.print(")     ");
            System.out.println(scoreTable.get(desc.getTeams()[i]));
            if ((i + 1) < desc.getTeams().length) {
                System.out.println("-------------------------------------------------------------------------------------");
            }
        }
        System.out.println("______________________________________________________________________________________");
        System.out.println("‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾");
    }

    private void broadcastBids(Bid bid, int playerNumber, Player[] playerArray) {
        //Only need to broadcast moves from local players to network players
        if (playerArray[playerNumber].getClass() == LocalPlayer.class) {
            for (Player player : playerArray) {
                player.broadcastBid(bid, playerNumber);
            }
        } else { //Only need to print out network moves to local players
            for (Player player : playerArray) {
                if (player.getClass() == LocalPlayer.class) {
                    player.broadcastBid(bid, playerNumber);
                }
            }
        }
        //Resets the printed for local players.
//        LocalPlayer.resetLocalPrinted();
    }

    private void broadcastMoves(Card card, int playerNumber, Player[] playerArray) {
        //Only need to broadcast moves from local players to network players
        if (playerArray[playerNumber].getClass() == LocalPlayer.class) {
            for (Player player : playerArray) {
                player.broadcastPlay(card, playerNumber);
            }
        } else { //Only need to print out network moves to local players
            for (Player player : playerArray) {
                if (player.getClass() == LocalPlayer.class) {
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
