package ameba.websocket;

import ameba.exception.AmebaException;

import javax.websocket.CloseReason;

/**
 * <p>WebSocketException class.</p>
 *
 * @author icode
 * @since 0.1.6e
 * @version $Id: $Id
 */
public class WebSocketException extends AmebaException {
    /**
     * <p>Constructor for WebSocketException.</p>
     */
    public WebSocketException() {
    }

    /**
     * <p>Constructor for WebSocketException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public WebSocketException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>Constructor for WebSocketException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public WebSocketException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for WebSocketException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause   a {@link java.lang.Throwable} object.
     */
    public WebSocketException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Get close reason.
     *
     * @return close reason used when processing this exception.
     */
    public CloseReason getCloseReason() {
        return null;
    }
}
