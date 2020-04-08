package src.rdmEvents;

import javafx.scene.control.RadioButton;
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
    Team weakestTeam = null;
    Team strongestTeam = null;
    boolean enabled;
    Random rand;
    Player[] players;
    String[] specialCardTypes = {"BOMB", "HEAVEN"};
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
            if ((rdmEventProb += probIncrement) > 0) {
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
                    return new RdmEvent("SwapCard", weakestTeam, strongestTeam, rand, players);
                    //return new RdmEvent("SwapHands", weakestTeam, strongestTeam);
                case "HAND":
                    rdmEventProb = getRdmEventProbDEFAULT;
                    return new RdmEvent(specialCardTypes[rand.nextInt(specialCardTypes.length)], rand, players);
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public void runSwapCards(RdmEvent rdmEventTRICK) {
        Player[] playerArray = rdmEventTRICK.getPlayers();
        int rdmPlayerIndexFrTeam = rdmEventTRICK.getRand().nextInt(rdmEventTRICK.getWeakestTeam().getPlayers().length);
        Player rdmPlayer = rdmEventTRICK.getWeakestTeam().getPlayers()[rdmPlayerIndexFrTeam];
        int rdmPlayerIndex = rdmPlayer.getPlayerNumber();
        rdmEventTRICK.setOriginalPlayer(rdmPlayerIndex);
        rdmEventTRICK.setPlayers(playerArray);
        Swap swap = playerArray[rdmPlayerIndex].getSwap(rdmEventTRICK);
        if (swap.getStatus().equals("live")) {
            Card originalPlayerCard = playerArray[swap.getOriginalPlayer()].getHand().giveCard(swap.getOriginalPlayerCardNumber());
            Card otherPlayerCard = playerArray[swap.getRdmPlayerIndex()].getHand().giveCard(swap.getRdmPlayerCardNumber());
            playerArray[swap.getOriginalPlayer()].getHand().getCard(otherPlayerCard);
            playerArray[swap.getRdmPlayerIndex()].getHand().getCard(originalPlayerCard);
        }
        if (rdmPlayer instanceof LocalPlayer) {
            for (Player player : playerArray) {
                player.broadcastSwap(swap);
            }
        }
    }

    public void runSwapHands(RdmEvent rdmEvent) {
        Team weakestTeam = rdmEvent.getWeakestTeam();
        Team strongestTeam = rdmEvent.getStrongestTeam();

        Player weakPlayer = weakestTeam.getPlayers()[0];
        Player strongPlayer = strongestTeam.getPlayers()[0];

        Hand tempHand = weakPlayer.getHand();
        Predicate<Card> tempPredicate = weakPlayer.getCanBePlayed();

        weakPlayer.setHand(strongPlayer.getHand());
        weakPlayer.setCanBePlayed(strongPlayer.getCanBePlayed());
        strongPlayer.setHand(tempHand);
        strongPlayer.setCanBePlayed(tempPredicate);
    }

    public void runSpecialCardOps(String cardType, int currentPlayer, ArrayList<Team> teams, Player[] players) {
        for (Team team : teams) {
            if (team.findPlayer(currentPlayer)) {
                int scoreChange = 10;
                if (cardType.equals("BOMB")) {
                    scoreChange *= (-1);
                    if (players[currentPlayer] instanceof LocalPlayer) {
                        System.out.println("You played a BOMB card: " + scoreChange + " deducted from your score");
                    }
                }
                else {
                    if (players[currentPlayer] instanceof LocalPlayer) {
                        System.out.println("You played a HEAVEN card: " + scoreChange + " added to your score");
                    }
                }
                if (players[currentPlayer] instanceof LocalPlayer) {
                    System.out.println("Changing score of team " + team.getTeamNumber());
                }
                team.setScore(Math.max((team.getScore() + scoreChange), 0));
                break;
            }
        }
    }

    public void runSpecialCardSetup(RdmEvent rdmEventHAND) {
        Player[] playerArray = rdmEventHAND.getPlayers();
        System.out.println("Adding special card type " + rdmEventHAND.getName() + " to deck");
        int rdmPlayerIndex = rdmEventHAND.getRand().nextInt(playerArray.length);
        int rdmCardIndex = rdmEventHAND.getRand().nextInt(desc.getHandSize());
        playerArray[rdmPlayerIndex].getHand().get(rdmCardIndex).setSpecialType(rdmEventHAND.getName());
        System.out.println(playerArray[rdmPlayerIndex].getHand().get(rdmCardIndex));
    }

}
