package src.parser;

import java.util.LinkedList;

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
                    String calculateScore,
                    String trumpPickingMode,
                    String trumpSuit,
                    String leadingCardForEachTrick,
                    String gameEnd,
                    Integer scoreThreshold,
                    Integer trickThreshold) {
        this.NUMBEROFPLAYERS = numOfPlayers;
        this.teams = teams;
        this.SEED = seed;
        this.SUITS = suits;
        this.RANKS = ranks;
        this.DECK = Deck.makeDeck(suits, ranks);
        this.RANKORDER = rankOrder;
        this.DEALCARDSCLOCKWISE = dealOrderClockwise;
        this.calculateScore = calculateScore;
        this.trumpPickingMode = trumpPickingMode;
        if (trumpPickingMode.equals("fixed")) this.trumpSuit = trumpSuit;
        this.leadingCardForEachTrick = leadingCardForEachTrick;
        this.gameEnd = gameEnd;
        this.scoreThreshold = scoreThreshold; // regardless of whether its score or number of hands played, this variable is used for the comparison
        if (calculateScore.equals("tricksWon")) this.trickThreshold = trickThreshold;
    }

    public int getNUMBEROFPLAYERS() {
        return NUMBEROFPLAYERS;
    }

    public long getSEED() {
        return SEED;
    }

    public LinkedList<Card> getDECK() {
        return DECK;
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
}
