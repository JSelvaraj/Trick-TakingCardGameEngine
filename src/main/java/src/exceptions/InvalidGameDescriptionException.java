package src.exceptions;

import java.io.InvalidObjectException;

public class InvalidGameDescriptionException extends InvalidObjectException {
    public InvalidGameDescriptionException(String s) {
        super(s);
    }
}
