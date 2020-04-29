package src.gameEngine;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.tuple.Pair;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import src.bid.Bid;
import src.bid.ContractBid;
import src.card.Card;
import src.card.CardComparator;
import src.deck.Deck;
import src.deck.Shuffle;
import src.functions.PlayerIncrementer;
import src.functions.validCards;
import src.parser.GameDesc;
import src.player.GUIPlayer;
import src.player.LocalPlayer;
import src.player.NetworkPlayer;
import src.player.POMDPPlayer;
import src.player.Player;
import src.rdmEvents.RdmEventsManager;
import src.team.Team;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/*  Version 1.1     */

/**
 * Main class that runs the game based on a provided game description.
 */
public class GameEngine extends WebSocketServer {
    //The game description that defines the game to be played
    private GameDesc desc;
    private StringBuilder trumpSuit;
    private ArrayList<Team> teams;
    //The current trick deck is stored in a hand made visible to the entire table (all players)
    private Hand currentTrick;
    //Flag to signal when/if the trump is broken
    private AtomicBoolean breakFlag;
    private int handsPlayed;
    //Object that stores the highest valid bid played at any point for contract games - includes doubling/redoubling.
    private ContractBid adjustedHighestBid;

    //Predicate function used in determining if card moves are valid
    private Predicate<Card> validCard;
    //Predicate function used to increment/rotate play from the current player
    private IntFunction<Integer> nextPlayerIndex;
    private int handSize;

    WebSocket webSocket = null;
    private static final Semaphore GUIConnectLock = new Semaphore(0);
    private static final Semaphore getCardLock = new Semaphore(0);
    private static final Semaphore getBidLock = new Semaphore(0);
    private Bid currentBid = null;
    private static final Object getCardSwapLock = new Object();
    private static boolean cardSwapFlag = false;
    private Player[] playerArray;
    private Semaphore lock;

    /**
     * Sets up game engine, sets attributes based on provided game description
     *
     * @param desc game description
     */
    public GameEngine(GameDesc desc) {
        this(desc, new InetSocketAddress("localhost", 60001), null, null);
    }

    public GameEngine(GameDesc desc, Semaphore lock) {
        this(desc, new InetSocketAddress("localhost", 60001), null, lock);
    }

    public GameEngine(GameDesc desc, InetSocketAddress address, Player[] playerArray, Semaphore lock) {
        super(address);
        this.desc = desc;
        this.trumpSuit = new StringBuilder();
        //Set fixed trump suit if specified
        if (desc.getTrumpPickingMode().equals("fixed")) {
            this.trumpSuit.append(desc.getTrumpSuit());
        }
        //Sets trump suit to predefined order if specified
        if (desc.getTrumpPickingMode().equals("predefined")) {
            this.trumpSuit.append(desc.getTrumpIterator().next());
        }
        //Initialises main attributes
        this.breakFlag = new AtomicBoolean(false);
        this.teams = new ArrayList<>();
        this.handsPlayed = 0;
        this.currentTrick = new Hand();
        //Initialises predicate to determine if a card chosen to lead a trick is valid, based on the game specification.
        Predicate<Card> validLeadingCard = validCards.getValidLeadingCardPredicate(desc.getLeadingCardForEachTrick(), this.trumpSuit, breakFlag);
        //Initialises other predicates based on the game specification.
        this.validCard = validCards.getValidCardPredicate("trick", this.trumpSuit, this.currentTrick, validLeadingCard);
        this.nextPlayerIndex = PlayerIncrementer.generateNextPlayerFunction(desc.isDEALCARDSCLOCKWISE(), desc.getNUMBEROFPLAYERS());
        this.lock = lock;
        this.playerArray = playerArray;
    }

