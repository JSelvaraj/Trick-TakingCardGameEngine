package src.player;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.java_websocket.WebSocket;
import org.json.JSONObject;
import src.bid.Bid;
import src.bid.ContractBid;
import src.bid.PotentialBid;
import src.card.Card;
import src.gameEngine.Hand;

import src.parser.GameDesc;
import src.rdmEvents.Swap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GUIPlayer extends LocalPlayer {

    private WebSocket webSocket;
    private GameDesc desc;

    @Override
    public Card playCard(String trumpSuit, Hand currentTrick) {
        if (aiTakeover){
            return aiPlayer.playCard(trumpSuit, currentTrick);
        }
        JsonObject request = new JsonObject();
        request.add("type", new JsonPrimitive("getCard"));
        JsonArray validCardsJson = new JsonArray();
        this.getHand().getHand().stream().filter(super.getCanBePlayed()).forEach(card -> {
            JsonObject cardJson = new Gson().fromJson(card.getJSON(), JsonObject.class);
            validCardsJson.add(cardJson);
        });
        request.add("playerhand", this.getHand().toJsonArray());
        request.add("validcards", validCardsJson);
        System.out.println("PLAYCARD REQUEST: " + new Gson().toJson(request));
        webSocket.send(new Gson().toJson(request));
        return new Card("SPADES", "ACE");
    }

    @Override
    public void broadcastBid(Bid bid, int playerNumber, ContractBid adjustedHighestBid) {
        JSONObject json = new JSONObject();
        json.put("type", "bid");
        json.put("playerindex", playerNumber);
        json.put("doubling", bid.isDoubling());

        if (bid.getSuit() != null) {
            json.put("suit", bid.getSuit());
        }
        json.put("value", bid.getBidValue());
        json.put("blindBid", bid.isBlind());
        webSocket.send(json.toString());
    }

    @Override
    public Bid makeBid(Predicate<PotentialBid> validBid, boolean trumpSuitBid, ContractBid adjustedHighestBid, boolean firstRound, boolean canBidBlind) {

        if(aiTakeover){
            System.out.println("AI takeover");
            return aiPlayer.makeBid(validBid, trumpSuitBid, adjustedHighestBid, firstRound, canBidBlind);
        }

        JsonObject request = new JsonObject();
        request.add("type", new JsonPrimitive("makebid"));


        request.add("suitenabled", new JsonPrimitive(trumpSuitBid));
        if (trumpSuitBid) {
            String[] suits;
            suits = new String[]{"CLUBS", "SPADES", "HEARTS", "DIAMONDS", "NO TRUMP"};
            JsonArray suitsJson = new JsonArray();
            for (String suit: suits) {
                suitsJson.add(suit);
            }
            request.add("suits", suitsJson);
        }

        request.add("bidblindenabled", new JsonPrimitive(canBidBlind));
        request.add("numberofroundsenabled", new JsonPrimitive(true));
        request.add("doublingenabled", new JsonPrimitive(desc.isCanDouble()));
        request.add("passingenabled", new JsonPrimitive(desc.isAscendingBid()));

        int minimumBid = adjustedHighestBid == null ? 0 : adjustedHighestBid.getBidValue();
        JsonArray numberofrounds = new JsonArray();
        for (int i = minimumBid; i < 14 ; i++) {
            numberofrounds.add(i);
        }
        request.add("numberofrounds", numberofrounds );


        request.add("isPlayerVuln", new JsonPrimitive(getTeam().isVulnerable()));
        request.add("firstround", new JsonPrimitive(firstRound));
        webSocket.send(new Gson().toJson(request));
        return null;
    }

    @Override
    public Swap getSwap(Player strongPlayer) {
        JsonObject request = new JsonObject();
        request.add("type", new JsonPrimitive("getswap"));
        request.add("choosingPlayer", new JsonPrimitive(getPlayerNumber()));
        request.add("otherplayer", new JsonPrimitive(strongPlayer.getPlayerNumber()));
        webSocket.send(new Gson().toJson(request));
        return null;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    @Override
    public void broadcastDummyHand(int playerNumber, List<Card> dummyHand) {
        JsonArray dummyHandArray = new JsonArray();
        JsonObject dummyplayer = new JsonObject();
        dummyplayer.add("type", new JsonPrimitive("dummyplayer"));
        dummyplayer.add("playerindex", new JsonPrimitive(playerNumber));
        dummyplayer.add("playerhand", dummyHandArray);
        System.out.println("SENDING DUMMY HAND TO GUI: " + new Gson().toJson(new Hand(dummyHand).toJsonArray()));
        webSocket.send(new Gson().toJson(dummyplayer));
    }

    public void setDesc(GameDesc desc) {
        this.desc = desc;
    }
}
