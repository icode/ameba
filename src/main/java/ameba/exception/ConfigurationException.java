package ameba.exception;

/**
 * <p>ConfigurationException class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public class ConfigurationException extends AmebaException {
    /**
     * <p>Constructor for ConfigurationException.</p>
     */
    public ConfigurationException() {
    }

    /**
     * <p>Constructor for ConfigurationException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>Constructor for ConfigurationException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for ConfigurationException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause   a {@link java.lang.Throwable} object.
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
