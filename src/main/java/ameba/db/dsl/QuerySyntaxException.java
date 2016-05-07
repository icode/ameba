package ameba.db.dsl;

import ameba.exception.AmebaException;

/**
 * @author icode
 */
public class QuerySyntaxException extends AmebaException {
    public QuerySyntaxException(String message) {
        super(message);
    }

    public QuerySyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}