    /**
     * Main driver method to start game logic for an instance
     *
     * @param gameDesc           The description of the game to run
     * @param dealer             The index of player set to deal first
     * @param playerArray        The array specifying the players in the game
     * @param seed               The seed used to initialise random actions
     * @param printMoves         Flag if moves should be printed for debugging
     * @param enableRandomEvents Flag if random events are enabled in the game
     */
    public static void main(GameDesc gameDesc, int dealer, Player[] playerArray, int seed, boolean printMoves, boolean enableRandomEvents) throws InterruptedException {
        //Start engine and set attributes based on game description
        GameEngine game = new GameEngine(gameDesc);
        Random rand = new Random(seed);


        //Initialise each players hands
        for (Player player : playerArray) {
            player.initPlayer(game.getValidCard(), gameDesc, game.trumpSuit);
        }

        //Assign players to teams as specified in the game description
        int teamCounter = 0;
        for (int[] team : gameDesc.getTeams()) {
            Player[] players = new Player[team.length];
            for (int i = 0; i < team.length; i++) {
                players[i] = playerArray[team[i]];
            }
            game.getTeams().add(new Team(players, teamCounter));
            teamCounter++;
        }

        //Initialise random events
        RdmEventsManager rdmEventsManager = new RdmEventsManager(gameDesc, game.getTeams(), rand, playerArray, enableRandomEvents);

        Deck deck;
        Shuffle shuffle = new Shuffle(seed);

        if (printMoves) {
            game.printScore();
        }
        //Loop until session/match ends
        do {
            //Loop until game winning condition has been met
            do {
                //Set the current player to the dealer
                int currentPlayer = dealer;
                //Create the deck
                deck = new Deck((LinkedList<Card>) gameDesc.getDECK());
                //Shuffle deck according to the given seed and deal the cards
                shuffle.shuffle(deck.cards);
                game.handSize = gameDesc.getHandSize();
                game.dealCards(playerArray, deck, currentPlayer);

                //Reset players to non-ai
                for (Player player : playerArray) {
                    if (player instanceof LocalPlayer) {
                        ((LocalPlayer) player).setAiTakeover(false);
                    }
                }
                //Check for a random event for duration of hand
                String rdmEventHAND = rdmEventsManager.eventChooser("HAND");
                if (rdmEventHAND != null) {
                    if (rdmEventHAND.equals("AI-TAKEOVER")) {
                        //Set weakest player to be taken over by AI
                        Player aiPLayer = rdmEventsManager.getRdmWeakestPlayer();
                        if (aiPLayer instanceof LocalPlayer) {
                            ((LocalPlayer) aiPLayer).setAiTakeover(true);
                        }
                    } else {
                        //Add special card to deck
                        rdmEventsManager.runSpecialCardSetup(rdmEventHAND);
                    }
                }

                //Increment the current player to set the first bidder
                currentPlayer = game.nextPlayerIndex.apply(currentPlayer);

                //Signal to players that a new hand has started.
                for (Player player : playerArray) {
                    //This doesn't do anything - assume middleware/frontend will handle
                    player.startHand(game.trumpSuit, game.handSize);
                }

                //Get bids from players if necessary
                if (gameDesc.isBidding()) {
                    game.getBids(currentPlayer, playerArray);
                }

                //Set first player to left of declarer if needed //TODO: Get from game desc when added
                int dummyPlayer = -1;
                if (gameDesc.getFirstTrickLeader().equals("contract")) {
                    //Get the declarer of the final bid, set the player to lead the trick as to the 'left'
                    currentPlayer = game.getAdjustedHighestBid().getDeclarer().getPlayerNumber();
                    currentPlayer = game.nextPlayerIndex.apply(currentPlayer);
                    //Set dummy player to the declarer's partner
                    dummyPlayer = game.nextPlayerIndex.apply(game.nextPlayerIndex.apply(currentPlayer));
                }

                if (printMoves) {
                    System.out.println("-----------------------------------");
                    System.out.println("----------------PLAY---------------");
                    System.out.println("-----------------------------------");
                }

                //Loop until hand has completed (all cards have been played)
                do {
                    //If the trump is based on bidding, set the trump suit based on the final bid
                    if (gameDesc.getTrumpPickingMode().equals("bid")) {
                        game.trumpSuit.replace(0, game.trumpSuit.length(), game.getAdjustedHighestBid().getSuit());
                    }
                    if (printMoves) {
                        System.out.println("Trump is " + game.trumpSuit.toString());
                    }

                    //Check for a random event at start of trick if a HAND event isn't active - run logic if successful
                    String rdmEventTRICK = rdmEventsManager.eventChooser("TRICK");
                    if (rdmEventTRICK != null && rdmEventHAND == null) {
                        if (rdmEventTRICK.equals("SwapHands")) {
                            rdmEventsManager.runSwapHands();
                        } else {
                            rdmEventsManager.runSwapCards();
                        }
                    }

                    //If a dummy player is in operation, show the hand to all players
                    if (dummyPlayer >= 0) {
                        for (Player player : playerArray) {
                            player.broadcastDummyHand(dummyPlayer, playerArray[dummyPlayer].getHand().getHand());
                        }
                    }

                    //Loop for all players to play a card
                    for (int i = 0; i < playerArray.length; i++) {
                        //Add the card played by the player to the current trick
                        game.currentTrick.getCard(playerArray[currentPlayer].playCard(game.trumpSuit.toString(), game.currentTrick));
                        //Broadcast the card played to all players
                        game.broadcastMoves(game.currentTrick.get(i), currentPlayer, playerArray);
                        //If a special card has been placed in deck, check if it has just been played - adjust points if it has.
                        if (rdmEventHAND != null && (rdmEventHAND.equals("BOMB") || rdmEventHAND.equals("HEAVEN"))) {
                            String playedCardType = game.currentTrick.getHand().get(game.currentTrick.getHandSize() - 1).getSpecialType();
                            if (playedCardType != null) {
                                rdmEventsManager.runSpecialCardOps(playedCardType, currentPlayer);
                            }
                        }
                        //Rotate the play
                        currentPlayer = game.nextPlayerIndex.apply(currentPlayer);
                    }

                    //Determine winning card
                    Card winningCard = game.winningCard();

                    //TODO: Explain this
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

                    //Find the team with the winning player and increment their tricks score
                    Team winningTeam = playerArray[currentPlayer].getTeam();
                    winningTeam.setTricksWon(winningTeam.getTricksWon() + 1);
                    winningTeam.addCardsWon(game.currentTrick.getHand());
                    if (printMoves) {
                        System.out.println("Player " + (currentPlayer + 1) + " was the winner of the trick with the " + winningCard.toString());
                        System.out.println("Tricks won: " + winningTeam.getTricksWon());
                    }

                    //Signal that trump suit was broken -> cards with the trump can now be played
                    if (game.currentTrick.getHand().stream().anyMatch(card -> card.getSUIT().equals(game.trumpSuit.toString()))) {
                        game.breakFlag.set(true);
                    }

                    //Reset trick hand by discarding the cards
                    game.currentTrick.dropHand();
                } while (playerArray[0].getHand().getHandSize() > gameDesc.getMinHandSize()); //Continue play until all cards have been played

                game.handsPlayed++;
                //Calculate the score of the hand
                game.calculateScore();

                //Increment the trump suit if specified
                if (gameDesc.getTrumpPickingMode().equals("predefined") && game.gameEnd()) {
                    game.trumpSuit.replace(0, game.trumpSuit.length(), gameDesc.getTrumpIterator().next());
                }

                //If random events is enabled - check if game needs balancing
                if (enableRandomEvents) {
                    rdmEventsManager.checkGameCloseness();
                }

                //Increment dealer for next hand
                dealer = game.nextPlayerIndex.apply(dealer);

                game.printScore();
            } while (game.gameEnd()); //Check if game ending condition has been met

            //If match scoring is bestOf
            setScoresAndVulnerability(gameDesc, game);
            System.out.println("End of Game");
        } while (game.sessionEnd()); //Check for match/session winning condition to be met

        //If game is specified to be best of set of games, find overall match winner
        findMatchWinner(gameDesc, game);
    }

