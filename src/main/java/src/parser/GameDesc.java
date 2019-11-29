package src.parser;
import src.card.*;
import src.deck.Deck;
import src.gameEngine.Bid;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class GameDesc {

    private final int NUMBEROFPLAYERS;
    private final int[][] teams;
    private final long SEED;
    private final String[] SUITS;
    private final String[] RANKS;
    private final LinkedList<Card> DECK;
    private final String[] RANKORDER;
    private boolean DEALCARDSCLOCKWISE; //bool because cards can only be dealt clockwise or anti-clockwise
    private final String calculateScore;
    private final String trumpPickingMode;
    private String trumpSuit;
    private final String leadingCardForEachTrick;
    private final String gameEnd;
    private final Integer scoreThreshold;
    private Integer trickThreshold;
    private final String nextLegalCardMode;
    private final String trickWinner;
    private final String trickLeader;
    //Bidding functions
    private boolean bidding;
    private IntPredicate validBid;
    private BiFunction<Bid, Integer, Integer> evaluateBid;
    private int minHandSize;
    private int initialHandSize;


    /**
     *
     * @param numOfPlayers      The number of Players in the game
     * @param seed              The seed value for the random shuffle
     * @param suits             An array of the suits in the game in the order the deck is going to be initially made
     * @param ranks             An array of the ranks of cards in the game in the initial order of the deck
     * @param trumpPickingMode  How the trump it is picked
     */
    public GameDesc(int numOfPlayers,
                    int[][] teams,
                    long seed,
                    String[] suits,
                    String[] ranks,
                    String[] rankOrder,
                    boolean dealOrderClockwise,
                    int minHandSize,
                    int initialHandSize,
                    String calculateScore,
                    String trumpPickingMode,
                    String trumpSuit,
                    String leadingCardForEachTrick,
                    String gameEnd,
                    Integer scoreThreshold,
                    Integer trickThreshold,
                    String nextLegalCardMode,
                    String trickWinner,
                    String trickLeader) {
        this.NUMBEROFPLAYERS = numOfPlayers;
        this.teams = teams;
        this.SEED = seed;
        this.SUITS = suits;
        this.RANKS = ranks;
        this.DECK = Deck.makeDeck(suits, ranks);
        this.RANKORDER = rankOrder;
        this.DEALCARDSCLOCKWISE = dealOrderClockwise;
        this.initialHandSize = initialHandSize;
        this.minHandSize = minHandSize;
        this.calculateScore = calculateScore;
        this.trumpPickingMode = trumpPickingMode;
        if (trumpPickingMode.equals("fixed")) this.trumpSuit = trumpSuit;
        this.leadingCardForEachTrick = leadingCardForEachTrick;
        this.gameEnd = gameEnd;
        this.scoreThreshold = scoreThreshold; // regardless of whether its score or number of hands played, this variable is used for the comparison
        if (calculateScore.equals("tricksWon")) this.trickThreshold = trickThreshold;
        this.nextLegalCardMode = nextLegalCardMode;
        this.trickWinner = trickWinner;
        this.trickLeader = trickLeader;
    }

    @Override
    public String toString() {
        return "GameDesc{" +
                "\nNUMBEROFPLAYERS=" + NUMBEROFPLAYERS +
                "\nteams=" + Arrays.toString(teams) +
                "\nSEED=" + SEED +
                "\nSUITS=" + Arrays.toString(SUITS) +
                "\nRANKS=" + Arrays.toString(RANKS) +
                "\nDECK=" + DECK +
                "\nRANKORDER=" + Arrays.toString(RANKORDER) +
                "\nDEALCARDSCLOCKWISE=" + DEALCARDSCLOCKWISE +
                "\ncalculateScore='" + calculateScore + '\'' +
                "\ntrumpPickingMode='" + trumpPickingMode + '\'' +
                "\ntrumpSuit='" + trumpSuit + '\'' +
                "\nleadingCardForEachTrick='" + leadingCardForEachTrick + '\'' +
                "\ngameEnd='" + gameEnd + '\'' +
                "\nscoreThreshold=" + scoreThreshold +
                "\ntrickThreshold=" + trickThreshold +
                "\nnextLegalCardMode=" + nextLegalCardMode +
                "\ntrickWinner=" + trickWinner +
                "\ntrickLeader=" + trickLeader +
                '}';
    }

    public int getNUMBEROFPLAYERS() {
        return NUMBEROFPLAYERS;
    }

    public long getSEED() {
        return SEED;
    }

    public LinkedList<Card> getDECK() {
        return Deck.makeDeck(this.SUITS, this.RANKS);
    }

    public boolean isDEALCARDSCLOCKWISE() {
        return DEALCARDSCLOCKWISE;
    }

    public String getTrumpPickingMode() {
        return trumpPickingMode;
    }

    public String getLeadingCardForEachTrick() {
        return leadingCardForEachTrick;
    }

    public String[] getRANKS() {
        return RANKS;
    }

    public String[] getSUITS() {
        return SUITS;
    }

    public String getTrumpSuit() {
        return trumpSuit;
    }

    public String getGameEnd() {
        return gameEnd;
    }

    public int getScoreThreshold() {
        return scoreThreshold;
    }

    public int[][] getTeams() {
        return teams;
    }

    public int getTrickThreshold() {
        return trickThreshold;
    }

    public String getCalculateScore() {
        return calculateScore;
    }

    public String[] getRANKORDER() {
        return RANKORDER;
    }

    public IntPredicate getValidBid() {
        return validBid;
    }

    public void setValidBid(IntPredicate validBid) {
        this.validBid = validBid;
    }

    public BiFunction<Bid, Integer, Integer> getEvaluateBid() {
        return evaluateBid;
    }

    public void setEvaluateBid(BiFunction<Bid, Integer, Integer> evaluateBid) {
        this.evaluateBid = evaluateBid;
    }

    public boolean isBidding() {
        return bidding;
    }

    public void setBidding(boolean bidding) {
        this.bidding = bidding;
    }

    public int getMinHandSize() {
        return minHandSize;
    }

    public int getInitialHandSize() {
        return initialHandSize;
    }
}
