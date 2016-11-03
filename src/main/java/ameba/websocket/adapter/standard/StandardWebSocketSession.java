/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ameba.websocket.adapter.standard;

import ameba.websocket.*;
import ameba.websocket.adapter.AbstractWebSocketSession;
import com.google.common.util.concurrent.Futures;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;

import javax.websocket.CloseReason;
import javax.websocket.Extension;
import javax.websocket.Session;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * A {@link WebSocketSession} for use with the standard WebSocket for Java API.
 *
 * @author Rossen Stoyanchev
 * @author icode
 */
public class StandardWebSocketSession extends AbstractWebSocketSession<Session> {

    private final MultivaluedMap<String, String> handshakeHeaders;
    private final InetSocketAddress localAddress;
    private final InetSocketAddress remoteAddress;
    private String id;
    private URI uri;
    private String negotiatedProtocol;
    private List<Extension> extensions;
    private Principal user;


    /**
     * Class constructor.
     *
     * @param headers       the headers of the handshake request
     * @param attributes    attributes from the HTTP handshake to associate with the WebSocket
     *                      session; the provided attributes are copied, the original map is not used.
     * @param localAddress  the address on which the request was received
     * @param remoteAddress the address of the remote client
     */
    public StandardWebSocketSession(MultivaluedMap<String, String> headers, Map<String, Object> attributes,
                                    InetSocketAddress localAddress, InetSocketAddress remoteAddress) {

        this(headers, attributes, localAddress, remoteAddress, null);
    }

    /**
     * Class constructor that associates a user with the WebSocket session.
     *
     * @param headers       the headers of the handshake request
     * @param attributes    attributes from the HTTP handshake to associate with the WebSocket session
     * @param localAddress  the address on which the request was received
     * @param remoteAddress the address of the remote client
     * @param user          the user associated with the session; if {@code null} we'll
     *                      fallback on the user available in the underlying WebSocket session
     */
    public StandardWebSocketSession(MultivaluedMap<String, String> headers, Map<String, Object> attributes,
                                    InetSocketAddress localAddress, InetSocketAddress remoteAddress, Principal user) {
        super(attributes);
        this.handshakeHeaders = (headers != null) ? headers : new MultivaluedStringMap();
        this.user = user;
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }


    @Override
    public String getId() {
        checkNativeSessionInitialized();
        return this.id;
    }

    @Override
    public URI getUri() {
        checkNativeSessionInitialized();
        return this.uri;
    }

    @Override
    public MultivaluedMap<String, String> getHandshakeHeaders() {
        return this.handshakeHeaders;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) getAttributes().get(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        getAttributes().put(key, value);
    }

    @Override
    public List<Extension> getExtensions() {
        checkNativeSessionInitialized();
        return this.extensions;
    }

    public Principal getPrincipal() {
        return this.user;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return this.localAddress;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    @Override
    public String getNegotiatedProtocol() {
        checkNativeSessionInitialized();
        return this.negotiatedProtocol;
    }

    @Override
    public int getTextMessageSizeLimit() {
        checkNativeSessionInitialized();
        return getNativeSession().getMaxTextMessageBufferSize();
    }

    @Override
    public void setTextMessageSizeLimit(int messageSizeLimit) {
        checkNativeSessionInitialized();
        getNativeSession().setMaxTextMessageBufferSize(messageSizeLimit);
    }

    @Override
    public int getBinaryMessageSizeLimit() {
        checkNativeSessionInitialized();
        return getNativeSession().getMaxBinaryMessageBufferSize();
    }

    @Override
    public void setBinaryMessageSizeLimit(int messageSizeLimit) {
        checkNativeSessionInitialized();
        getNativeSession().setMaxBinaryMessageBufferSize(messageSizeLimit);
    }

    @Override
    public boolean isOpen() {
        return (getNativeSession() != null && getNativeSession().isOpen());
    }

    @Override
    public Set<Session> getOpenSessions() {
        checkNativeSessionInitialized();
        return getNativeSession().getOpenSessions();
    }

    @Override
    public void initializeNativeSession(Session session) {
        super.initializeNativeSession(session);

        this.id = session.getId();
        this.uri = session.getRequestURI();

        this.negotiatedProtocol = session.getNegotiatedSubprotocol();

        this.extensions = session.getNegotiatedExtensions();

        if (this.user == null) {
            this.user = session.getUserPrincipal();
        }
    }

    @Override
    protected Future<Void> sendTextMessage(TextMessage message) {
        return getNativeSession().getAsyncRemote().sendText(message.getPayload());
    }

    @Override
    protected Future<Void> sendBinaryMessage(BinaryMessage message) {
        return getNativeSession().getAsyncRemote().sendBinary(message.getPayload());
    }

    @Override
    protected Future<Void> sendPingMessage(PingMessage message) throws IOException {
        getNativeSession().getAsyncRemote().sendPing(message.getPayload());
        return Futures.immediateFuture(null);
    }

    @Override
    protected Future<Void> sendPongMessage(PongMessage message) throws IOException {
        getNativeSession().getAsyncRemote().sendPong(message.getPayload());
        return Futures.immediateFuture(null);
    }

    @Override
    protected Future<Void> sendObjectMessage(Object message) {
        return getNativeSession().getAsyncRemote().sendObject(message);
    }

    @Override
    protected void closeInternal(CloseReason status) throws IOException {
        checkNativeSessionInitialized();
        getNativeSession().close(status);
    }
}
