package src.rdmEvents;

import src.card.Card;
import src.gameEngine.Hand;
import src.parser.GameDesc;
import src.player.LocalPlayer;
import src.player.NetworkPlayer;
import src.player.Player;
import src.team.Team;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Predicate;

public class RdmEventsManager {

    int maxAcceptableScoreSeparation;
    int scoreThreshold;
    double rdmEventProb;
    final double rdmEventProbDEFAULT = 0.4;
    double probIncrement = 0.3;
    ArrayList<Team> teams;
    Team weakestTeam;
    Team strongestTeam;
    boolean enabled;
    Random rand;
    Player[] players;
    String[] specialCardEvents = {"BOMB", "HEAVEN"};
    String[] TRICKEvents = {"SwapCard", "SwapHands"};
    GameDesc desc;

    public RdmEventsManager(GameDesc desc, ArrayList<Team> teams, Random rand, Player[] players, boolean enabled) {
        this.teams = teams;
        this.enabled = enabled;
        this.rand = rand;
        this.players = players;
        this.desc = desc;
        setup();
    }

    public void setup() {
        rdmEventProb = rdmEventProbDEFAULT;
        setWeakestTeam(teams.get(0));
        setStrongestTeam(teams.get(1));
        scoreThreshold = desc.getScoreThreshold();
        maxAcceptableScoreSeparation = scoreThreshold / 3;
    }


    public void checkGameCloseness() {
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
            } else {
                rdmEventProb = 1;
            }
        }
    }

    public RdmEvent eventChooser(String eventPlayTime) {
        if (enabled && rand.nextDouble() < rdmEventProb) {
            switch (eventPlayTime) {
                case "TRICK":
                    rdmEventProb = rdmEventProbDEFAULT;
                    return new RdmEvent(TRICKEvents[rand.nextInt(TRICKEvents.length)]);
                case "SPECIAL-CARD":
                    rdmEventProb = rdmEventProbDEFAULT;
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
            System.out.println("Swapping " + originalPlayerCard + " from Player " + (swap.getOriginalPlayerIndex() + 1) + " with " +
                    otherPlayerCard + " from Player " + (swap.getRdmPlayerIndex() + 1));
        }
        if (!(weakPlayer instanceof NetworkPlayer)) {
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
        Team affectedTeam = getPlayers()[currentPlayer].getTeam();
        int scoreChange = 10;
        if (cardType.equals("BOMB")) {
            System.out.println("Player " + currentPlayer + " played a BOMB card: " + scoreChange + " deducted from their teams score");
            scoreChange *= (-1);
        } else {
            System.out.println("Player " + currentPlayer + "played a HEAVEN card: " + scoreChange + " added to their teams score");
        }
        System.out.println("Changing score of team " + affectedTeam.getTeamNumber());
        affectedTeam.setScore(Math.max((affectedTeam.getScore() + scoreChange), 0));
    }

    public void runSpecialCardSetup(RdmEvent rdmEventHAND) {
        Player[] playerArray = getPlayers();
        int rdmPlayerIndex = getRand().nextInt(playerArray.length);
        int rdmCardIndex = getRand().nextInt(desc.getHandSize());
        playerArray[rdmPlayerIndex].getHand().get(rdmCardIndex).setSpecialType(rdmEventHAND.getName());
        System.out.println(playerArray[rdmPlayerIndex].getHand().get(rdmCardIndex) + " is a " + rdmEventHAND.getName() + " special card.");
    }

    public int getMaxAcceptableScoreSeparation() {
        return maxAcceptableScoreSeparation;
    }

    public void setMaxAcceptableScoreSeparation(int maxAcceptableScoreSeparation) {
        this.maxAcceptableScoreSeparation = maxAcceptableScoreSeparation;
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

    public Random getRand() {
        return rand;
    }

    public Player[] getPlayers() {
        return players;
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }

}
