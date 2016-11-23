package ameba.websocket.sockjs.transport;

import ameba.exception.AmebaException;

/**
 * <p>SockJsException class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class SockJsException extends AmebaException {
    /**
     * <p>Constructor for SockJsException.</p>
     */
    public SockJsException() {
        super();
    }

    /**
     * <p>Constructor for SockJsException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public SockJsException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>Constructor for SockJsException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public SockJsException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for SockJsException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause   a {@link java.lang.Throwable} object.
     */
    public SockJsException(String message, Throwable cause) {
        super(message, cause);
    }
}
