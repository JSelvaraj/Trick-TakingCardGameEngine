package src.gameEngine;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
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
import src.rdmEvents.rdmEvent;
import src.rdmEvents.rdmEventsManager;
import src.team.Team;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/**
 * Main class that runs the game based of on a provided game description.
 */
public class GameEngine extends WebSocketServer {

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

    static ArrayList<Team> teams = new ArrayList<>();
    //Starts as 1 in 10 chance;
    double rdmEventProb = 10;
    WebSocket webSocket;

    /**
     * Set up game engine
     *
     * @param desc game description
     */
    public GameEngine(GameDesc desc) {
        this(desc, new InetSocketAddress("localhost", 8081));
    }

    public GameEngine (GameDesc desc, InetSocketAddress address) {
        super(address);
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


    public static void main(GameDesc gameDesc, int dealer, Player[] playerArray, int seed, boolean printMoves ) {
        GameEngine game = new GameEngine(gameDesc);

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
            teams.add(new Team(players, teamCounter));
            teamCounter++;
        }

        /* Initialise random events */
        rdmEventsManager rdmEventsManager = new rdmEventsManager(2, gameDesc.getScoreThreshold(),
                1, 3, teams.get(0), teams.get(1));

        Deck deck; // make standard deck from a linked list of Cards
        Shuffle shuffle = new Shuffle(seed);
        //Shuffle.seedGenerator(seed); // TODO remove cast to int
        if (printMoves) {
            game.printScore();
        }

        //Loop until game winning condition has been met
        do {
            int currentPlayer = dealer;
            deck = new Deck(gameDesc.getDECK());
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
                boolean rdmEventHappenedTRICK = false;

                if (printMoves) {
                    System.out.println("Trump is " + game.trumpSuit.toString());
                }
                //Each player plays a card
                for (int i = 0; i < playerArray.length; i++) {
                    if (!rdmEventHappenedTRICK) {
                        rdmEvent rdmEvent = rdmEventsManager.eventChooser("TRICK");
                        if (rdmEvent != null){
                            //Do rdmevent
                            System.out.println("Random event creation start");
                            game.runRdmEvent(rdmEvent);
                            rdmEventHappenedTRICK = true;
                        }
                    }
                    game.currentTrick.getCard(playerArray[currentPlayer].playCard(game.trumpSuit.toString(), game.currentTrick));
                    game.broadcastMoves(game.currentTrick.get(i), currentPlayer, playerArray);
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
                for (Team team : teams) {
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
                for (Team team : teams) {
                    int score = team.getTricksWon();
                    if (score > gameDesc.getTrickThreshold()) { // if score greater than trick threshold
                        team.setScore(team.getScore() + (score - gameDesc.getTrickThreshold())); // add score to team's running total
                    }
                    team.setTricksWon(0);
                }
            }
            //
            if (gameDesc.getCalculateScore().equals("bid")) { //TODO handle special bids.
                for (Team team : teams) {
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
            rdmEventsManager.checkGameCloseness(teams);

            game.printScore();
        } while (game.gameEnd());

    }

    public static void main(GameDesc gameDesc, int dealer, Player[] playerArray, int seed, boolean printMoves, WebSocket webSocket ) {
        GameEngine game = new GameEngine(gameDesc);

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
            teams.add(new Team(players, teamCounter));
            teamCounter++;
        }

        /* Initialise random events */
        rdmEventsManager rdmEventsManager = new rdmEventsManager(2, gameDesc.getScoreThreshold(),
                1, 3, teams.get(0), teams.get(1));

        Deck deck; // make standard deck from a linked list of Cards
        Shuffle shuffle = new Shuffle(seed);
        //Shuffle.seedGenerator(seed); // TODO remove cast to int
        if (printMoves) {
            game.printScore();
        }

        //Loop until game winning condition has been met
        do {
            int currentPlayer = dealer;
            deck = new Deck(gameDesc.getDECK());
            shuffle.shuffle(deck.cards); //shuffle deck according to the given seed
            game.dealCards(playerArray, deck, currentPlayer);

            //Sends every player's hand to the GUI
            JsonObject jsonMessage = new JsonObject();
            jsonMessage.add("type", new JsonPrimitive("gameplay"));
            jsonMessage.add("subtype", new JsonPrimitive("playerhands"));
            JsonObject jsonPlayers = new JsonObject();
            JsonArray jsonPlayersArray = new JsonArray();
            for (int i = 0; i < playerArray.length; i++) {
                LinkedList<Card> hand = playerArray[i].getHand().getHand();
                JsonObject jsonPlayer = new JsonObject();
                JsonArray jsonCards = new JsonArray();
                for (Card card: hand) {
                    jsonCards.add(new Gson().fromJson(card.getJSON(), JsonObject.class));
                }
                jsonPlayer.add("playerindex", new JsonPrimitive(i));
                jsonPlayer.add("hand", jsonCards);
                jsonPlayersArray.add(jsonPlayer);
            }
            jsonPlayers.add("players", jsonPlayersArray);
            webSocket.send(jsonPlayers.getAsString());



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
                boolean rdmEventHappenedTRICK = false;

                if (printMoves) {
                    System.out.println("Trump is " + game.trumpSuit.toString());
                }
                //Each player plays a card
                for (int i = 0; i < playerArray.length; i++) {
                    if (!rdmEventHappenedTRICK) {
                        rdmEvent rdmEvent = rdmEventsManager.eventChooser("TRICK");
                        if (rdmEvent != null){
                            //Do rdmevent
                            System.out.println("Random event creation start");
                            game.runRdmEvent(rdmEvent);
                            rdmEventHappenedTRICK = true;
                        }
                    }
                    game.currentTrick.getCard(playerArray[currentPlayer].playCard(game.trumpSuit.toString(), game.currentTrick));
                    game.broadcastMoves(game.currentTrick.get(i), currentPlayer, playerArray);

                    //Send card played to GUI
                    JsonObject cardPlayed = new JsonObject();
                    cardPlayed.add("type", new JsonPrimitive("gameplay"));
                    cardPlayed.add("subtype", new JsonPrimitive("cardplayed"));
                    cardPlayed.add("playerindex", new JsonPrimitive(currentPlayer));
                    cardPlayed.add("card", new Gson().fromJson(game.currentTrick.get(i).getJSON(), JsonObject.class));
                    webSocket.send(cardPlayed.getAsString());

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
                for (Team team : teams) {
                    if (team.findPlayer(currentPlayer)) {
                        team.setTricksWon(team.getTricksWon() + 1);
                        if (printMoves) {
                            System.out.println("Player " + (currentPlayer + 1) + " was the winner of the trick with the " + winningCard.toString());
                            System.out.println("Tricks won: " + team.getTricksWon());
                        }

                        //sends winningCard, who that card belonged to and that persons team to the GUI



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
                for (Team team : teams) {
                    int score = team.getTricksWon();
                    if (score > gameDesc.getTrickThreshold()) { // if score greater than trick threshold
                        team.setScore(team.getScore() + (score - gameDesc.getTrickThreshold())); // add score to team's running total
                    }
                    team.setTricksWon(0);
                }
            }
            //
            if (gameDesc.getCalculateScore().equals("bid")) { //TODO handle special bids.
                for (Team team : teams) {
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
            rdmEventsManager.checkGameCloseness(teams);

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

    private void runRdmEvent(rdmEvent rdmEvent) {
        System.out.println("rdm Event runner triggered");
        swapHands(rdmEvent.getWeakestTeam(), rdmEvent.getStrongestTeam());
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

        System.out.println("swapHands triggered");
    }

    private Predicate<Card> getValidCard() {
        return validCard;
    }

    public Bid[] getBidTable() {
        return bidTable;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {

    }
}
