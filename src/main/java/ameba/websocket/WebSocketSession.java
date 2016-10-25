package ameba.websocket;

import javax.websocket.CloseReason;
import javax.websocket.Extension;
import javax.ws.rs.core.HttpHeaders;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * @author icode
 */
public interface WebSocketSession extends Closeable {

    /**
     * Return a unique session identifier.
     */
    String getId();

    /**
     * Return the URI used to open the WebSocket connection.
     */
    URI getUri();

    /**
     * Return the headers used in the handshake request.
     */
    HttpHeaders getHandshakeHeaders();

    /**
     * Return the map with attributes associated with the WebSocket session.
     *
     * @return a Map with the session attributes, never {@code null}.
     */
    Map<String, Object> getAttributes();

    /**
     * Return the attribute associated with the WebSocket session.
     *
     * @return a value the session attribute.
     */
    <T> T getAttribute(String key);

    /**
     * set attribute with the WebSocket session.
     *
     * @param key   name key
     * @param value value
     */
    void setAttribute(String key, Object value);

    /**
     * Return a {@link java.security.Principal} instance containing the name of the
     * authenticated user.
     * <p>If the user has not been authenticated, the method returns <code>null</code>.
     */
    Principal getPrincipal();

    /**
     * Return the address on which the request was received.
     */
    InetSocketAddress getLocalAddress();

    /**
     * Return the address of the remote client.
     */
    InetSocketAddress getRemoteAddress();

    /**
     * Return the negotiated sub-protocol or {@code null} if none was specified or
     * negotiated successfully.
     */
    String getAcceptedProtocol();

    /**
     * Get the configured maximum size for an incoming text message.
     */
    int getTextMessageSizeLimit();

    /**
     * Configure the maximum size for an incoming text message.
     */
    void setTextMessageSizeLimit(int messageSizeLimit);

    /**
     * Get the configured maximum size for an incoming binary message.
     */
    int getBinaryMessageSizeLimit();

    /**
     * Configure the maximum size for an incoming binary message.
     */
    void setBinaryMessageSizeLimit(int messageSizeLimit);

    /**
     * Return the negotiated extensions or {@code null} if none was specified or
     * negotiated successfully.
     */
    List<Extension> getExtensions();

    /**
     * Send a WebSocket message
     */
    void sendMessage(Object message) throws IOException;

    /**
     * Return whether the connection is still open.
     */
    boolean isOpen();

    /**
     * Close the WebSocket connection with status 1000, i.e. equivalent to:
     * <pre class="code">
     * session.close(CloseReason.NORMAL_CLOSURE);
     * </pre>
     */
    @Override
    void close() throws IOException;

    /**
     * Close the WebSocket connection with the given close status.
     */
    void close(CloseReason reason) throws IOException;

}
