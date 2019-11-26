package src.player;

import src.card.Card;
import src.exceptions.InvalidPlayerMoveException;
import src.gameEngine.Hand;

import java.io.*;
import java.net.*;
import java.nio.*;
import org.json.JSONObject;

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
    public Card playCard(String trumpSuit, Hand currentTrick) {
        StringBuilder message = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
            String readLine;
            while((readLine = reader.readLine()) != null){
                message.append(readLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject cardEvent = new JSONObject(message); //TODO catch exceptions
        String type = cardEvent.getString("type");
        int playerNumber = cardEvent.getInt("playerIndex");
        if(!type.equals("play") || super.getPlayerNumber() != playerNumber){
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
        throw new UnsupportedOperationException();
    }


    @Override
    public int makeBid() {
        throw new UnsupportedOperationException();
    }
}
