package src.player;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.java_websocket.WebSocket;
import src.card.Card;
import src.gameEngine.Hand;
import src.rdmEvents.Swap;

import java.util.function.Consumer;

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
        webSocket.send(request.getAsString());
        return null;
    }

//    @Override
//    public Bid makeBid(IntPredicate validBid) {
//        JsonObject request = new JsonObject();
//        request.add("type", new JsonPrimitive("makebid"));
//        JsonArray validBidsJson = new JsonArray();
//        IntConsumer integerConsumer = validBidsJson::add;
//        IntStream.range(0,52).filter(validBid).forEach(integerConsumer);
//        request.add("validbids", validBidsJson);
//        webSocket.send(request.getAsString());
//        return null;
//    }

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
