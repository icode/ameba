package ameba.websocket;

import ameba.exceptions.AmebaException;

/**
 * @author icode
 */
public class WebSocketExcption extends AmebaException {
    public WebSocketExcption() {
    }

    public WebSocketExcption(Throwable cause) {
        super(cause);
    }

    public WebSocketExcption(String message) {
        super(message);
    }

    public WebSocketExcption(String message, Throwable cause) {
        super(message, cause);
    }
}
