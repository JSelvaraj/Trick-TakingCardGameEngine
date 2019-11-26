package src;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import src.parser.Parser;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;


class ParserTest {
    Parser parser = new Parser();

    @Test
    void validateGameDescription(){
        JSONObject gameDesc = null;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("whist.json")) {
            if (inputStream == null){
                throw new IOException();
            }
            gameDesc = new JSONObject(new JSONTokener(inputStream));
        } catch (IOException e) {
            fail("Couldn't read input file.");
        }
        assertTrue(parser.validateObject(gameDesc));
    }
}
