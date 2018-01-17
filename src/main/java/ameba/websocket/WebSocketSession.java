package ameba.websocket;

import javax.websocket.CloseReason;
import javax.websocket.Extension;
import javax.ws.rs.core.MultivaluedMap;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * <p>WebSocketSession interface.</p>
 *
 * @author icode
 *
 */
public interface WebSocketSession extends Closeable {

    /**
     * Return a unique session identifier.
     *
     * @return a {@link java.lang.String} object.
     */
    String getId();

    /**
     * Return the URI used to open the WebSocket connection.
     *
     * @return a {@link java.net.URI} object.
     */
    URI getUri();

    /**
     * Return the headers used in the handshake request.
     *
     * @return a {@link javax.ws.rs.core.MultivaluedMap} object.
     */
    MultivaluedMap<String, String> getHandshakeHeaders();

    /**
     * Return the request parameters associated with the request this session
     * was opened under.
     *
     * @return the unmodifiable map of the request parameters.
     */
    Map<String, List<String>> getRequestParameterMap();

    /**
     * Return a map of the path parameter names and values used associated with the
     * request this session was opened under.
     *
     * @return the unmodifiable map of path parameters. The key of the map is the parameter name,
     * the values in the map are the parameter values.
     */
    Map<String, String> getPathParameters();

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
     * @param key a {@link java.lang.String} object.
     * @param <T> a T object.
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
     *
     * @return a {@link java.security.Principal} object.
     */
    Principal getPrincipal();

    /**
     * Return the address on which the request was received.
     *
     * @return a {@link java.net.InetSocketAddress} object.
     */
    InetSocketAddress getLocalAddress();

    /**
     * Return the address of the remote client.
     *
     * @return a {@link java.net.InetSocketAddress} object.
     */
    InetSocketAddress getRemoteAddress();

    /**
     * Return the negotiated sub-protocol or {@code null} if none was specified or
     * negotiated successfully.
     *
     * @return a {@link java.lang.String} object.
     */
    String getNegotiatedProtocol();

    /**
     * Get the configured maximum size for an incoming text message.
     *
     * @return a int.
     */
    int getTextMessageSizeLimit();

    /**
     * Configure the maximum size for an incoming text message.
     *
     * @param messageSizeLimit a int.
     */
    void setTextMessageSizeLimit(int messageSizeLimit);

    /**
     * Get the configured maximum size for an incoming binary message.
     *
     * @return a int.
     */
    int getBinaryMessageSizeLimit();

    /**
     * Configure the maximum size for an incoming binary message.
     *
     * @param messageSizeLimit a int.
     */
    void setBinaryMessageSizeLimit(int messageSizeLimit);

    /**
     * Return the negotiated extensions or {@code null} if none was specified or
     * negotiated successfully.
     *
     * @return a {@link java.util.List} object.
     */
    List<Extension> getExtensions();

    /**
     * Send a WebSocket message
     *
     * @param message a {@link java.lang.Object} object.
     * @return a {@link java.util.concurrent.Future} object.
     * @throws java.io.IOException if any.
     */
    Future<Void> sendMessage(Object message) throws IOException;

    /**
     * Return whether the connection is still open.
     *
     * @return a boolean.
     */
    boolean isOpen();

    /**
     * {@inheritDoc}
     *
     * Close the WebSocket connection with status 1000, i.e. equivalent to:
     * <pre class="code">
     * session.close(CloseReason.NORMAL_CLOSURE);
     * </pre>
     */
    @Override
    void close() throws IOException;

    /**
     * Close the WebSocket connection with the given close status.
     *
     * @param reason a {@link javax.websocket.CloseReason} object.
     * @throws java.io.IOException if any.
     */
    void close(CloseReason reason) throws IOException;
}
