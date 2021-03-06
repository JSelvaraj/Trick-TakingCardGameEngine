package src.parser;

import org.apache.commons.lang3.ArrayUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import src.bid.Bid;
import src.card.Card;
import src.deck.Deck;
import src.exceptions.InvalidGameDescriptionException;
import src.functions.validBids;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * Creates parser based on JSON schema file to parse the game description JSON
 */
public class Parser {
    //Sets the JSON schema
    private static final String schemaFile = "json-schema.json";
    private Schema schema;

    private final String[] DEFAULT_SUITS = {"CLUBS", "DIAMONDS", "HEARTS", "SPADES"};
    private final String[] DEFAULT_RANKS = {"ACE", "KING", "QUEEN", "JACK", "TEN", "NINE", "EIGHT", "SEVEN", "SIX", "FIVE", "FOUR", "THREE", "TWO"};
    private final String[] DEFAULT_RANK_ORDER = {"TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "JACK", "QUEEN", "KING", "ACE"};

    public Parser() {
        this.schema = initSchema();
    }

    /**
     * @return Schema that defines how to parse gamedescription
     */
    private Schema initSchema() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(schemaFile)) {
            if (inputStream == null) {
                throw new IOException();
            }
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            return SchemaLoader.load(rawSchema);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * @param object JSON object to be validated based on the schema
     * @return
     */
    public boolean validateObject(JSONObject object) {
        try {
            schema.validate(object);
            return true;
        } catch (ValidationException e) {
            System.out.println(e.toJSON().toString(4));
            return false;
        }
    }

    public GameDesc parseGameDescription(JSONObject gameJSON) throws InvalidGameDescriptionException {
        //System.out.println(gameJSON.toString(4));
        if (gameJSON == null) {
            throw new InvalidGameDescriptionException("Failed to parse game description file.");
        }
        try {
            schema.validate(gameJSON);
        } catch (ValidationException e) {
            throw new InvalidGameDescriptionException(e.toJSON().toString(4));
        }
        //Gets the name and description
        String name = gameJSON.getString("name");
        String description = gameJSON.getString("description");
        //Gets player count
        int players = gameJSON.getInt("players");
        //Gets the suits and ranks to make the deck from.
        String[] suits;
        String[] ranks;
        String[] rank_order;
        Deck deck;
        if (gameJSON.isNull("deck")) {
            suits = DEFAULT_SUITS;
            ranks = DEFAULT_RANKS;
            rank_order = DEFAULT_RANK_ORDER;
            deck = new Deck();
        } else {
            JSONObject deckJSON = gameJSON.getJSONObject("deck");
            JSONArray cards = deckJSON.getJSONArray("cards");
            //TODO remove suit and ranks.
            suits = DEFAULT_SUITS;
            ranks = DEFAULT_RANKS;
            deck = new Deck((LinkedList<Card>) Deck.makeDeck(cards));
            JSONArray rank_orderJSON = deckJSON.getJSONArray("rankOrder");
            rank_order = new String[rank_orderJSON.length()];
            for (int i = 0; i < rank_order.length; i++) {
                rank_order[i] = rank_orderJSON.getString(i);
            }
            //Needed as spec is wrong
            ArrayUtils.reverse(rank_order);
        }
        //Gets the teams to use.
        int[][] teams;
        if (gameJSON.isNull("teams")) {
            teams = new int[players][1];
        } else {
            teams = convertTeamArray(gameJSON.getJSONArray("teams"));
        }
        boolean ascedingOrdering = gameJSON.getBoolean("ascending_ordering");
        int initialHandSize = gameJSON.getInt("initialHandSize");
        int minHandSize = gameJSON.optInt("minimumHandSize");
        //Rules
        //TODO fill in defaults
        String calculateScore = null;
        String trumpPickingMode = null;
        JSONArray trumpOrdering = null;
        String trumpSuit = null;
        String leadingCardForEachTrick = null;
        String gameEnd = null;
        String sessionEnd = "fixed";
        int sessionEndValue = 1;
        Integer scoreThreshold = null;
        Integer trickThreshold = 0;
        String nextLegalCardMode = null;
        String trickWinner = null;
        String trickLeader = null;
        String firstTrickLeader = "default";
        String handSize = "fixed";
        JSONArray rules = gameJSON.getJSONArray("rules"); //TODO check for null
        for (int i = 0; i < rules.length(); i++) {
            JSONObject rule = rules.getJSONObject(i);
            String rulename = rule.getString("name");
            switch (rulename) {
                case "calculateScore":
                    calculateScore = rule.getString("data");
                    break;
                case "trumpPickingMode":
                    trumpPickingMode = rule.getString("data");
                    break;
                case "trumpSuit":
                    trumpSuit = rule.getString("data");
                    break;
                case "leadingCardForEachTrick":
                    leadingCardForEachTrick = rule.getString("data");
                    break;
                case "sessionEnd":
                    sessionEnd = rule.getString("data");
                    break;
                case "sessionEndValue":
                    sessionEndValue = rule.getInt("data");
                    break;
                case "gameEnd":
                    gameEnd = rule.getString("data");
                    break;
                case "gameEndValue":
                    scoreThreshold = rule.getInt("data");
                    break;
                case "trickThreshold":
                    trickThreshold = rule.getInt("data");
                    break;
                case "nextLegalCardMode":
                    nextLegalCardMode = rule.getString("data");
                    break;
                case "trickWinner":
                    trickWinner = rule.getString("data");
                    break;
                case "trickLeader":
                    trickLeader = rule.getString("data");
                    break;
                case "firstTrickLeader":
                    firstTrickLeader = rule.getString("data");
                    break;
                case "handEnd":
                    //TODO change
                    break;
                case "tieBreaker":
                    //TODO change
                    break;
                case "handSize":
                    handSize = rule.getString("data");
                    break;
                case "trumpOrder":
                    trumpOrdering = rule.getJSONArray("data");
                    break;
                default:
                    break;

//                    throw new InvalidGameDescriptionException("Unrecognised rule: " + rulename);
            }
        }
        if (trumpPickingMode.equals("fixed") && trumpSuit == null) {
            throw new InvalidGameDescriptionException("No trump suit specified with fixed trump mode.");
        }

        assert trumpPickingMode != null;
        Iterator<String> trumpIterator = null;
        if (trumpPickingMode.equals("predefined") && trumpOrdering != null) {
            trumpIterator = parseTrumpOrdering(trumpOrdering);

        }

        //Pass any parameters that main engine needs that are bidding specific.
        boolean trumpSuitBid = false;
        boolean ascendingBid = false;
        int vulnerabilityThreshold = 0;
        boolean canBidBlind = false;
        boolean canDouble = false;
        boolean canRedouble = false;
        int minBid = 0;
        int maxBid = initialHandSize;
        List<String> bidSuits = new ArrayList<>();
        JSONObject bidObject = gameJSON.optJSONObject("bid");
        if (bidObject != null) {
            canDouble = bidObject.optBoolean("canDouble", false);
            canRedouble = bidObject.optBoolean("canRedouble", false);
            trumpSuitBid = bidObject.optBoolean("trumpSuitBid", false);
            ascendingBid = bidObject.optBoolean("ascendingBid", false);
            vulnerabilityThreshold = bidObject.optInt("vulnerabilityThreshold", 0);
            canBidBlind = bidObject.optBoolean("canBidBlind");
            minBid = bidObject.optInt("minBid", minBid);
            maxBid = bidObject.optInt("maxBid", maxBid);
            JSONArray bidSuitsJSON = bidObject.optJSONArray("suitBidRank");
            if(bidSuitsJSON != null){
                bidSuitsJSON.forEach((obj) ->bidSuits.add(obj.toString().equals("null") ? Bid.NOTRUMP : obj.toString()));
            }

        }


        GameDesc gameDesc = new GameDesc(name,
                players,
                teams,
                suits,
                ranks,
                rank_order,
                ascedingOrdering,
                minHandSize,
                initialHandSize,
                calculateScore,
                trumpPickingMode,
                trumpSuit,
                leadingCardForEachTrick,
                gameEnd,
                scoreThreshold,
                trickThreshold,
                nextLegalCardMode,
                trickWinner,
                trickLeader,
                firstTrickLeader,
                handSize,
                trumpIterator,
                trumpSuitBid,
                ascendingBid,
                sessionEnd,
                sessionEndValue,
                vulnerabilityThreshold,
                canBidBlind,
                minBid,
                maxBid,
                bidSuits,
                deck,
                canDouble,
                canRedouble);

        if (bidObject != null) {
            bidObject = gameJSON.getJSONObject("bid");
            initBidding(bidObject, gameDesc);
        }
        return gameDesc;
    }

    /**
     * @param filename Path to file containing the JSON object.
     * @return JSONObject parsed from the input file.
     */
    public static JSONObject readJSONFile(String filename) {
        try (InputStream inputStream = new FileInputStream(filename)) {
            return new JSONObject(new JSONTokener(inputStream));
        } catch (IOException e) {
            return null;
        }
    }


    /**
     * @param teamsJSON Convert 2 dimension JSON array into a 2 dimensional array of player numbers.
     * @return array containing the teams.
     */
    private int[][] convertTeamArray(JSONArray teamsJSON) {
        int teamSize = teamsJSON.getJSONArray(0).length();
        int[][] teams = new int[teamsJSON.length()][teamSize];
        for (int i = 0; i < teamsJSON.length(); i++) {
            for (int j = 0; j < teamSize; j++) {
                teams[i][j] = teamsJSON.getJSONArray(i).getInt(j);
            }
        }
        return teams;
    }

    private void initBidding(JSONObject bidObject, GameDesc gameDesc) {
        gameDesc.setValidBid(validBids.isValidBidValue(bidObject, gameDesc.getInitialHandSize()));
        gameDesc.setEvaluateBid(validBids.evaluateBid(bidObject, gameDesc.getTrickThreshold()));
        gameDesc.setBidding(true);
    }

    private static Iterator<String> parseTrumpOrdering(JSONArray trumpOrderingJSON) {
        String[] trumpOrdering = new String[trumpOrderingJSON.length()];
        for (int i = 0; i < trumpOrdering.length; i++) {
            trumpOrdering[i] = trumpOrderingJSON.getString(i);
        }
        return Arrays.asList(trumpOrdering).iterator();
    }

}

