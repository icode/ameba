package ameba.exceptions;

/**
 * @author icode
 */
public class UnexpectedException extends AmebaException {
    public UnexpectedException(String message) {
        super(message);
    }

    public UnexpectedException(Throwable exception) {
        super("Unexpected Error", exception);
    }

    public UnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
