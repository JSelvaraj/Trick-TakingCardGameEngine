package src.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import org.json.JSONObject;
import src.card.Card;
import src.exceptions.InvalidBidException;
import src.exceptions.InvalidPlayerMoveException;
import src.gameEngine.Bid;
import src.gameEngine.ContractBid;
import src.gameEngine.Hand;
import src.gameEngine.PotentialBid;
import src.rdmEvents.Swap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class NetworkPlayer extends Player {

    private Socket playerSocket;
    private JsonStreamParser reader;

    public NetworkPlayer(int playerNumber, Predicate<Card> validCard) {
        super(playerNumber, validCard);
    }

    public NetworkPlayer(int playerNumber, Socket playerSocket) {
        super(playerNumber);
        this.playerSocket = playerSocket;
        try {
            this.reader = new JsonStreamParser((new InputStreamReader(playerSocket.getInputStream())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Card playCard(String trumpSuit, Hand currentTrick) { // when you're receiving a card
        StringBuilder message = new StringBuilder();
        JsonElement msg = null;
        msg = reader.next();
        JSONObject cardEvent = new JSONObject(msg.getAsJsonObject().toString()); //TODO catch exceptions
        String type = cardEvent.getString("type");
        if (!type.equals("play")) {
            System.out.println(type);
            throw new InvalidPlayerMoveException();
        }
        String suit = cardEvent.getString("suit");
        String rank = cardEvent.getString("rank");
        Card card = new Card(suit, rank);
        //Checks if card is valid
        if (!super.getCanBePlayed().test(card)) {
            System.out.println(this.getHand().toString());
            System.out.println(card.toString());
            throw new InvalidPlayerMoveException();
        }
        return super.getHand().giveCard(card);
    }

    @Override
    public void broadcastPlay(Card card, int playerNumber) {
        //Creates the json object to be sent.
        JSONObject json = new JSONObject();
        json.put("type", "play");
        json.put("suit", card.getSUIT());
        json.put("rank", card.getRANK());
        json.put("playerIndex", playerNumber);
        //Sends the json object over the socket.
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream(), StandardCharsets.UTF_8));
            out.write(json.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void broadcastSwap(Swap swap) {
        //Creates the json object to be sent.
        JSONObject json = new JSONObject();
        json.put("type", "swap");
        json.put("currentPlayer", swap.getOriginalPlayerIndex());
        json.put("currentPlayerCardNumber", swap.getOriginalPlayerCardNumber());
        json.put("rdmPlayerIndex", swap.getRdmPlayerIndex());
        json.put("rdmPlayerCardNumber", swap.getRdmPlayerCardNumber());
        json.put("status", swap.getStatus());
        System.out.println("Broadcasting swap to Player " + getPlayerNumber());
        //Sends the json object over the socket.
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream(), StandardCharsets.UTF_8));
            out.write(json.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Swap getSwap(Player strongPlayer) {
        JsonElement msg = null;
        msg = reader.next();
        JSONObject swapEvent = new JSONObject(msg.getAsJsonObject().toString()); //TODO catch exceptions
        String type = swapEvent.getString("type");
        if (!type.equals("swap")) {
            throw new InvalidPlayerMoveException();
        }
        System.out.println("Player " + getPlayerNumber() + " received swap event");
        return new Swap(swapEvent.getInt("currentPlayer"), swapEvent.getInt("currentPlayerCardNumber"),
                swapEvent.getInt("rdmPlayerIndex"), swapEvent.getInt("rdmPlayerCardNumber"), swapEvent.getString("status"));
    }

    public Socket getPlayerSocket() {
        return playerSocket;
    }

    @Override
    public void broadcastBid(Bid bid, int playerNumber) {
        JSONObject json = new JSONObject();
        json.put("type", "bid");
        json.put("doubling", bid.isDoubling());
        if (bid.getSuit() != null) {
            json.put("suit", bid.getSuit());
        }
        json.put("value", bid.getBidValue());
        json.put("blindBid", bid.isBlind());
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream(), StandardCharsets.UTF_8));
            out.write(json.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Bid makeBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, Player[] players, ContractBid adjustedHighestBid) {
        JsonElement msg = null;
        try {
            JsonStreamParser reader = new JsonStreamParser(new InputStreamReader(playerSocket.getInputStream()));
            msg = reader.next();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        JSONObject bidEvent = new JSONObject(msg.getAsJsonObject().toString());
        String type = bidEvent.getString("type");
        if (!type.equals("bid")) {
            throw new InvalidPlayerMoveException();
        }
        Bid bid;
        String value;
        String suit = null;
        boolean blind;
        boolean doubling = bidEvent.optBoolean("doubling", false);
        if (doubling) {
            bid =  new Bid(true, null,0,false);
            value = "d";
        }
        else {
            suit = bidEvent.optString("suit", null);
            int valueInt = bidEvent.getInt("value");
            blind = bidEvent.optBoolean("blindBid", false);
            bid = new Bid(false,suit,valueInt,blind);
            value = Integer.toString(valueInt);
        }
        if (!validBid.test(new PotentialBid(suit, value, this.getPlayerNumber(), players, adjustedHighestBid))) {
            throw new InvalidBidException();
        }
        return bid;
    }
}
