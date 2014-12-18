package ameba.websocket;

import ameba.exception.AmebaException;

/**
 * @author icode
 */
public class WebSocketException extends AmebaException {
    public WebSocketException() {
    }

    public WebSocketException(Throwable cause) {
        super(cause);
    }

    public WebSocketException(String message) {
        super(message);
    }

    public WebSocketException(String message, Throwable cause) {
        super(message, cause);
    }
}
