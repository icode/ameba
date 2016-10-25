package ameba.websocket.sockjs;

import ameba.exception.AmebaException;

/**
 * @author icode
 */
public class SockJsException extends AmebaException {
    public SockJsException() {
        super();
    }

    public SockJsException(Throwable cause) {
        super(cause);
    }

    public SockJsException(String message) {
        super(message);
    }

    public SockJsException(String message, Throwable cause) {
        super(message, cause);
    }
}
