package ameba.exception;

/**
 * <p>UnexpectedException class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public class UnexpectedException extends AmebaException {
    /**
     * <p>Constructor for UnexpectedException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public UnexpectedException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for UnexpectedException.</p>
     *
     * @param exception a {@link java.lang.Throwable} object.
     */
    public UnexpectedException(Throwable exception) {
        super("Unexpected Error", exception);
    }

    /**
     * <p>Constructor for UnexpectedException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause   a {@link java.lang.Throwable} object.
     */
    public UnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
