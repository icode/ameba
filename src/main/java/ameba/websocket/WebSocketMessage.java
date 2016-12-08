package ameba.websocket;

/**
 * <p>WebSocketMessage interface.</p>
 *
 * @author icode
 *
 */
public interface WebSocketMessage<T> {

    /**
     * Returns the message payload. This will never be {@code null}.
     *
     * @return a T object.
     */
    T getPayload();

    /**
     * Return the number of bytes contained in the message.
     *
     * @return a int.
     */
    int getPayloadLength();

    /**
     * When partial message support is available and requested via
     * supports partial messages,
     * this method returns {@code true} if the current message is the last part of the
     * complete WebSocket message sent by the client. Otherwise {@code false} is returned
     * if partial message support is either not available or not enabled.
     *
     * @return a boolean.
     */
    boolean isLast();
}
