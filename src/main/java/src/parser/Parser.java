package src.parser;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;

import java.io.IOException;
import java.io.InputStream;


public class Parser{
    private static final String schemaFile = "json-schema·json";
    private Schema schema;

    public Parser(){
        this.schema = initSchema();
    }

    private Schema initSchema(){
        try (InputStream inputStream = getClass().getResourceAsStream(schemaFile)) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            return SchemaLoader.load(rawSchema);
        } catch (IOException e) {
            return null;
        }
    }

    public boolean validateObject(JSONObject object){
        try {
            schema.validate(object);
            return true;
        } catch (ValidationException e){
            System.out.println(e.toJSON().toString(4));
            return false;
        }
    }
}

