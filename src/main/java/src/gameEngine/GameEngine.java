package src.gameEngine;

import src.bid.Bid;
import src.bid.ContractBid;
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
import src.player.POMDPPlayer;
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
        this.trickHistory = new LinkedList<>();
        this.trumpSuitBid = desc.isTrumpSuitBid();
    }

    public static void main(GameDesc gameDesc, int dealer, Player[] playerArray, int seed, boolean printMoves, boolean enableRandomEvents) {
        GameEngine game = new GameEngine(gameDesc);
        Random rand = new Random(seed);

        assert playerArray.length == gameDesc.getNUMBEROFPLAYERS(); //TODO remove

        /* initialize each players hands */

        for (Player player : playerArray) {
            player.initPlayer(game.getValidCard(), gameDesc, game.trumpSuit);
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
            //Signify to players that a new hand has started.
            for (Player player : playerArray) {
                player.startHand(game.trumpSuit);
            }

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
                    Bid bid = new Bid(false, null, teamBid, false, false);
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
    public void getBids(int currentPlayer, Player[] players, GameDesc desc) {
        System.out.println("-----------------------------------");
        System.out.println("--------------BIDDING--------------");
        System.out.println("-----------------------------------");
        int originalCurrentPlayer = currentPlayer;
        int passCounter = 0;
        do {
            //Adds the bids (checks they are valid in other class)
            Bid bid = players[currentPlayer].makeBid(this.desc.getValidBid(), trumpSuitBid, adjustedHighestBid);
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

    public boolean getBiddingEnd(Player[] players, int currentPlayer, int originalPlayer, int passCounter, GameDesc desc) {
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
        HashMap<String, Integer> suitMap = generateSuitOrder(desc, trumpSuit, currentTrick.get(0));
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
    public static HashMap<String, Integer> generateSuitOrder(GameDesc desc, StringBuilder trumpSuit, Card leadingCard) {
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
                if (player.getClass() == LocalPlayer.class || player.getClass() == POMDPPlayer.class) {
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

    public ContractBid getAdjustedHighestBid() {
        return adjustedHighestBid;
    }

    public void setAdjustedHighestBid(ContractBid adjustedHighestBid) {
        this.adjustedHighestBid = adjustedHighestBid;
    }
}
