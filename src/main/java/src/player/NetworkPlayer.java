package src.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import org.json.JSONObject;
import src.card.Card;
import src.exceptions.InvalidBidException;
import src.exceptions.InvalidPlayerMoveException;
import src.bid.Bid;
import src.bid.ContractBid;
import src.gameEngine.Hand;
import src.bid.PotentialBid;
import src.rdmEvents.Swap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
    public Card playCard(String trumpSuit, Hand currentTrick) {
        // when you're receiving a card
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

    //Send a card swap event to other players over network
    @Override
    public void broadcastSwap(Swap swap) {
        //Creates the json object to be sent.
        JSONObject json = new JSONObject();
        //Indicates it's a swap event
        json.put("type", "swap");
        //Puts the relevant info
        json.put("currentPlayer", swap.getOriginalPlayerIndex());
        json.put("currentPlayerCardNumber", swap.getOriginalPlayerCardNumber());
        json.put("rdmPlayerIndex", swap.getOtherPlayerIndex());
        json.put("rdmPlayerCardNumber", swap.getOtherPlayerCardNumber());
        json.put("status", swap.getStatus());
        //Sends the json object over the socket.
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream(), StandardCharsets.UTF_8));
            out.write(json.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Reads in a swap from the network
    @Override
    public Swap getSwap(Player strongPlayer) {
        JsonElement msg = null;
        msg = reader.next();
        JSONObject swapEvent = new JSONObject(msg.getAsJsonObject().toString());
        String type = swapEvent.getString("type");
        //Checks it's a swap event
        if (!type.equals("swap")) {
            throw new InvalidPlayerMoveException();
        }
        //Returns swap for logic to be performed
        return new Swap(swapEvent.getInt("currentPlayer"), swapEvent.getInt("currentPlayerCardNumber"),
                swapEvent.getInt("rdmPlayerIndex"), swapEvent.getInt("rdmPlayerCardNumber"), swapEvent.getString("status"));
    }

    public Socket getPlayerSocket() {
        return playerSocket;
    }

    @Override
    public void broadcastBid(Bid bid, int playerNumber, ContractBid adjustedHighestBid) {
        JSONObject json = new JSONObject();
        json.put("type", "bid");
        json.put("doubling", bid.isDoubling());
        if (bid.getSuit() != null) {
            if (bid.getSuit().equals("NO TRUMP")) {
                json.put("suit", JSONObject.NULL);
            } else {
                json.put("suit", bid.getSuit());
            }
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
    public Bid makeBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, boolean firstRound, boolean canBidBlind) {
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
            bid = new Bid(true, null, 0, false, false);
            value = "d";
        } else {
            int valueInt = bidEvent.getInt("value");
            blind = bidEvent.optBoolean("blindBid", false);
            if (valueInt >= 0 && (trumpSuitBid || bidEvent.has("suit"))) {
                suit = bidEvent.optString("suit", null);
                if (suit == null || suit.equals("null")) {
                    suit = "NO TRUMP";
                } else {
                    suit = bidEvent.getString("suit");
                }
            }
            bid = new Bid(false, suit, valueInt, blind, false);
            value = Integer.toString(valueInt);
        }
        if (!validBid.test(new PotentialBid(suit, value, adjustedHighestBid, this, firstRound))) {
            throw new InvalidBidException();
        }
        return bid;
    }
}
