package src.rdmEvents;


import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import src.card.Card;
import src.gameEngine.Hand;
import src.parser.GameDesc;
import src.player.GUIPlayer;
import src.player.NetworkPlayer;
import src.player.Player;
import src.team.Team;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Predicate;

//Class that distributes and runs the logic of random events.
public class RdmEventsManager {
    //Defines the maximum score separation between the weakest and strongest team that is allowed before
    //the manager increases the chance of a random event occurring.
    int maxAcceptableScoreSeparation;
    //Probability between 0 and 1 that a random event will be triggered
    double rdmEventProb;
    //Default probability - after a random event is run, probability resets to this
    final double rdmEventProbDEFAULT;
    //Value by which the probability is increased when the maxAcceptableScoreSeparation is breached
    double probIncrement;
    //Flag to indicate if random events are triggered
    boolean enabled;
    //Score threshold as specified in game description
    int scoreThreshold;
    //Stores the weakest (lowest-scoring) and strongest team (highest-scoring)
    Team weakestTeam;
    Team strongestTeam;
    Player rdmWeakestPlayer;
    //Array to choose which special card to insert
    String[] specialCardEvents = {"BOMB", "HEAVEN"};
    //Array to choose which trick event to run
    String[] TRICKEvents = {"SwapCard", "SwapHands"};
    Random rand;
    ArrayList<Team> teams;
    Player[] players;
    GameDesc desc;

    //Constructor to initialise the random event manager
    public RdmEventsManager(GameDesc desc, ArrayList<Team> teams, Random rand, Player[] players, boolean enabled) {
        this.teams = teams;
        this.enabled = enabled;
        this.rand = rand;
        this.players = players;
        this.desc = desc;
        //Set initial weak/strong teams
        setWeakestTeam(teams.get(0));
        setStrongestTeam(teams.get(1));
        rdmWeakestPlayer = weakestTeam.getPlayers()[0];
        scoreThreshold = desc.getScoreThreshold();
        //Set max score separation based on the game desc
        maxAcceptableScoreSeparation = scoreThreshold / 3;
        //Starting probability a random event is run
        rdmEventProbDEFAULT = 0.2;
        rdmEventProb = rdmEventProbDEFAULT;
        probIncrement = 0.3;
    }

    //Method that checks the team scores and raises the random event probability of necessary
    public void checkGameCloseness() {
        int highestScore = 0;
        int lowestScore = scoreThreshold + 1;
        //Find weakest and strongest team
        for (Team team : teams) {
            if (team.getGameScore() < lowestScore) {
                weakestTeam = team;
                lowestScore = team.getGameScore();
            }
            if (team.getGameScore() > lowestScore) {
                strongestTeam = team;
                highestScore = team.getGameScore();
            }
        }
        //Check if allowed score separation is exceeded
        if (highestScore - lowestScore > maxAcceptableScoreSeparation) {
            //Increment the random probability or set to max if necessary
            if ((rdmEventProb += probIncrement) <= 1) {
                rdmEventProb += probIncrement;
            } else {
                rdmEventProb = 1;
            }
        }
        //Otherwise make sure the random probability is reset
        else {
            rdmEventProb = rdmEventProbDEFAULT;
        }
    }