    private static void setScoresAndVulnerability(GameDesc gameDesc, GameEngine game) {
        if (gameDesc.getSessionEnd().equals("bestOf")) {
            for (Team team : game.getTeams()) {
                //If the team won the game by surpassing the threshold, increment their match score, and set vulnerability
                if (team.getGameScore() >= gameDesc.getScoreThreshold()) {
                    System.out.println("Team " + team.getTeamNumber() + " wins game");
                    team.setGamesWon(team.getGamesWon() + 1);
                    team.setCumulativeScore(team.getCumulativeScore() + team.getGameScore());
                    team.setVulnerable(true);
                } else {
                    //Team that lost the game can't be vulnerable
                    team.setVulnerable(false);
                    //Add score from the game to cumulative score
                    team.setCumulativeScore(team.getCumulativeScore() + team.getGameScore());
                }
                //Reset all teams game score for new game
                team.setGameScore(0);
            }
        }
    }

    //Auxiliary method for calculating the score of a hand
    private void calculateScore() {
        if (desc.getCalculateScore().equals("tricksWon")) {
            //Check if any team has exceeded the threshold, if they have increment their score accordingly
            for (Team team : getTeams()) {
                int score = team.getTricksWon();
                if (score > desc.getTrickThreshold()) {
                    team.setGameScore(team.getGameScore() + (score - desc.getTrickThreshold()));
                }
                //Reset trick score
                team.setTricksWon(0);
            }
        }
        if (desc.getCalculateScore().equals("trumpPointValue")) {
            for (Team team : getTeams()) {
                int score = team.getCardsWon().stream().filter((card -> card.getSUIT().equals(trumpSuit.toString()))).mapToInt(Card::getPointValue).sum();
                team.setGameScore(team.getGameScore() + score);
                team.setTricksWon(0);
                team.getCardsWon().clear();
            }
        }
        if (desc.getCalculateScore().equals("bid")) {
            //If game is bridge style scoring
            if (desc.isAscendingBid()) {
                //Find the team that declared/won the contract
                Team declaringTeam = getAdjustedHighestBid().getTeam();
                //Set the contract's vulnerability based on the declaring Team's state
                getAdjustedHighestBid().setVulnerable(declaringTeam.isVulnerable());
                //If the declaring team matched their contract, increment their game score accordingly (See validBids.java)
                if (declaringTeam.getTricksWon() >= getAdjustedHighestBid().getBidValue()) {
                    declaringTeam.setGameScore(declaringTeam.getGameScore() + desc.getEvaluateBid().apply(getAdjustedHighestBid(), declaringTeam.getTricksWon()));
                }
                //Otherwise, find the opposition team, and increment their game score accordingly
                else {
                    for (Team team : getTeams()) {
                        if (team != declaringTeam) {
                            team.setGameScore(team.getGameScore() + desc.getEvaluateBid().apply(getAdjustedHighestBid(), declaringTeam.getTricksWon()));
                            break;
                        }
                    }
                }
                //Reset the tricks won for all teams
                for (Team team : getTeams()) {
                    team.setTricksWon(0);
                }
            }
            //For spades style bid scoring
            else {
                for (Team team : getTeams()) {
                    int teamBid = 0;
                    //Get collective team bids
                    for (Player player : team.getPlayers()) {
                        teamBid += player.getBid().getBidValue();
                    }
                    Bid bid = new Bid(false, null, teamBid, false, false);
                    //Increase score of winning team based on bid scoring system (See validBids.java)
                    team.setGameScore(team.getGameScore() + desc.getEvaluateBid().apply(bid, team.getTricksWon()));
                    //Reset tricks won for next round.
                    team.setTricksWon(0);
                }
            }
        }
    }

