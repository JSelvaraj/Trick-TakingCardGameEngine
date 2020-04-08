package src.rdmEvents;

import src.card.Card;
import src.gameEngine.Hand;
import src.parser.GameDesc;
import src.player.LocalPlayer;
import src.player.Player;
import src.team.Team;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Predicate;

public class RdmEventsManager {

    int maxAcceptableScoreSeparation;
    int scoreThreshold;
    double rdmEventProb;
    double getRdmEventProbDEFAULT;
    double probIncrement = 0.2;
    Team weakestTeam;
    Team strongestTeam;
    boolean enabled;
    Random rand;
    Player[] players;
    String[] specialCardEvents = {"BOMB", "HEAVEN"};
    String[] TRICKEvents = {"SwapCard", "SwapHands"};
    GameDesc desc;

    public RdmEventsManager(GameDesc desc, double rdmEventProb,
                            Team initialTeam1, Team initialTeam2, Random rand, Player[] players, boolean enabled) {
        this.maxAcceptableScoreSeparation = 10;
        this.scoreThreshold = 10;
        this.rdmEventProb = rdmEventProb;
        this.getRdmEventProbDEFAULT = rdmEventProb;
        this.weakestTeam = initialTeam1;
        this.strongestTeam = initialTeam2;
        this.enabled = enabled;
        this.rand = rand;
        this.players = players;
        this.desc = desc;
    }

    public void checkGameCloseness(ArrayList<Team> teams) {
        int highestScore = 0;
        int lowestScore = scoreThreshold + 1;

        for (Team team : teams) {
            if (team.getScore() < lowestScore) {
                weakestTeam = team;
                lowestScore = team.getScore();
            }
            if (team.getScore() > lowestScore) {
                strongestTeam = team;
                highestScore = team.getScore();
            }
        }

        if (highestScore - lowestScore > maxAcceptableScoreSeparation) {
            System.out.println("Balancing needed");
            if ((rdmEventProb += probIncrement) <= 1) {
                rdmEventProb += probIncrement;
            }
            else {
                rdmEventProb = 1;
            }
        }
    }

