package src.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import src.card.Card;
import src.exceptions.InvalidPlayerMoveException;
import src.gameEngine.Bid;
import src.gameEngine.Hand;

import java.io.*;
import java.net.*;
import java.nio.*;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class NetworkPlayer extends Player {

    private Socket playerSocket;

    public NetworkPlayer(int playerNumber, Predicate<Card> validCard) {
        super(playerNumber, validCard);
    }

    public NetworkPlayer(int playerNumber, Socket playerSocket){
        super(playerNumber);
        this.playerSocket = playerSocket;
    }

    @Override
    public Card playCard(String trumpSuit, Hand currentTrick) { // when you're receiving a card
        StringBuilder message = new StringBuilder();
        JsonElement msg = null;
        try {
            JsonStreamParser reader = new JsonStreamParser(new InputStreamReader(playerSocket.getInputStream()));
            msg = reader.next();
//            while((readLine = reader.readLine()) != null){
//                message.append(readLine);
//            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
//        System.out.println("String: "+ msg.toString());
        JSONObject cardEvent = new JSONObject(msg.getAsJsonObject().toString()); //TODO catch exceptions
        System.out.println(cardEvent.toString(4));
        String type = cardEvent.getString("type");
        if(!type.equals("play")){
            throw new InvalidPlayerMoveException();
        }
        String suit = cardEvent.getString("suit");
        String rank = cardEvent.getString("rank");
        Card card = new Card(suit, rank);
        //Checks if card is valid
        if (!super.getCanBePlayed().test(card)){
            throw new InvalidPlayerMoveException();
        }
        return card;
    }

    @Override
    public void broadcastPlay(Card card, int playerNumber) {
        //Creates the json object to be sent.
        JSONObject json = new JSONObject();
        System.out.println(json.toString(4));
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

    public Socket getPlayerSocket() {
        return playerSocket;
    }

    @Override
    public Bid makeBid(IntPredicate validBid) {
        throw new UnsupportedOperationException();
    }
}
