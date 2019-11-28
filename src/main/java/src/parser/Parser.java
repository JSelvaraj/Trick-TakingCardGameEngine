package src.parser;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import src.exceptions.InvalidGameDescriptionException;
import src.functions.validBids;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Creates parser based on JSON schema file to parse the game description JSON
 */
public class Parser {
    //Sets the JSON schema
    private static final String schemaFile = "json-schema.json";
    private Schema schema;

    private final String[] DEFAULT_SUITS = {"CLUBS", "DIAMONDS","HEARTS", "SPADES"};
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

    /**
     * @param filename Game description filename to be parsed
     * @return GameDesc object with attributes set from the game description
     * @throws InvalidGameDescriptionException
     */
    public GameDesc parseGameDescription(String filename) throws InvalidGameDescriptionException {
        JSONObject gameJSON = readJSONFile(filename);
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
        if (gameJSON.isNull("deck")) {
            suits = DEFAULT_SUITS;
            ranks = DEFAULT_RANKS;
            rank_order = DEFAULT_RANK_ORDER;
        } else {
            //TODO implement this. Currently only supports default deck.
            throw new UnsupportedOperationException();
        }
        //Gets the teams to use.
        int[][] teams;
        if (gameJSON.isNull("teams")) {
            teams = new int[players][1];
        } else {
            teams = convertTeamArray(gameJSON.getJSONArray("teams"));
        }
        boolean ascedingOrdering = gameJSON.getBoolean("ascending_ordering");
        //Rules
        //TODO fill in defaults
        String calculateScore = null;
        String trumpPickingMode = null;
        String trumpSuit = null;
        String leadingCardForEachTrick = null;
        String gameEnd = null;
        Integer scoreThreshold = null;
        Integer trickThreshold = null;
        String nextLegalCardMode = null;
        String trickWinner = null;
        String trickLeader = null;
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
                default:
                    //break;
                    throw new InvalidGameDescriptionException("Unrecognised rule: " + rulename);
            }
        }
        //Seed for generator
        long seed = 2; //TODO remove this.
        assert trumpPickingMode != null;
        GameDesc gameDesc = new GameDesc(players,
                teams,
                seed,
                suits,
                ranks,
                rank_order,
                ascedingOrdering,
                calculateScore,
                trumpPickingMode,
                trumpSuit,
                leadingCardForEachTrick,
                gameEnd,
                scoreThreshold,
                trickThreshold,
                nextLegalCardMode,
                trickWinner,
                trickLeader);

        if(!gameJSON.isNull("bid")) {
            JSONObject bidObject = gameJSON.getJSONObject("bid");
            initBidding(bidObject, gameDesc);
        }
        return gameDesc;
    }

    /**
     * @param filename Path to file containing the JSON object.
     * @return JSONObject parsed from the input file.
     */
    private JSONObject readJSONFile(String filename) {
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

    private void initBidding(JSONObject bidObject, GameDesc gameDesc){
        int minBid = bidObject.getInt("minBid");
        int maxBid = bidObject.getInt("maxBid");
        gameDesc.setValidBid(validBids.isValidBidValue(minBid, maxBid));
        gameDesc.setEvaluateBid(validBids.evaluateBid(bidObject));
        gameDesc.setBidding(true);
    }

}

