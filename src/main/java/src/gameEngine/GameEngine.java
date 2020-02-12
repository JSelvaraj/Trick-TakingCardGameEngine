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
import src.team.Team;

import java.util.*;
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

    static ArrayList<Team> teams = new ArrayList<>();


    /**
     * Set up game engine
     * @param desc game description
     */
    public GameEngine(GameDesc desc) {
        this.desc = desc;
        this.trumpSuit = new StringBuilder();
        //Set fixed trump suit if specified
        if(desc.getTrumpPickingMode().equals("fixed")){
            this.trumpSuit.append(desc.getTrumpSuit());
        }
        if(desc.getTrumpPickingMode().equals("predefined")){
            this.trumpSuit.append(desc.getTrumpIterator().next());
        }
        //Flags if the trump suit has been broken in the hand
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

        int teamCounter = 0;
        for (int[] team : gameDesc.getTeams()) {
            Player[] players = new Player[team.length];
            for (int i = 0; i < team.length; i++) {
                players[i] = playerArray[team[i]];
            }
            teams.add(new Team(players, teamCounter));
            teamCounter++;
        }

        for (Team team: teams) {
            team.printTeam();
        }

        for (int[] team : gameDesc.getTeams()) {
            game.tricksWonTable.put(team, 0);
            game.scoreTable.put(team, 0);
        }
        Deck deck; // make standard deck from a linked list of Cards
        Shuffle.seedGenerator(seed); // TODO remove cast to int
        game.printScore();

        //game.checkGameCloseness();
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

            if(gameDesc.isBidding()){
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
                for (Team team : teams) {
                    if (team.findPlayer(currentPlayer)) {
                        team.setTricksWon(team.getTricksWon() + 1);
                        System.out.println("Player " + (currentPlayer + 1) + " was the winner of the trick with the " + winningCard.toString());
                        System.out.println("Tricks won: " + team.getTricksWon());
                        break;
                    }
                    //Signal that trump suit was broken -> can now be played
                    if(game.currentTrick.getHand().stream().anyMatch(card -> card.getSUIT().equals(game.trumpSuit.toString()))){
                        game.breakFlag.set(true);
                    }
                }
                //Reset trick hand
                game.currentTrick.dropHand();
            } while (playerArray[0].getHand().getHandSize() > gameDesc.getMinHandSize());

            game.handsPlayed++;
            //Calculate the score of the hand
            if (gameDesc.getCalculateScore().equals("tricksWon")) {
                for (Team team: teams) {
                    int score = team.getScore();
                    if (score > gameDesc.getTrickThreshold()) { // if score greater than trick threshold
                        team.setScore(team.getScore() + (score - gameDesc.getTrickThreshold())); // add score to team's running total
                    }
                    team.setTricksWon(0);
                }
            }
            //
            if(gameDesc.getCalculateScore().equals("bid")) { //TODO handle special bids.
                for (Team team : teams){
                    int teamBid = 0;
                    //Get collective team bids
                    for (Player player : team.getPlayers()){
                        teamBid += game.bidTable[player.getPlayerNumber()].getBidValue();
                    }
                    Bid bid = new Bid(teamBid, false);
                    //Increase score of winning team based on bid scoring system (See validBids.java)
                    team.setScore(team.getScore() + gameDesc.getEvaluateBid().apply(bid, team.getTricksWon()));
                    //Reset tricks won for next round.
                    team.setTricksWon(0);
                }
            }
            if(gameDesc.getTrumpPickingMode().equals("predefined")){
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
                for (Team team : teams) {
                    if (team.getScore() >= desc.getScoreThreshold())
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
     * @param currentPlayer
     * @param players
     */
    public void getBids(int currentPlayer, Player[] players){
        System.out.println("-----------------------------------");
        System.out.println("--------------BIDDING--------------");
        System.out.println("-----------------------------------");
        for (int i = 0; i < players.length; i++){
            //Adds the bids (checks they are valid in other class)
            bidTable[currentPlayer] = players[currentPlayer].makeBid(this.desc.getValidBid());
            broadcastBids(bidTable[currentPlayer], currentPlayer, players);
            if (this.desc.isDEALCARDSCLOCKWISE()) currentPlayer = (currentPlayer + 1) % players.length;
            else currentPlayer = Math.floorMod((currentPlayer - 1), players.length);
        }
    }


    /**
     * Distributes cards from the deck starting from the dealer +/- 1
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
                if (!currentTrick.get(0).getSUIT().equals(trumpSuit.toString())) suitMap.put(currentTrick.get(0).getSUIT(), 2);
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
        for (int i = 0; i < teams.size(); i++) {
            System.out.print("    Team: (");
            //Print team names
            for (int j = 0; j < teams.get(i).getPlayers().length; j++) {
                System.out.print(teams.get(i).getPlayers()[j].getPlayerNumber());
                if ((j + 1) < teams.get(i).getPlayers().length) System.out.print(", ");
            }
            //Print score of team
            System.out.print(")     ");
            System.out.println(teams.get(i).getScore());
            if ((i + 1) < teams.size()) {
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

    private void checkGameCloseness() {
        int maxAcceptableScoreSeparation = 10;
        int[] emptyTeam = new int[]{0,1};
        int[] weakestTeam = null;
        int[] strongestTeam = null;
        HashMap<int[], Integer> testScoreTable = new HashMap<>();
        testScoreTable.put(new int[]{0,1}, 2);
        testScoreTable.put(new int[]{2,3}, 3);
        testScoreTable.put(new int[]{4,5}, 1);

        Map.Entry<int[], Integer> maxEntry = null;
        for (Map.Entry<int[], Integer> entry : testScoreTable.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) >= 0)
            {
                maxEntry = entry;
            }
        }
        Map.Entry<int[], Integer> minEntry = null;
        for (Map.Entry<int[], Integer> entry : testScoreTable.entrySet())
        {
            if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) >= 0)
            {
                minEntry = entry;
            }
        }
        int highestScore = maxEntry.getValue();
        int lowestScore = minEntry.getValue();


        if (highestScore - lowestScore > maxAcceptableScoreSeparation) {
            //trigger balancing event
        }

        System.exit(0);
    }

    private Predicate<Card> getValidCard() {
        return validCard;
    }

    public Bid[] getBidTable() {
        return bidTable;
    }
}