    public static void main(GameDesc gameDesc, int dealer, Player[] playerArray, int seed, boolean printMoves, boolean enableRandomEvents, WebSocket oldWebSocket, Semaphore lock) throws InterruptedException {
        //Start engine and set attributes based on game description
        GameEngine game = new GameEngine(gameDesc, lock);
        Random rand = new Random(seed);
        Gson gson = new Gson();

        game.start();


        //Wait for Middleware to connect to Back-end
        while (game.webSocket == null) {
            System.out.println("WAITING FOR TUNNEL");
            GUIConnectLock.acquire();
            System.out.println("TUNNEL RECEIVED");
        }

        /* initialize each players hands */
        for (Player player : playerArray) {
            player.initPlayer(game.getValidCard(), gameDesc, game.trumpSuit);

            //Sets websocket for player to send to
            if (player instanceof GUIPlayer) {
                System.out.println("SETTING PLAYER WEBSOCKET");
                ((GUIPlayer) player).setWebSocket(game.webSocket);
            }
        }


        //Assign players to teams as specified in the game description
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
        //Loop until session/match ends
        do {
            //Loop until game winning condition has been met
            do {
                //Set the current player to the dealer
                int currentPlayer = dealer;
                //Create the deck
                deck = new Deck((LinkedList<Card>) gameDesc.getDECK());
                //Shuffle deck according to the given seed and deal the cards
                shuffle.shuffle(deck.cards);
                game.handSize = gameDesc.getHandSize();
                game.dealCards(playerArray, deck, currentPlayer);

                //Reset players to non-ai
                for (Player player : playerArray) {
                    if (player instanceof LocalPlayer) {
                        ((LocalPlayer) player).setAiTakeover(false);
                    }
                }

                //Check for a random event for duration of hand
                String rdmEventHAND = rdmEventsManager.eventChooser("HAND");
                if (rdmEventHAND != null) {
                    if (rdmEventHAND.equals("AI-TAKEOVER")) {
                        //Set weakest player to be taken over by AI
                        Player aiPLayer = rdmEventsManager.getRdmWeakestPlayer();
                        if (aiPLayer instanceof LocalPlayer) {
                            ((LocalPlayer) aiPLayer).setAiTakeover(true);
                        }
                    } else {
                        //Add special card to deck
                        rdmEventsManager.runSpecialCardSetup(rdmEventHAND);
                    }
                }

                //Sends every player's hand to the GUI
                sendPlayerHands(playerArray, game, gson);

                currentPlayer = game.nextPlayerIndex.apply(currentPlayer);

                //Signal to players that a new hand has started.
                for (Player player : playerArray) {
                    //This doesn't do anything - assume middleware/frontend will handle
                    player.startHand(game.trumpSuit, game.handSize);
                }

                //Get bids from players if necessary
                if (gameDesc.isBidding()) {
                    game.getBids(currentPlayer, playerArray);
                }

                //Set first player to left of declarer if needed //TODO: Get from game desc when added
                int dummyPlayer = -1;
                if (gameDesc.getFirstTrickLeader().equals("contract")) {
                    //Get the declarer of the final bid, set the player to lead the trick as to the 'left'
                    currentPlayer = game.getAdjustedHighestBid().getDeclarer().getPlayerNumber();
                    currentPlayer = game.nextPlayerIndex.apply(currentPlayer);
                    //Set dummy player to the declarer's partner
                    dummyPlayer = game.nextPlayerIndex.apply(game.nextPlayerIndex.apply(currentPlayer));
                }

                if (printMoves) {
                    System.out.println("-----------------------------------");
                    System.out.println("----------------PLAY---------------");
                    System.out.println("-----------------------------------");
                }

                //Loop until trick has completed (all cards have been played)
                do {
                    //If the trump is based on bidding, set the trump suit based on the final bid
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

                    //Check for a random event at start of trick if a HAND event isn't active - run logic if successful
                    String rdmEventTRICK = rdmEventsManager.eventChooser("TRICK");
                    if (rdmEventTRICK != null && rdmEventHAND == null) {
                        Pair<Player, Player> swappedPlayers = rdmEventsManager.runSwapHands();
                        //Send swapped hand event to front-end
                        JsonObject swappedHandsEvent = new JsonObject();
                        swappedHandsEvent.add("type", new JsonPrimitive("handswap"));
                        JsonArray swappedPlayersJson = new JsonArray();
                        swappedPlayersJson.add(swappedPlayers.getLeft().getPlayerNumber());
                        swappedPlayersJson.add(swappedPlayers.getRight().getPlayerNumber());
                        swappedHandsEvent.add("playerswapped", swappedPlayersJson);
                    } else {
                        synchronized (getCardSwapLock) { //TODO  GUI interaction stuff need to be re-implemented - Jeffrey
                            while (!cardSwapFlag) {
                                cardSwapFlag = rdmEventsManager.runSwapCards();
                                getCardSwapLock.wait();
                            }
                        }
                    }

                    //Each player plays a card
                    for (int i = 0; i < playerArray.length; i++) {
                        //Add the card played by the player to the current trick
                        game.currentTrick.getCard(playerArray[currentPlayer].playCard(game.trumpSuit.toString(), game.currentTrick));
//                    System.out.println("CURRENT TRICK: " + game.currentTrick.toString());
                        while (game.currentTrick.getHand().getLast() == null) { //last card should only be null if GUIplayer
                            System.out.println("WAITING FOR CARD");
                            getCardLock.acquire();
                        }
                        game.broadcastMoves(game.currentTrick.get(i), currentPlayer, playerArray);

                        //If a special card has been placed in deck, check if it has just been played - adjust points if it has.
                        if (rdmEventHAND != null && (rdmEventHAND.equals("BOMB") || rdmEventHAND.equals("HEAVEN"))) {
                            String playedCardType = game.currentTrick.getHand().get(game.currentTrick.getHandSize() - 1).getSpecialType();
                            if (playedCardType != null) {
                                rdmEventsManager.runSpecialCardOps(playedCardType, currentPlayer);

                                //Message Special card event
                                JsonObject specialCardEvent = new JsonObject();
                                specialCardEvent.add("type", new JsonPrimitive("specialcard"));
                                specialCardEvent.add("player", new JsonPrimitive(currentPlayer));
                                specialCardEvent.add("team", new JsonPrimitive(playerArray[currentPlayer].getTeam().getTeamNumber()));
                                specialCardEvent.add("cardtype", new JsonPrimitive(playedCardType));
                            }
                        }

                        //Send card played to GUI
                        JsonObject cardPlayed = new JsonObject();
                        cardPlayed.add("target", new JsonPrimitive("GUI"));
                        cardPlayed.add("type", new JsonPrimitive("cardplayed"));
                        cardPlayed.add("playerindex", new JsonPrimitive(currentPlayer));
                        cardPlayed.add("card", gson.fromJson(game.currentTrick.get(i).getJSON(), JsonObject.class));
                        System.out.println("cardplayed: " + cardPlayed);
                        game.webSocket.send(gson.toJson(cardPlayed));

                        //Rotate the play
                        currentPlayer = game.nextPlayerIndex.apply(currentPlayer);
                    }

                    //Determine winning card
                    Card winningCard = game.winningCard();
                    //TODO: Explain this

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


                    //Find the team with the winning player and increment their tricks score
                    Team winningTeam = playerArray[currentPlayer].getTeam();
                    winningTeam.setTricksWon(winningTeam.getTricksWon() + 1);
                    winningTeam.addCardsWon(game.currentTrick.getHand());
                    if (printMoves) {
                        System.out.println("Player " + (currentPlayer + 1) + " was the winner of the trick with the " + winningCard.toString());
                        System.out.println("Tricks won: " + winningTeam.getTricksWon());
                    }

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
                    game.webSocket.send(gson.toJson(winningCardJson));

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

                    sendPlayerHands(playerArray, game, gson);
                } while (playerArray[0].getHand().getHandSize() > gameDesc.getMinHandSize());

                game.handsPlayed++;
                //Calculate the score of the hand
                game.calculateScore();

                //
                if (gameDesc.getTrumpPickingMode().equals("predefined")) {
                    game.trumpSuit.replace(0, game.trumpSuit.length(), gameDesc.getTrumpIterator().next());
                }

                //send updated scores when round has ended
                JsonObject roundEndMessage = new JsonObject();
                roundEndMessage.add("type", new JsonPrimitive("gameendmessage"));
                sendTeamScoresJson(game, roundEndMessage);

                //Check if game needs balancing
                if (enableRandomEvents) {
                    rdmEventsManager.checkGameCloseness();
                }

                //Increment dealer for next hand
                dealer = game.nextPlayerIndex.apply(dealer);

                game.printScore();

                game.printScore();
            } while (game.gameEnd());
            //If match scoring is bestOf
            setScoresAndVulnerability(gameDesc, game);
            //send updated scores when game has ended
            JsonObject roundEndMessage = new JsonObject();
            roundEndMessage.add("type", new JsonPrimitive("matchendmessage"));
            sendTeamScoresJson(game, roundEndMessage);

            System.out.println("End of Game");
        } while (game.sessionEnd()); //Check for match/session winning condition to be met

        //If game is specified to be best of set of games, find overall match winner
        findMatchWinner(gameDesc, game);
    }

