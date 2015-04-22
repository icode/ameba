package ameba.container;

import ameba.exception.AmebaException;

/**
 * <p>ContainerException class.</p>
 *
 * @author icode
 */
public class ContainerException extends AmebaException {
    /**
     * <p>Constructor for ContainerException.</p>
     */
    public ContainerException() {
        super();
    }

    /**
     * <p>Constructor for ContainerException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public ContainerException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>Constructor for ContainerException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public ContainerException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for ContainerException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause   a {@link java.lang.Throwable} object.
     */
    public ContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}
