package src.gameEngine;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.apache.commons.lang3.tuple.Pair;
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
import src.player.GUIPlayer;
import src.player.LocalPlayer;
import src.player.NetworkPlayer;
import src.player.Player;
import src.rdmEvents.RdmEvent;
import src.rdmEvents.RdmEventsManager;
import src.team.Team;

import java.net.InetSocketAddress;
import java.util.*;
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
    private List<Trick> trickHistory;
    //Predicate functions used in determining if card moves are valid
    private Predicate<Card> validCard;
    private Predicate<Card> validLeadingCard;
    private IntFunction<Integer> nextPlayerIndex;
    //If you bid suits
    private boolean trumpSuitBid;
    //Redoubling field set to true if the contract was a redoubling, doubling field same,
    // the BidValue is the adjusted value based on any redoubling/doubling, bidSuit is the trumpSuit for the trick
    private ContractBid adjustedHighestBid;

    static ArrayList<Team> teams = new ArrayList<>();
    //Starts as 1 in 10 chance;
    double rdmEventProb = 10;
    WebSocket webSocket = null;
    private static final Object GUIConnectLock = new Object();
    private static final Object getCardLock = new Object();
    private static final Object getBidLock = new Object();
    private Bid currentBid = null;
    private static final Object getCardSwapLock = new Object();
    private static boolean cardSwapFlag = false;
    private Player[] playerArray;

    /**
     * Set up game engine
     *
     * @param desc game description
     */
    public GameEngine(GameDesc desc) {
        this(desc, new InetSocketAddress("localhost", 60001),null);
    }

    public GameEngine(GameDesc desc, InetSocketAddress address, Player[] playerArray) {
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
        this.trickHistory = new LinkedList<>();
        this.trumpSuitBid = desc.isTrumpSuitBid();
        this.playerArray = playerArray;
    }

    public static void main(GameDesc gameDesc, int dealer, Player[] playerArray, int seed, boolean printMoves, boolean enableRandomEvents) throws InterruptedException {
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
        RdmEventsManager rdmEventsManager = new RdmEventsManager(gameDesc, game.getTeams(), rand, playerArray, enableRandomEvents);

        Deck deck; // make standard deck from a linked list of Cards
        Shuffle shuffle = new Shuffle(seed);

        if (printMoves) {
            game.printScore();
        }

        //Loop until game winning condition has been met
        do {
            int currentPlayer = dealer;
            deck = new Deck(gameDesc.getDECK());

            shuffle.shuffle(deck.cards); //shuffle deck according to the given seed
            game.dealCards(playerArray, deck, currentPlayer);

            //Check for a random event at start of hand - run logic if successful
            RdmEvent rdmEventHAND = rdmEventsManager.eventChooser("SPECIAL-CARD");
            if (rdmEventHAND != null) {
                rdmEventsManager.runSpecialCardSetup(rdmEventHAND);
            }

            currentPlayer = game.nextPlayerIndex.apply(currentPlayer);

            if (gameDesc.isBidding()) {
                game.getBids(currentPlayer, playerArray, gameDesc);
            }
            if (printMoves) {
                System.out.println("-----------------------------------");
                System.out.println("----------------PLAY---------------");
                System.out.println("-----------------------------------");
            }
            //Loop until trick has completed (all cards have been played)
            do {
                if (gameDesc.getTrumpPickingMode().equals("bid")) {
                    game.trumpSuit.replace(0, game.trumpSuit.length(), game.getAdjustedHighestBid().getSuit());
                }
                if (printMoves) {
                    System.out.println("Trump is " + game.trumpSuit.toString());
                }

                //Check for a random event at start of trick - run logic if successful
                RdmEvent rdmEventTRICK = rdmEventsManager.eventChooser("TRICK");
                if (rdmEventTRICK != null) {
                    if (rdmEventTRICK.getName().equals("SwapHands")) {
                        rdmEventsManager.runSwapHands();
                    }
                    else {
                        rdmEventsManager.runSwapCards();
                    }
                }

                //Each player plays a card
                for (int i = 0; i < playerArray.length; i++) {
                    game.currentTrick.getCard(playerArray[currentPlayer].playCard(game.trumpSuit.toString(), game.currentTrick));
                    game.broadcastMoves(game.currentTrick.get(i), currentPlayer, playerArray);
                    //If a special card has been placed in deck, check if it has just been played - adjust points if it has.
                    if (rdmEventHAND != null) {
                        String playedCardType =  game.currentTrick.getHand().get(game.currentTrick.getHandSize()-1).getSpecialType();
                        if (playedCardType != null) {
                            rdmEventsManager.runSpecialCardOps(playedCardType, currentPlayer, game.getTeams());
                        }
                    }
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

                Team winningTeam = playerArray[currentPlayer].getTeam();
                winningTeam.setTricksWon(winningTeam.getScore() + 1);
                if (printMoves) {
                    System.out.println("Player " + (currentPlayer + 1) + " was the winner of the trick with the " + winningCard.toString());
                    System.out.println("Tricks won: " + winningTeam.getTricksWon());
                }

                //Signal that trump suit was broken -> can now be played
                if (game.currentTrick.getHand().stream().anyMatch(card -> card.getSUIT().equals(game.trumpSuit.toString()))) {
                    game.breakFlag.set(true);
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
                        teamBid += player.getBid().getBidValue();
                    }
                    Bid bid = new Bid(false, null, teamBid, false);
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
            if (enableRandomEvents) {
                rdmEventsManager.checkGameCloseness();
            }

            game.printScore();
        } while (game.gameEnd());
        System.out.println("End of Game");
    }

    public static void main(GameDesc gameDesc, int dealer, Player[] playerArray, int seed, boolean printMoves, boolean enableRandomEvents, WebSocket oldWebSocket) throws InterruptedException {
        GameEngine game = new GameEngine(gameDesc);
        Random rand = new Random(seed);
        Gson gson = new Gson();

        game.start();
        JsonObject gameSetup = new JsonObject();
        gameSetup.add("type", new JsonPrimitive("gameSetup"));
        gameSetup.add("port", new JsonPrimitive(game.getPort()));
        oldWebSocket.send(gson.toJson(gameSetup));

        assert playerArray.length == gameDesc.getNUMBEROFPLAYERS(); //TODO remove

        /* initialize each players hands */

        for (Player player : playerArray) {
            player.initCanBePlayed(game.getValidCard());
        }

        //wait for GUI to connect to back-end
        synchronized (GUIConnectLock) {
            while (game.webSocket == null) {
                System.out.println("WAITING FOR TUNNEL");
                GUIConnectLock.wait();
                System.out.println("TUNNEL RECEIVED");
            }
        }
        for (Player player : playerArray) {
            if (player instanceof GUIPlayer) {
                System.out.println("SETTING PLAYER WEBSOCKET");
                ((GUIPlayer) player).setWebSocket(game.webSocket);
            }
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
        RdmEventsManager rdmEventsManager = new RdmEventsManager(gameDesc, game.getTeams(), rand, playerArray, enableRandomEvents);

        Deck deck; // make standard deck from a linked list of Cards
        Shuffle shuffle = new Shuffle(seed);

        if (printMoves) {
            game.printScore();
        }

        //Loop until game winning condition has been met
        do {
            int currentPlayer = dealer;
            deck = new Deck(gameDesc.getDECK());

            shuffle.shuffle(deck.cards); //shuffle deck according to the given seed
            game.dealCards(playerArray, deck, currentPlayer);

            //Check for a random event at start of hand - run logic if successful
            RdmEvent rdmEventHAND = rdmEventsManager.eventChooser("SPECIAL-CARD");
            if (rdmEventHAND != null) {
                rdmEventsManager.runSpecialCardSetup(rdmEventHAND);
            }

            //Sends every player's hand to the GUI
            System.out.println("SENDING CARDS TO GUI");
            JsonObject jsonPlayers = new JsonObject();
            jsonPlayers.add("type", new JsonPrimitive("playerhands"));
            JsonArray jsonPlayersArray = new JsonArray();
            for (int i = 0; i < playerArray.length; i++) {
                LinkedList<Card> hand = playerArray[i].getHand().getHand();
                JsonObject jsonPlayer = new JsonObject();
                JsonArray jsonCards = new JsonArray();
                for (Card card : hand) {
                    jsonCards.add(gson.fromJson(card.getJSON(), JsonObject.class)); //converts hand to JSON array of JSON objects
                }
                jsonPlayer.add("playerindex", new JsonPrimitive(i));
                jsonPlayer.add("hand", jsonCards);
                jsonPlayersArray.add(jsonPlayer);
            }
            jsonPlayers.add("players", jsonPlayersArray);
            jsonPlayers.add("target", new JsonPrimitive("GUI"));
            game.webSocket.send(gson.toJson(jsonPlayers));

            currentPlayer = game.nextPlayerIndex.apply(currentPlayer);

            if (gameDesc.isBidding()) {
                game.getBids(currentPlayer, playerArray, gameDesc);
            }
            if (printMoves) {
                System.out.println("-----------------------------------");
                System.out.println("----------------PLAY---------------");
                System.out.println("-----------------------------------");
            }
            //Loop until trick has completed (all cards have been played)
            do {
                if (gameDesc.getTrumpPickingMode().equals("bid")) {
                    game.trumpSuit.replace(0, game.trumpSuit.length(), game.getAdjustedHighestBid().getSuit());
                }
                if (printMoves) {
                    System.out.println("Trump is " + game.trumpSuit.toString());
                }
                //send trump suit to front-end
                JsonObject currentTrumpMsg = new JsonObject();
                currentTrumpMsg.add("type", new JsonPrimitive("currenttrump"));
                currentTrumpMsg.add("suit", new JsonPrimitive(game.trumpSuit.toString()));

                //Check for a random event at start of trick - run logic if successful
                RdmEvent rdmEventTRICK = rdmEventsManager.eventChooser("TRICK");
                if (rdmEventTRICK != null) {
                    if (rdmEventTRICK.getName().equals("SwapHands")) {
                        Pair<Player,Player> swappedPlayers = rdmEventsManager.runSwapHands();

                        //Send swapped hand event to front-end
                        JsonObject swappedHandsEvent = new JsonObject();
                        swappedHandsEvent.add("type", new JsonPrimitive("handswap"));
                        JsonArray swappedPlayersJson = new JsonArray();
                        swappedPlayersJson.add(swappedPlayers.getLeft().getPlayerNumber());
                        swappedPlayersJson.add(swappedPlayers.getRight().getPlayerNumber());
                        swappedHandsEvent.add("playerswapped", swappedPlayersJson);
                    }
                    else {
                        synchronized (getCardSwapLock) {
                            while (!cardSwapFlag) {
                                cardSwapFlag = rdmEventsManager.runSwapCards();
                                getCardSwapLock.wait();
                            }
                        }
                    }
                }

                //Each player plays a card
                for (int i = 0; i < playerArray.length; i++) {
                    synchronized (getCardLock) {
                        game.currentTrick.getCard(playerArray[currentPlayer].playCard(game.trumpSuit.toString(), game.currentTrick));
                        System.out.println("CURRENT TRICK: "+ game.currentTrick.toString());
                        while (game.currentTrick.getHand().getLast() == null) {//last card should only be null if GUIplayer
                            System.out.println("WAITING FOR CARD");
                            getCardLock.wait();
                        }
                    }
                    game.broadcastMoves(game.currentTrick.get(i), currentPlayer, playerArray);

                    //Send card played to GUI
                    JsonObject cardPlayed = new JsonObject();
                    cardPlayed.add("target", new JsonPrimitive("GUI"));
                    cardPlayed.add("type", new JsonPrimitive("cardplayed"));
                    cardPlayed.add("playerindex", new JsonPrimitive(currentPlayer));
                    cardPlayed.add("card", gson.fromJson(game.currentTrick.get(i).getJSON(), JsonObject.class));
                    System.out.println("cardplayed: " + cardPlayed);
                    game.webSocket.send(gson.toJson(cardPlayed));


                    //If a special card has been placed in deck, check if it has just been played - adjust points if it has.
                    if (rdmEventHAND != null) {
                        String playedCardType =  game.currentTrick.getHand().get(game.currentTrick.getHandSize()-1).getSpecialType();
                        if (playedCardType != null) {
                            rdmEventsManager.runSpecialCardOps(playedCardType, currentPlayer, game.getTeams());

                            //Message Special card event
                            JsonObject specialCardEvent = new JsonObject();
                            specialCardEvent.add("type", new JsonPrimitive("specialcard"));
                            specialCardEvent.add("player", new JsonPrimitive(currentPlayer));
                            specialCardEvent.add("team", new JsonPrimitive(playerArray[currentPlayer].getTeam().getTeamNumber()));
                            specialCardEvent.add("cardtype", new JsonPrimitive(playedCardType));

                        }
                    }
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

                //send WinningCard, player and trick to front-end
                JsonObject winningCardJson = new JsonObject();
                winningCardJson.add("type", new JsonPrimitive("winningcard"));
                winningCardJson.add("card", gson.fromJson(winningCard.getJSON(), JsonObject.class));
                winningCardJson.add("playerindex", new JsonPrimitive(currentPlayer));
//                JsonArray array = new JsonArray();
//                for (Card card : game.currentTrick.getHand()) {
//                    array.add(new Gson().fromJson(card.getJSON(), JsonObject.class));
//                }
//                winningCardJson.add("trick", array);
                System.out.println("winningCardJson: " + winningCardJson.getAsString());
                game.webSocket.send(winningCardJson.getAsString());

                Team winningTeam = playerArray[currentPlayer].getTeam();
                winningTeam.setTricksWon(winningTeam.getScore() + 1);
                if (printMoves) {
                    System.out.println("Player " + (currentPlayer + 1) + " was the winner of the trick with the " + winningCard.toString());
                    System.out.println("Tricks won: " + winningTeam.getTricksWon());
                }

                //Signal that trump suit was broken -> can now be played
                if (game.currentTrick.getHand().stream().anyMatch(card -> card.getSUIT().equals(game.trumpSuit.toString()))) {
                    game.breakFlag.set(true);
                    //send trumpbroken message to front-end
                    JsonObject trumpBroken = new JsonObject();
                    trumpBroken.add("type", new JsonPrimitive("trumpbroken"));
                    game.webSocket.send(trumpBroken.getAsString());
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
                        teamBid += player.getBid().getBidValue();
                    }
                    Bid bid = new Bid(false, null, teamBid, false);
                    //Increase score of winning team based on bid scoring system (See validBids.java)
                    team.setScore(team.getScore() + gameDesc.getEvaluateBid().apply(bid, team.getTricksWon()));
                    //Reset tricks won for next round.
                    team.setTricksWon(0);
                }
            }
            if (gameDesc.getTrumpPickingMode().equals("predefined")) {
                game.trumpSuit.replace(0, game.trumpSuit.length(), gameDesc.getTrumpIterator().next());
            }

            //send updated scores when round has ended
            JsonObject roundEndMessage = new JsonObject();
            roundEndMessage.add("type", new JsonPrimitive("roundendmessage"));
            sendTeamScoresJson(game, roundEndMessage);

            //Check if game needs balancing
            if (enableRandomEvents) {
                rdmEventsManager.checkGameCloseness();
            }

            game.printScore();
        } while (game.gameEnd());
        //send updated scores when game has ended
        JsonObject roundEndMessage = new JsonObject();
        roundEndMessage.add("type", new JsonPrimitive("gameendmessage"));
        sendTeamScoresJson(game, roundEndMessage);

        System.out.println("End of Game");
    }

    private static void sendTeamScoresJson(GameEngine game, JsonObject message) {
        JsonArray scoresArray = new JsonArray();
        for (Team team: teams) {
            JsonObject teamJson = new JsonObject();
            teamJson.add("teamnumber", new JsonPrimitive(team.getTeamNumber()));
            teamJson.add("teamscore", new JsonPrimitive(team.getScore()));
            scoresArray.add(teamJson);
        }
        message.add("scores", scoresArray);
        game.webSocket.send(message.getAsString());
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
     * @param currentPlayer index of current player in the player array
     * @param players the array of players
     */
    private void getBids(int currentPlayer, Player[] players, GameDesc desc) throws InterruptedException {
        System.out.println("-----------------------------------");
        System.out.println("--------------BIDDING--------------");
        System.out.println("-----------------------------------");
        int originalCurrentPlayer = currentPlayer;
        int passCounter = 0;
        do {
            //Adds the bids (checks they are valid in other class)
            synchronized (getBidLock) {
                while (currentBid == null) {
                    currentBid = players[currentPlayer].makeBid(this.desc.getValidBid(), trumpSuitBid, adjustedHighestBid);
                    getBidLock.wait();
                }
            }
            Bid bid = currentBid;
            if (bid.isDoubling()) {
                passCounter = 0;
                if (getAdjustedHighestBid().isDoubling()) {
                    getAdjustedHighestBid().setDoubling(false);
                    getAdjustedHighestBid().setRedoubling(true);
                }
                else {
                    getAdjustedHighestBid().setDoubling(true);
                }
                getAdjustedHighestBid().setBidValue(getAdjustedHighestBid().getBidValue()*2);
            }
            else {
                if (bid.getBidValue() >= 0) {
                    passCounter = 0;
                    if (getAdjustedHighestBid() == null) {
                        String suit = null;
                        if (trumpSuitBid) {
                            suit = bid.getSuit();
                        }
                        setAdjustedHighestBid(new ContractBid(false, suit, bid.getBidValue(), false, false, players[currentPlayer]));
                    }
                    else {
                        if (trumpSuitBid) {
                            getAdjustedHighestBid().setSuit(bid.getSuit());
                            if (!(getAdjustedHighestBid().getSuit().equals(bid.getSuit()))) {
                                getAdjustedHighestBid().setDeclarer(players[currentPlayer]);
                            }
                        }
                        getAdjustedHighestBid().setRedoubling(false);
                        getAdjustedHighestBid().setDoubling(false);
                        getAdjustedHighestBid().setBidValue(bid.getBidValue());
                    }
                }
                else {
                    passCounter += 1;
                }
            }
            //System.out.println(getAdjustedHighestBid());
            players[currentPlayer].setBid(bid);
            broadcastBids(players[currentPlayer].getBid(), currentPlayer, players);
            currentPlayer = this.nextPlayerIndex.apply(currentPlayer);
        }
        while (getBiddingEnd(players, currentPlayer, originalCurrentPlayer, passCounter, desc));
    }

    private boolean getBiddingEnd(Player[] players, int currentPlayer, int originalPlayer, int passCounter, GameDesc desc) {
        //TODO:Adjust this if game desc field gets added
        if (desc.isCanPass()) {
            return passCounter != players.length - 1;
        }
        else {
            return currentPlayer != originalPlayer;
        }
    }


    /**
     * Distributes cards from the deck starting from the dealer +/- 1
     *
     * @param players     the array of current players
     * @param deck        deck of cards being dealt
     * @param dealerIndex the index of the first person being dealt a card
     */
    private void dealCards(Player[] players, Deck deck, int dealerIndex) {
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
    private Card winningCard() {
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


    private Predicate<Card> getValidCard() {
        return validCard;
    }

    public ArrayList<Team> getTeams() {
        return teams;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
//        System.out.println("WAITING FOR LOCK");
        synchronized (GUIConnectLock) {
//            System.out.println("LOCK RECEIVED");
            if (getConnections().size() == 1) {
                System.out.println("Opened connection");
                this.webSocket = conn;
                GUIConnectLock.notifyAll();
            }
        }
    }

    private ContractBid getAdjustedHighestBid() {
        return adjustedHighestBid;
    }

    private void setAdjustedHighestBid(ContractBid adjustedHighestBid) {
        this.adjustedHighestBid = adjustedHighestBid;
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
    }


    @Override
    public void onMessage(WebSocket conn, String message) {
        JsonObject request = new Gson().fromJson(message, JsonObject.class);
        System.out.println("MESSAGE RECEIVED: " + message);
        switch (request.get("type").getAsString()) {
            case "playcard":
                synchronized (getCardLock) {
                    currentTrick.dropLast();
                    currentTrick.getCard(Card.fromJson(request.get("card").getAsString()));
                    getCardLock.notifyAll();
                }
                break;
            case "makebid":
                JsonObject bidJson = request.getAsJsonObject("bid");
                synchronized (getBidLock) {
                    boolean doubling = bidJson.get("doubling").getAsBoolean();
                    String suit = bidJson.get("suit").getAsString();
                    int bidValue = bidJson.get("bidValue").getAsInt();
                    boolean blind = bidJson.get("blindBid").getAsBoolean();
                    currentBid = new Bid(doubling, suit, bidValue, blind);
                    getBidLock.notify();
                }
                break;
            case "getswap":
                synchronized (getCardSwapLock) {
                    Player swapper = playerArray[request.get("choosingplayer").getAsInt()];
                    Player beingSwapped = playerArray[request.get("otherplayer").getAsInt()];
                    Card swapperCard = Card.fromJson(request.get("choosingplayercard").getAsString());
                    Card beingSwappedCard = Card.fromJson(request.get("otherplayercard").getAsString());
                    beingSwapped.getHand().getCard(swapper.getHand().giveCard(swapperCard));
                    swapper.getHand().getCard(beingSwapped.getHand().giveCard(beingSwappedCard));
                    cardSwapFlag = true;
                    getCardSwapLock.notify();
                    System.out.println("Swappers Cards: " + swapper.getHand().toString());
                    System.out.println("Swappee's Cards: " + beingSwapped.getHand().toString());
                }
                break;


        }

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Server Started \nwaiting for connection on port: " + getPort() + "...");

    }


}
