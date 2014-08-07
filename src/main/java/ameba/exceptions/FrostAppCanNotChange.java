package ameba.exceptions;

/**
 * @author icode
 */
public class FrostAppCanNotChange extends AmebaException {
    public FrostAppCanNotChange() {
    }

    public FrostAppCanNotChange(Throwable cause) {
        super(cause);
    }

    public FrostAppCanNotChange(String message) {
        super(message);
    }

    public FrostAppCanNotChange(String message, Throwable cause) {
        super(message, cause);
    }
}
