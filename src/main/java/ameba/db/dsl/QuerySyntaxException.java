package ameba.db.dsl;

import ameba.exception.AmebaException;

/**
 * <p>QuerySyntaxException class.</p>
 *
 * @author icode
 *
 */
public class QuerySyntaxException extends AmebaException {
    /**
     * <p>Constructor for QuerySyntaxException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public QuerySyntaxException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for QuerySyntaxException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause   a {@link java.lang.Throwable} object.
     */
    public QuerySyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}
