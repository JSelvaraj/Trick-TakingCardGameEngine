package src.gameEngine;

import src.card.Card;
import src.card.CardComparator;
import src.deck.Deck;
import src.deck.Shuffle;
import src.deck.Trick;
import src.functions.PlayerIncrementer;
import src.functions.validCards;
import src.parser.GameDesc;
import src.player.LocalPlayer;
import src.player.NetworkPlayer;
import src.player.Player;
import src.rdmEvents.RdmEvent;
import src.rdmEvents.RdmEventsManager;
import src.team.Team;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
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
    private Bid[] bidTable;
    private List<Trick> trickHistory;
    //Predicate functions used in determining if card moves are valid
    private Predicate<Card> validCard;
    private Predicate<Card> validLeadingCard;
    private IntFunction<Integer> nextPlayerIndex;

    private ArrayList<Team> teams = new ArrayList<>();

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
        this.nextPlayerIndex = PlayerIncrementer.generateNextPlayerFunction(desc.isDEALCARDSCLOCKWISE(), desc.getNUMBEROFPLAYERS());
        if (desc.isBidding()) {
            bidTable = new Bid[this.desc.getNUMBEROFPLAYERS()];
        }
        this.trickHistory = new LinkedList<>();
    }

    public static void main(GameDesc gameDesc, int dealer, Player[] playerArray, int seed, boolean printMoves, boolean enableRandomEvents) {
        GameEngine game = new GameEngine(gameDesc);
        Random rand = new Random(seed);

        assert playerArray.length == gameDesc.getNUMBEROFPLAYERS(); //TODO remove

        /* initialize each players hands */

        for (Player player : playerArray) {
            player.initCanBePlayed(game.getValidCard());
        }

        /* Assign players to teams */
        int teamCounter = 0;
        for (int[] team : gameDesc.getTeams()) {
            Player[] players = new Player[team.length];
            for (int i = 0; i < team.length; i++) {
                players[i] = playerArray[team[i]];
            }
            game.getTeams().add(new Team(players, teamCounter));
            teamCounter++;
        }

        /* Initialise random events */
        RdmEventsManager rdmEventsManager = new RdmEventsManager(2, gameDesc.getScoreThreshold(),
                1, 3, game.getTeams().get(0), game.getTeams().get(1), enableRandomEvents, rand);

        Deck deck; // make standard deck from a linked list of Cards
        Shuffle shuffle = new Shuffle(seed);
        //Shuffle.seedGenerator(seed); // TODO remove cast to int
        if (printMoves) {
            game.printScore();
        }

        //Loop until game winning condition has been met
        do {
            RdmEvent rdmEventHAND = rdmEventsManager.eventChooser("HAND");

            int currentPlayer = dealer;
            deck = new Deck(gameDesc.getDECK());

            if (rdmEventHAND != null && (rdmEventHAND.getName().equals("BOMB") || rdmEventHAND.getName().equals("HEAVEN"))) {
                System.out.println("Adding special card type " + rdmEventHAND.getName() + " to deck");
                int rdmIndex = rand.nextInt(deck.getDeckSize());
                deck.cards.get(rdmIndex).setSpecialType(rdmEventHAND.getName());
                System.out.println(deck.cards.get(rdmIndex));
            }

            shuffle.shuffle(deck.cards); //shuffle deck according to the given seed
            game.dealCards(playerArray, deck, currentPlayer);

            currentPlayer = game.nextPlayerIndex.apply(currentPlayer);

            if (gameDesc.isBidding()) {
                game.getBids(currentPlayer, playerArray);
            }
            if (printMoves) {
                System.out.println("-----------------------------------");
                System.out.println("----------------PLAY---------------");
                System.out.println("-----------------------------------");
            }
            //Loop until trick has completed (all cards have been played)
            do {
                //Check for random event probability
                RdmEvent rdmEventTRICK = null;//rdmEventsManager.eventChooser("TRICK");
                if (rdmEventTRICK != null){
                    System.out.println("Random event type TRICK triggered");
                    game.runRdmEvent(rdmEventTRICK);
                }
                if (printMoves) {
                    System.out.println("Trump is " + game.trumpSuit.toString());
                }
                //Each player plays a card
                for (int i = 0; i < playerArray.length; i++) {
                    RdmEvent rdmEventMIDTRICK = null;//rdmEventsManager.eventChooser("MID-TRICK");
                    if (rdmEventMIDTRICK != null){
                        System.out.println("Random event type MID-TRICK triggered");
                        game.runRdmEvent(rdmEventMIDTRICK);
                    }
                    game.currentTrick.getCard(playerArray[currentPlayer].playCard(game.trumpSuit.toString(), game.currentTrick));
                    game.broadcastMoves(game.currentTrick.get(i), currentPlayer, playerArray);
                    String playedCardType =  game.currentTrick.getHand().get(game.currentTrick.getHandSize()-1).getSpecialType();
                    game.runSpecialCardOps(playedCardType, currentPlayer, game.getTeams());
                    currentPlayer = game.nextPlayerIndex.apply(currentPlayer);
                }
                //Determine winning card
                Card winningCard = game.winningCard();
                //Works out who played the winning card
                /* go back to the previous player.
                 Loop 1 less than the number of players, so you actually move one back.
                 If you wanted to go from 1 -> 0, then this is the same as 1 -> 2 -> 3 -> 0
                 */
                for (int j = 0; j < playerArray.length - 1; j++) {
                    currentPlayer = game.nextPlayerIndex.apply(currentPlayer);
                }

                //Find player who played winning card
                for (int i = playerArray.length - 1; i >= 0; i--) {
                    if (game.currentTrick.get(i).equals(winningCard)) {
                        break;
                    } else {
                        // go back to the previous player.
                        for (int j = 0; j < playerArray.length - 1; j++) {
                            currentPlayer = game.nextPlayerIndex.apply(currentPlayer);
                        }
                    }
                }
                //Adds the trick to the trick history.
                Trick trick = new Trick(winningCard, game.trumpSuit.toString(), currentPlayer, new LinkedList<>(game.currentTrick.getHand()));
                game.trickHistory.add(trick);
                //Find the team with the winning player and increment their tricks score
                for (Team team : game.getTeams()) {
                    if (team.findPlayer(currentPlayer)) {
                        team.setTricksWon(team.getTricksWon() + 1);
                        if (printMoves) {
                            System.out.println("Player " + (currentPlayer + 1) + " was the winner of the trick with the " + winningCard.toString());
                            System.out.println("Tricks won: " + team.getTricksWon());
                        }
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
                for (Team team : game.getTeams()) {
                    int score = team.getTricksWon();
                    if (score > gameDesc.getTrickThreshold()) { // if score greater than trick threshold
                        team.setScore(team.getScore() + (score - gameDesc.getTrickThreshold())); // add score to team's running total
                    }
                    team.setTricksWon(0);
                }
            }
            //
            if (gameDesc.getCalculateScore().equals("bid")) { //TODO handle special bids.
                for (Team team : game.getTeams()) {
                    int teamBid = 0;
                    //Get collective team bids
                    for (Player player : team.getPlayers()) {
                        teamBid += game.bidTable[player.getPlayerNumber()].getBidValue();
                    }
                    Bid bid = new Bid(teamBid, false);
                    //Increase score of winning team based on bid scoring system (See validBids.java)
                    team.setScore(team.getScore() + gameDesc.getEvaluateBid().apply(bid, team.getTricksWon()));
                    //Reset tricks won for next round.
                    team.setTricksWon(0);
                }
            }
            if (gameDesc.getTrumpPickingMode().equals("predefined")) {
                game.trumpSuit.replace(0, game.trumpSuit.length(), gameDesc.getTrumpIterator().next());
            }

            //Check if game needs balancing
            rdmEventsManager.checkGameCloseness(game.getTeams());

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
                    System.out.println(team.getScore());
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
            currentPlayer = this.nextPlayerIndex.apply(currentPlayer);
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
        dealerIndex = this.nextPlayerIndex.apply(dealerIndex);
        int cardsLeft = deck.getDeckSize() - (players.length * this.desc.getHandSize());
        //Deal until the deck is empty
        while (deck.getDeckSize() > cardsLeft) {
            //Deal card to player by adding to their hand and removing from the deck
            players[dealerIndex].getHand().getCard(deck.drawCard());
            
            dealerIndex = this.nextPlayerIndex.apply(dealerIndex);
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

    private void broadcastBids(Bid bid, int playerNumber, Player[] playerArray) {
        //Only need to broadcast moves from local players to network players
        if (!(playerArray[playerNumber] instanceof NetworkPlayer)) {
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
    }

    private void broadcastMoves(Card card, int playerNumber, Player[] playerArray) {
        //Only need to broadcast moves from local players to network players
        if (!(playerArray[playerNumber] instanceof NetworkPlayer)) {
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
    }

    private void runRdmEvent(RdmEvent rdmEvent) {
        switch (rdmEvent.getName()) {
            case "SwapHands":
                swapHands(rdmEvent.getWeakestTeam(), rdmEvent.getStrongestTeam());
                System.out.println("Swap hand triggered");
        }
    }

    private void runSpecialCardOps(String cardType, int currentPlayer, ArrayList<Team> teams) {
        if (cardType != null) {
            for (Team team : teams) {
                if (team.findPlayer(currentPlayer)) {
                    int scoreChange = 10;
                    if (cardType.equals("BOMB")) {
                        scoreChange *= (-1);
                        System.out.println("You played a BOMB card - " + scoreChange + " deducted from your score");
                    }
                    else {
                        System.out.println("You played a HEAVEN card - " + scoreChange + " added to your score");
                    }
                    System.out.println("Changing score of team " + 0);
                    team.setScore(Math.max((team.getScore() + scoreChange), 0));
                    break;
                }
            }
        }
    }

    private void swapHands(Team weakestTeam, Team strongestTeam) {
        Player weakPlayer = weakestTeam.getPlayers()[0];
        Player strongPlayer = strongestTeam.getPlayers()[0];

        Hand tempHand = weakPlayer.getHand();
        Predicate<Card> tempPredicate = weakPlayer.getCanBePlayed();

        weakPlayer.setHand(strongPlayer.getHand());
        weakPlayer.setCanBePlayed(strongPlayer.getCanBePlayed());
        strongPlayer.setHand(tempHand);
        strongPlayer.setCanBePlayed(tempPredicate);
    }

    private Predicate<Card> getValidCard() {
        return validCard;
    }

    public Bid[] getBidTable() {
        return bidTable;
    }

    public ArrayList<Team> getTeams() {
        return teams;
    }
}
