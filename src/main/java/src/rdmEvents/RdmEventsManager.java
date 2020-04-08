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
    final double getRdmEventProbDEFAULT = 0.5;
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
        //For debugging
        rdmEventProb = 0.99;
        setWeakestTeam(teams.get(0));
        setStrongestTeam(teams.get(1));
        if (desc.getGameEnd().equals("scoreThreshold")) {
            scoreThreshold = desc.getScoreThreshold();
            maxAcceptableScoreSeparation = desc.getScoreThreshold() / 3;
        } else {

        }
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
            } else {
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
            System.out.println("Swapping " + originalPlayerCard + " from Player " + (swap.getOriginalPlayerIndex() + 1) + " with " +
                    otherPlayerCard + " from Player " + (swap.getRdmPlayerIndex() + 1));
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
                } else {
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
