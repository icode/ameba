package ameba.websocket.internal;

import ameba.websocket.CloseReasons;
import ameba.websocket.WebSocketException;

import javax.websocket.CloseReason;

/**
 * @author icode
 */
public class MessageTooBigException extends WebSocketException {
    private static final CloseReason CLOSE_REASON = CloseReasons.TOO_BIG.getCloseReason();

    MessageTooBigException(String message) {
        super(message);
    }

    @Override
    public CloseReason getCloseReason() {
        return CLOSE_REASON;
    }
}
