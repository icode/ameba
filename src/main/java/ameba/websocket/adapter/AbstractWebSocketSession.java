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

package ameba.websocket.adapter;

import ameba.util.Assert;
import ameba.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * An abstract base class for implementations of {@link ameba.websocket.WebSocketSession}.
 *
 * @author Rossen Stoyanchev
 * @author icode
 * @version $Id: $Id
 */
public abstract class AbstractWebSocketSession<T> implements NativeWebSocketSession {

    /**
     * Constant <code>logger</code>
     */
    protected static final Logger logger = LoggerFactory.getLogger(NativeWebSocketSession.class);
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private T nativeSession;


    /**
     * Create a new instance and associate the given attributes with it.
     *
     * @param attributes attributes from the HTTP handshake to associate with the WebSocket
     *                   session; the provided attributes are copied, the original map is not used.
     */
    public AbstractWebSocketSession(Map<String, Object> attributes) {
        if (attributes != null) {
            this.attributes.putAll(attributes);
        }
    }


    /** {@inheritDoc} */
    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    /** {@inheritDoc} */
    @Override
    public T getNativeSession() {
        return this.nativeSession;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <R> R getNativeSession(Class<R> requiredType) {
        if (requiredType != null) {
            if (requiredType.isInstance(this.nativeSession)) {
                return (R) this.nativeSession;
            }
        }
        return null;
    }

    /**
     * <p>initializeNativeSession.</p>
     *
     * @param session a T object.
     */
    public void initializeNativeSession(T session) {
        Assert.notNull(session, "session must not be null");
        this.nativeSession = session;
    }

    /**
     * <p>checkNativeSessionInitialized.</p>
     */
    protected final void checkNativeSessionInitialized() {
        Assert.state(this.nativeSession != null, "WebSocket session is not yet initialized");
    }

    /** {@inheritDoc} */
    @Override
    public final Future<Void> sendMessage(Object message) throws IOException {

        checkNativeSessionInitialized();

        if (logger.isTraceEnabled()) {
            logger.trace("Sending " + message + ", " + this);
        }

        if (message instanceof TextMessage) {
            return sendTextMessage((TextMessage) message);
        } else if (message instanceof BinaryMessage) {
            return sendBinaryMessage((BinaryMessage) message);
        } else if (message instanceof PingMessage) {
            return sendPingMessage((PingMessage) message);
        } else if (message instanceof PongMessage) {
            return sendPongMessage((PongMessage) message);
        }
        return sendObjectMessage(message);
    }

    /**
     * <p>sendTextMessage.</p>
     *
     * @param message a {@link ameba.websocket.TextMessage} object.
     * @return a {@link java.util.concurrent.Future} object.
     * @throws java.io.IOException if any.
     */
    protected abstract Future<Void> sendTextMessage(TextMessage message) throws IOException;

    /**
     * <p>sendBinaryMessage.</p>
     *
     * @param message a {@link ameba.websocket.BinaryMessage} object.
     * @return a {@link java.util.concurrent.Future} object.
     * @throws java.io.IOException if any.
     */
    protected abstract Future<Void> sendBinaryMessage(BinaryMessage message) throws IOException;

    /**
     * <p>sendPingMessage.</p>
     *
     * @param message a {@link ameba.websocket.PingMessage} object.
     * @return a {@link java.util.concurrent.Future} object.
     * @throws java.io.IOException if any.
     */
    protected abstract Future<Void> sendPingMessage(PingMessage message) throws IOException;

    /**
     * <p>sendPongMessage.</p>
     *
     * @param message a {@link ameba.websocket.PongMessage} object.
     * @return a {@link java.util.concurrent.Future} object.
     * @throws java.io.IOException if any.
     */
    protected abstract Future<Void> sendPongMessage(PongMessage message) throws IOException;

    /**
     * <p>sendObjectMessage.</p>
     *
     * @param message a {@link java.lang.Object} object.
     * @return a {@link java.util.concurrent.Future} object.
     * @throws java.io.IOException if any.
     */
    protected abstract Future<Void> sendObjectMessage(Object message) throws IOException;

    /** {@inheritDoc} */
    @Override
    public final void close() throws IOException {
        close(CloseReasons.NORMAL_CLOSURE.getCloseReason());
    }

    /** {@inheritDoc} */
    @Override
    public final void close(CloseReason status) throws IOException {
        checkNativeSessionInitialized();
        if (logger.isDebugEnabled()) {
            logger.debug("Closing " + this);
        }
        closeInternal(status);
    }

    /**
     * <p>closeInternal.</p>
     *
     * @param status a {@link javax.websocket.CloseReason} object.
     * @throws java.io.IOException if any.
     */
    protected abstract void closeInternal(CloseReason status) throws IOException;


    /** {@inheritDoc} */
    @Override
    public String toString() {
        if (this.nativeSession != null) {
            return getClass().getSimpleName() + "[id=" + getId() + ", uri=" + getUri() + "]";
        } else {
            return getClass().getSimpleName() + "[nativeSession=null]";
        }
    }

}