    public RdmEvent eventChooser(String eventPlayTime) {
        if (enabled && rand.nextDouble() < rdmEventProb) {
            switch (eventPlayTime) {
                case "TRICK":
                    rdmEventProb = getRdmEventProbDEFAULT;
                    return new RdmEvent(TRICKEvents[rand.nextInt(TRICKEvents.length)]);
                case "HAND":
                    rdmEventProb = getRdmEventProbDEFAULT;
                    return new RdmEvent(specialCardEvents[rand.nextInt(specialCardEvents.length)]);
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public void runSwapCards() {
        Player[] playerArray = getPlayers();
        int rdmPlayerIndexFrTeam = getRand().nextInt(getWeakestTeam().getPlayers().length);
        Player weakPlayer = getWeakestTeam().getPlayers()[rdmPlayerIndexFrTeam];

        int rdmStrongPlayerTeamIndex = getRand().nextInt(getStrongestTeam().getPlayers().length);
        Player rdmStrongPlayer = strongestTeam.getPlayers()[rdmStrongPlayerTeamIndex];

        Swap swap = weakPlayer.getSwap(rdmStrongPlayer);
        if (swap.getStatus().equals("live")) {
            Card originalPlayerCard = playerArray[swap.getOriginalPlayerIndex()].getHand().giveCard(swap.getOriginalPlayerCardNumber());
            Card otherPlayerCard = playerArray[swap.getRdmPlayerIndex()].getHand().giveCard(swap.getRdmPlayerCardNumber());
            playerArray[swap.getOriginalPlayerIndex()].getHand().getCard(otherPlayerCard);
            playerArray[swap.getRdmPlayerIndex()].getHand().getCard(originalPlayerCard);
            System.out.println("Swapping " + originalPlayerCard + " from Player " + swap.getOriginalPlayerIndex() + " with " +
                    otherPlayerCard + " from Player " + swap.getRdmPlayerIndex());
        }
        if (weakPlayer instanceof LocalPlayer) {
            for (Player player : playerArray) {
                player.broadcastSwap(swap);
            }
        }
    }

    public void runSwapHands() {
        Team weakestTeam = getWeakestTeam();
        Team strongestTeam = getStrongestTeam();

        Player weakPlayer = weakestTeam.getPlayers()[0];
        Player strongPlayer = strongestTeam.getPlayers()[0];

        System.out.println("Swapping hands between Player " + weakPlayer.getPlayerNumber() + " and Player " + strongPlayer.getPlayerNumber());

        Hand tempHand = weakPlayer.getHand();
        Predicate<Card> tempPredicate = weakPlayer.getCanBePlayed();

        weakPlayer.setHand(strongPlayer.getHand());
        weakPlayer.setCanBePlayed(strongPlayer.getCanBePlayed());
        strongPlayer.setHand(tempHand);
        strongPlayer.setCanBePlayed(tempPredicate);
    }

    public void runSpecialCardOps(String cardType, int currentPlayer, ArrayList<Team> teams) {
        for (Team team : teams) {
            if (team.findPlayer(currentPlayer)) {
                int scoreChange = 10;
                if (cardType.equals("BOMB")) {
                    scoreChange *= (-1);
                    if (getPlayers()[currentPlayer] instanceof LocalPlayer) {
                        System.out.println("You played a BOMB card: " + scoreChange + " deducted from your score");
                    }
                }
                else {
                    if (getPlayers()[currentPlayer] instanceof LocalPlayer) {
                        System.out.println("You played a HEAVEN card: " + scoreChange + " added to your score");
                    }
                }
                if (getPlayers()[currentPlayer] instanceof LocalPlayer) {
                    System.out.println("Changing score of team " + team.getTeamNumber());
                }
                team.setScore(Math.max((team.getScore() + scoreChange), 0));
                break;
            }
        }
    }

    public void runSpecialCardSetup(RdmEvent rdmEventHAND) {
        Player[] playerArray = getPlayers();
        int rdmPlayerIndex = getRand().nextInt(playerArray.length);
        int rdmCardIndex = getRand().nextInt(desc.getHandSize());
        playerArray[rdmPlayerIndex].getHand().get(rdmCardIndex).setSpecialType(rdmEventHAND.getName());
        System.out.println(playerArray[rdmPlayerIndex].getHand().get(rdmCardIndex) +  " is a " + rdmEventHAND.getName() + " special card.");
    }

    public int getMaxAcceptableScoreSeparation() {
        return maxAcceptableScoreSeparation;
    }

    public void setMaxAcceptableScoreSeparation(int maxAcceptableScoreSeparation) {
        this.maxAcceptableScoreSeparation = maxAcceptableScoreSeparation;
    }

    public int getScoreThreshold() {
        return scoreThreshold;
    }

    public void setScoreThreshold(int scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    public double getRdmEventProb() {
        return rdmEventProb;
    }

    public void setRdmEventProb(double rdmEventProb) {
        this.rdmEventProb = rdmEventProb;
    }

    public double getGetRdmEventProbDEFAULT() {
        return getRdmEventProbDEFAULT;
    }

    public void setGetRdmEventProbDEFAULT(double getRdmEventProbDEFAULT) {
        this.getRdmEventProbDEFAULT = getRdmEventProbDEFAULT;
    }

    public double getProbIncrement() {
        return probIncrement;
    }

    public void setProbIncrement(double probIncrement) {
        this.probIncrement = probIncrement;
    }

    public Team getWeakestTeam() {
        return weakestTeam;
    }

    public void setWeakestTeam(Team weakestTeam) {
        this.weakestTeam = weakestTeam;
    }

    public Team getStrongestTeam() {
        return strongestTeam;
    }

    public void setStrongestTeam(Team strongestTeam) {
        this.strongestTeam = strongestTeam;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Random getRand() {
        return rand;
    }

    public void setRand(Random rand) {
        this.rand = rand;
    }

    public Player[] getPlayers() {
        return players;
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }


    public GameDesc getDesc() {
        return desc;
    }

    public void setDesc(GameDesc desc) {
        this.desc = desc;
    }
}
