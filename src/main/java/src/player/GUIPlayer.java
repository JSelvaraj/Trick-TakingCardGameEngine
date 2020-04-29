package src.player;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.java_websocket.WebSocket;
import src.bid.Bid;
import src.bid.ContractBid;
import src.bid.PotentialBid;
import src.card.Card;
import src.gameEngine.Hand;

import src.rdmEvents.Swap;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GUIPlayer extends LocalPlayer {

    private WebSocket webSocket;

    @Override
    public Card playCard(String trumpSuit, Hand currentTrick) {
        JsonObject request = new JsonObject();
        request.add("type", new JsonPrimitive("playcard"));
        JsonArray validCardsJson = new JsonArray();
        Consumer<Card> cardConsumer;
        cardConsumer = card -> {
            JsonObject cardJson = new Gson().fromJson(card.getJSON(), JsonObject.class);
            validCardsJson.add(cardJson);
        };
        this.getHand().getHand().stream().filter(super.getCanBePlayed()).forEach(cardConsumer);
        request.add("validcards", validCardsJson);
        System.out.println("PLAYCARD REQUEST: " + new Gson().toJson(request));
        webSocket.send(new Gson().toJson(request));
        return null;
    }

    @Override
    public Bid makeBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, boolean firstRound, boolean canBidBlind) {
        JsonObject request = new JsonObject();
        request.add("type", new JsonPrimitive("makebid"));
        //initialize possible bid values for each field of PotentialBid
        String[] suits;
        if (trumpSuitBid) {
            suits = new String[]{"CLUBS", "SPADES", "HEARTS", "DIAMONDS", "NO TRUMP"};
        } else {
            suits = new String[]{null};
        }
        ArrayList<String> bidInputs = new ArrayList<>();
        for (int i = 0; i < getHand().getHandSize() + 1; i++) {
            bidInputs.add(String.valueOf(i));
        }
        bidInputs.add(String.valueOf(-2));
        bidInputs.add("d");

        //create an arraylist of all possible potential bids
        ArrayList<PotentialBid> bids = new ArrayList<>();
        for (String input: bidInputs) {
            for (String suit: suits) {
                bids.add(new PotentialBid(suit, input, adjustedHighestBid, this, firstRound));
            }
        }
        JsonArray validBidsJson = new JsonArray();
        //tests every potential bid for validity and adds it to a JsonArray to be sent to front-end
        bids.forEach(new Consumer<PotentialBid>() {
            @Override
            public void accept(PotentialBid potentialBid) {
                if (validBid.test(potentialBid)){
                    Gson gson = new Gson();
                    validBidsJson.add(potentialBid.toBid(true).toJson());
                    validBidsJson.add(potentialBid.toBid(false).toJson());
                }
            }
        });
        request.add("validBids", validBidsJson);
        request.add("isPlayerVuln", new JsonPrimitive(getTeam().isVulnerable()));
        webSocket.send(request.getAsString());
        return null;
    }

    @Override
    public Swap getSwap(Player strongPlayer) {
        JsonObject request = new JsonObject();
        request.add("type", new JsonPrimitive("getswap"));
        request.add("choosingPlayer", new JsonPrimitive(getPlayerNumber()));
        request.add("otherplayer", new JsonPrimitive(strongPlayer.getPlayerNumber()));
        webSocket.send(request.getAsString());
        return null;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }
}
