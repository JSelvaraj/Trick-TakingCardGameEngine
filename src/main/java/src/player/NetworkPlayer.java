package src.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import org.json.JSONObject;
import src.card.Card;
import src.exceptions.InvalidBidException;
import src.exceptions.InvalidPlayerMoveException;
import src.gameEngine.Bid;
import src.gameEngine.Hand;
import src.rdmEvents.RdmEvent;
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
        json.put("currentPlayer", swap.getOriginalPlayer());
        json.put("currentPlayerCardNumber", swap.getOriginalPlayerCardNumber());
        json.put("rdmPlayerIndex", swap.getRdmPlayerIndex());
        json.put("rdmPlayerCardNumber", swap.getRdmPlayerCardNumber());
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

    @Override
    public Swap getSwap(RdmEvent rdmEvent) {
        JsonElement msg = null;
        msg = reader.next();
        JSONObject swapEvent = new JSONObject(msg.getAsJsonObject().toString()); //TODO catch exceptions
        String type = swapEvent.getString("type");
        if (!type.equals("swap")) {
            throw new InvalidPlayerMoveException();
        }
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
        json.put("value", bid.getBidValue());
        json.put("blindBid", bid.isBlind());
        json.put("playerIndex", playerNumber);
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream(), StandardCharsets.UTF_8));
            out.write(json.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Bid makeBid(IntPredicate validBid) { //TODO allow passing
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
        //TODO take suit into account
        int value = bidEvent.getInt("value");
        boolean blind = bidEvent.optBoolean("blind", false);
        if (!validBid.test(value)) {
            throw new InvalidBidException();
        }
        return new Bid(value, blind);

    }
}