    private static void findMatchWinner(GameDesc gameDesc, GameEngine game) {
        if (gameDesc.getSessionEnd().equals("bestOf")) {
            //Find the winning team by finding the highest match score
            Team winningTeam = game.getTeams().get(0);
            for (Team team : game.getTeams()) {
                if (team.getCumulativeScore() > winningTeam.getCumulativeScore()) {
                    winningTeam = team;
                }
            }
            System.out.println("Team " + winningTeam.getTeamNumber() + " wins match");
        }
        System.out.println("End of match");
    }

    private static void sendPlayerHands(Player[] playerArray, GameEngine game, Gson gson) {
        System.out.println("SENDING CARDS TO GUI");
        JsonObject jsonPlayers = new JsonObject();
        jsonPlayers.add("type", new JsonPrimitive("playerhands"));
        JsonArray jsonPlayersArray = new JsonArray();
        for (int i = 0; i < playerArray.length; i++) {
            LinkedList<Card> hand = playerArray[i].getHand().getHand();
            JsonObject jsonPlayer = new JsonObject();
            JsonArray jsonCards = new JsonArray();
            //converts hand to JSON array of JSON objects
            for (Card card : hand) {
                jsonCards.add(gson.fromJson(card.getJSON(), JsonObject.class));
            }
            jsonPlayer.add("playerindex", new JsonPrimitive(i));
            jsonPlayer.add("hand", jsonCards);
            jsonPlayersArray.add(jsonPlayer);
        }
        jsonPlayers.add("players", jsonPlayersArray);
        game.webSocket.send(gson.toJson(jsonPlayers));
    }