    //Method that decides if a random event should be run, and chooses one if necessary
    public String eventChooser(String eventPlayTime) {
        //If random events are enabled, randomly decide if an event should be run
        if (enabled && rand.nextDouble() <= rdmEventProb) {
            //Choose an appropriate event based on the point in the game
            switch (eventPlayTime) {
                case "TRICK":
                    //Choose the event to be run and return it to the game engine
                    return TRICKEvents[rand.nextInt(TRICKEvents.length)];
                case "HAND":
                    if (rand.nextInt(1) == 1) {
                        //Randomly choose a weak player
                        rdmWeakestPlayer = weakestTeam.getPlayers()[rand.nextInt(weakestTeam.getPlayers().length)];
                        System.out.println("AI playing for Player " + (rdmWeakestPlayer.getPlayerNumber() + 1) + " for this hand");
                        return "AI-TAKEOVER";
                    }
                    else {
                        //Randomly choose a special card to be added
                        return specialCardEvents[rand.nextInt(specialCardEvents.length)];
                    }
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    //Method that runs the logic for a swap cards event
    public boolean runSwapCards() {
        Player[] playerArray = getPlayers();
        //Randomly select a player from the weakest team
        int rdmPlayerIndexFromWeakTeam = getRand().nextInt(getWeakestTeam().getPlayers().length);
        Player weakPlayer = getWeakestTeam().getPlayers()[rdmPlayerIndexFromWeakTeam];
        //Randomly select a player from the strongest team
        int rdmStrongPlayerTeamIndex = getRand().nextInt(getStrongestTeam().getPlayers().length);
        Player rdmStrongPlayer = strongestTeam.getPlayers()[rdmStrongPlayerTeamIndex];
        //Ask the weak player if the want to swap a card with the random player
        Swap swap = weakPlayer.getSwap(rdmStrongPlayer);
        //If the player selected a swap, run the swap logic
        if (swap.getStatus().equals("live")) {
            //Get the original (swapper) player's card
            Card originalPlayerCard = playerArray[swap.getOriginalPlayerIndex()].getHand().giveCard(swap.getOriginalPlayerCardNumber());
            //Get the card to be swapped with
            Card otherPlayerCard = playerArray[swap.getOtherPlayerIndex()].getHand().giveCard(swap.getOtherPlayerCardNumber());
            //Add the other card to the swapper's hand
            playerArray[swap.getOriginalPlayerIndex()].getHand().getCard(otherPlayerCard);
            //Add the swapper's card to the other players hand
            playerArray[swap.getOtherPlayerIndex()].getHand().getCard(originalPlayerCard);
            System.out.println("Swapping " + originalPlayerCard + " from Player " + (swap.getOriginalPlayerIndex() + 1) + " with " +
                    otherPlayerCard + " from Player " + (swap.getOtherPlayerIndex() + 1));
        } else return false;
        //If the swap was made by the local player, notify the network players
        if (!(weakPlayer instanceof NetworkPlayer)) {
            for (Player player : playerArray) {
                player.broadcastSwap(swap);
            }
        }
        return true;
    }

    //Method to run swap hands event logic
    public Pair<Player, Player> runSwapHands() {
        //Randomly select a player from the weakest team
        int rdmPlayerIndexFromWeakTeam = getRand().nextInt(getWeakestTeam().getPlayers().length);
        Player weakPlayer = getWeakestTeam().getPlayers()[rdmPlayerIndexFromWeakTeam];
        //Randomly select a player from the strongest team
        int rdmStrongPlayerTeamIndex = getRand().nextInt(getStrongestTeam().getPlayers().length);
        Player strongPlayer = strongestTeam.getPlayers()[rdmStrongPlayerTeamIndex];

        System.out.println("Swapping hands between Player " + weakPlayer.getPlayerNumber() + " and Player " + strongPlayer.getPlayerNumber());
        //Swap hands and predicates
        Hand tempHand = weakPlayer.getHand();
        Predicate<Card> tempPredicate = weakPlayer.getCanBePlayed();

        weakPlayer.setHand(strongPlayer.getHand());
        weakPlayer.setCanBePlayed(strongPlayer.getCanBePlayed());

        strongPlayer.setHand(tempHand);
        strongPlayer.setCanBePlayed(tempPredicate);
        ImmutablePair<Player,Player> swappedPlayers = new ImmutablePair<>(weakPlayer,strongPlayer);
        return swappedPlayers;
    }

    //Method for running logic of score changes from playing a special card
    public void runSpecialCardOps(String cardType, int currentPlayer) {
        //Get the team that played the card
        Team affectedTeam = getPlayers()[currentPlayer].getTeam();
        //Change the score my an appropriate amount
        int scoreChange = scoreThreshold / 4;
        //Indicate which card has been played
        if (cardType.equals("BOMB")) {
            System.out.println("Player " + currentPlayer + " played a BOMB card: " + scoreChange + " deducted from their teams score");
            scoreChange *= (-1);
        } else {
            System.out.println("Player " + currentPlayer + "played a HEAVEN card: " + scoreChange + " added to their teams score");
        }
        System.out.println("Changing score of team " + affectedTeam.getTeamNumber());
        //Change score of team
        affectedTeam.setGameScore(affectedTeam.getGameScore() + scoreChange);
    }

    //Method for inserting a special card into the deck
    public void runSpecialCardSetup(String cardType) {
        Player[] playerArray = getPlayers();
        //Select a random card from a player and set it to the given special type
        int rdmPlayerIndex = getRand().nextInt(playerArray.length);
        int rdmCardIndex = getRand().nextInt(desc.getHandSize());
        System.out.println("CHANGED CARD" + playerArray[rdmPlayerIndex].getHand().get(rdmCardIndex));
        playerArray[rdmPlayerIndex].getHand().get(rdmCardIndex).setSpecialType(cardType);
    }

    //Standard getters and setters
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

    public Player getRdmWeakestPlayer() {
        return rdmWeakestPlayer;
    }
}