    private static void sendTeamScoresJson(GameEngine game, JsonObject message) {
        JsonArray scoresArray = new JsonArray();
        for (Team team : game.getTeams()) {
            JsonObject teamJson = new JsonObject();
            teamJson.add("teamnumber", new JsonPrimitive(team.getTeamNumber()));
            teamJson.add("teamscore", new JsonPrimitive(team.getGameScore()));
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
                //Check for a team that has exceeded the score threshold
                for (Team team : teams) {
                    if (team.getGameScore() >= desc.getScoreThreshold()) {
                        return false;
                    }
                }
                return true;
            case "handsPlayed":
                return handsPlayed < desc.getScoreThreshold();
            default:
                return true;
        }
    }

    /**
     * @return flag to signal match/session should end based on game description
     */
    private boolean sessionEnd() {
        if ("bestOf".equals(desc.getSessionEnd())) {
            //Check if a team has the required number of games to win a match
            for (Team team : getTeams()) {
                if (team.getGamesWon() == (desc.getSessionEndValue() / 2) + 1) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the bids from the players
     *
     * @param currentPlayer Player bidding starts at
     * @param players       Array of players
     */
    public void getBids(int currentPlayer, Player[] players) throws InterruptedException {
        System.out.println("-----------------------------------");
        System.out.println("--------------BIDDING--------------");
        System.out.println("-----------------------------------");
        //Copy of initial player used to check when every player has bid once
        int originalCurrentPlayer = currentPlayer;
        //Tracks number of consecutive passes to signal a contract bid has been accepted
        int passCounter = 0;
        //Flag that the first round of bidding is in operation - to check for special cases
        boolean firstRound = true;
        //Tracks the number of first round passes - to make sure a player makes a non-pass on the 4th bid in the first round
        int firstRoundPassCount = 0;
        //Resets the previous highest bid for new bidding round
        adjustedHighestBid = null;
        //Loop until bidding end condition met
        do {
            //Gets a bid from a player - validation done through validBids.java
            do {
                currentBid = players[currentPlayer].makeBid(this.desc.getValidBid(), desc.isTrumpSuitBid(), adjustedHighestBid, firstRound, desc.isCanBidBlind());
                getBidLock.acquire();
            } while (currentBid == null); //currentBid will only be null if it's a GUI player
            Bid bid = currentBid;
            if (bid.isDoubling()) {
                //Reset pass counter, consecutive passes has been broken
                passCounter = 0;
                //If the current highest bid is a double, then the new bid indicates a redouble
                if (getAdjustedHighestBid().isDoubling()) {
                    //Set the current highest bid to a redouble
                    getAdjustedHighestBid().setDoubling(false);
                    getAdjustedHighestBid().setRedoubling(true);
                }
                //Otherwise it's a standard double
                else {
                    getAdjustedHighestBid().setDoubling(true);
                    getAdjustedHighestBid().setRedoubling(false);
                }
                //Update the new highest bid team
                getAdjustedHighestBid().setTeam(players[currentPlayer].getTeam());
            }
            //If it's not a double, it's either a pass or a standard bid
            else {
                //Bid value indicates whether it's a pass or no
                if (bid.getBidValue() >= 0) {
                    //First round is broken
                    firstRound = false;
                    //Pass counter reset
                    passCounter = 0;
                    //Check if it's the first non-pass bid
                    if (getAdjustedHighestBid() == null) {
                        String suit = null;
                        if (desc.isTrumpSuitBid()) {
                            suit = bid.getSuit();
                        }
                        //Create the starting non-pass bid
                        setAdjustedHighestBid(new ContractBid(false, suit, bid.getBidValue(), bid.isBlind(),
                                false, false, players[currentPlayer], players[currentPlayer].getTeam()));
                    }
                    //Standard raising bid
                    else {
                        if (desc.isTrumpSuitBid()) {
                            getAdjustedHighestBid().setSuit(bid.getSuit());
                            //If the suit has been raised, or a different team will now have the highest bid, update the declarer.
                            if (!(getAdjustedHighestBid().getSuit().equals(bid.getSuit())) || getAdjustedHighestBid().getTeam() != players[currentPlayer].getTeam()) {
                                getAdjustedHighestBid().setDeclarer(players[currentPlayer]);
                            }
                        }
                        //Update current highest bid
                        getAdjustedHighestBid().setRedoubling(false);
                        getAdjustedHighestBid().setDoubling(false);
                        getAdjustedHighestBid().setBidValue(bid.getBidValue());
                        getAdjustedHighestBid().setTeam(players[currentPlayer].getTeam());
                    }
                }
                //It's a pass
                else {
                    //If it's a first round pass, check if the bidding has reached all but one player passing
                    // - to force bidding on final player of first round
                    if (firstRound) {
                        firstRoundPassCount++;
                        if (firstRoundPassCount >= players.length - 1) {
                            firstRound = false;
                        }
                    }
                    //Standard pass
                    else {
                        passCounter++;
                    }
                }
            }
            //Update the player's current bid
            players[currentPlayer].setBid(bid);
            //Broadcast the new bit to other players
            broadcastBids(players[currentPlayer].getBid(), currentPlayer, players);
            //Rotate to the next player
            currentPlayer = this.nextPlayerIndex.apply(currentPlayer);
        }
        while (getBiddingEnd(players, currentPlayer, originalCurrentPlayer, passCounter, firstRound)); //Check if bidding end condition met
    }

    //Method for checking if bidding should end
    public boolean getBiddingEnd(Player[] players, int currentPlayer, int originalPlayer, int passCounter, boolean firstRound) {
        //If contract style bidding, bidding ends when all players have passed in a row except for one, except for when it's the first round.
        if (desc.isAscendingBid()) {
            return passCounter != players.length - 1 || firstRound;
        }
        //Otherwise, bidding ends when all players have bid
        else {
            return currentPlayer != originalPlayer;
        }
    }


    /**
     * Distributes cards from the deck starting from the dealer +/- 1
     *
     * @param players     Players to deal cards to
     * @param deck        Deck to deal cards from
     * @param dealerIndex Current dealer index
     */
    public void dealCards(Player[] players, Deck deck, int dealerIndex) {
        //Start dealing to the next player from the dealer
        dealerIndex = this.nextPlayerIndex.apply(dealerIndex);
        int cardsLeft = deck.getDeckSize() - (players.length * handSize);
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
        HashMap<String, Integer> suitMap = generateSuitOrder(desc, trumpSuit, currentTrick.get(0));
        //Get comparator for comparing cards based on the suit ranking
        CardComparator comparator = new CardComparator(suitMap, desc.getRANKORDER());
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
    public static HashMap<String, Integer> generateSuitOrder(GameDesc desc, StringBuilder trumpSuit, Card leadingCard) {
        HashMap<String, Integer> suitMap = new HashMap<>();
        //Set default value for suits
        for (String suit : desc.getSUITS()) {
            suitMap.put(suit, 4);
        }
        //Refine ranking based on how the game chooses the trump
        switch (desc.getTrumpPickingMode()) {
            case "lastDealt"://follows through to 'fixed' case
            case "predefined":
            case "fixed":
                suitMap.put(trumpSuit.toString(), 1);
                if (leadingCard.getSUIT().equals(trumpSuit.toString()))
                    suitMap.put(leadingCard.getSUIT(), 2);
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
            System.out.println(teams.get(i).getGameScore());
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
                player.broadcastBid(bid, playerNumber, getAdjustedHighestBid());
            }
        } else { //Only need to print out network moves to local players
            for (Player player : playerArray) {
                if (player.getClass() == LocalPlayer.class) {
                    player.broadcastBid(bid, playerNumber, getAdjustedHighestBid());
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
                if (player.getClass() == LocalPlayer.class || player.getClass() == POMDPPlayer.class) {
                    player.broadcastPlay(card, playerNumber);
                }
            }
        }
    }

    //Standard getters

    private Predicate<Card> getValidCard() {
        return validCard;
    }

    public ArrayList<Team> getTeams() {
        return teams;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {

        System.out.println("Opened connection");
        this.webSocket = conn;
        GUIConnectLock.release();

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
        System.out.println("GAMEENGINE RECEIVED: " + message);
        switch (request.get("type").getAsString()) {
            case "playcard":
                currentTrick.dropLast();
                currentTrick.getCard(Card.fromJson(request.get("card").getAsString()));
                getCardLock.release();
                break;
            case "makebid":
                JsonObject bidJson = request.getAsJsonObject("bid");
                boolean doubling = bidJson.get("doubling").getAsBoolean();
                String suit = bidJson.get("suit").getAsString();
                int bidValue = bidJson.get("bidValue").getAsInt();
                boolean blind = bidJson.get("blindBid").getAsBoolean();
                boolean vulnerability = bidJson.get("isPlayerVuln").getAsBoolean();
                currentBid = new Bid(doubling, suit, bidValue, blind, vulnerability);
                getBidLock.release();
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
        lock.release();
        System.out.println("Server Started \nwaiting for connection on port: " + getPort() + "...");


    }


}
