package ameba.websocket.internal;

import ameba.i18n.Messages;
import ameba.util.ClassUtils;
import ameba.websocket.CloseReasons;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * <p>Abstract EndpointMeta class.</p>
 *
 * @author icode
 *
 */
public abstract class EndpointMeta {
    private static final Logger logger = LoggerFactory.getLogger(EndpointMeta.class);
    protected final Set<MessageHandlerFactory> messageHandlerFactories = Sets.newLinkedHashSet();
    private Class endpointClass;

    /**
     * <p>Constructor for EndpointMeta.</p>
     *
     * @param endpointClass a {@link java.lang.Class} object.
     */
    public EndpointMeta(Class endpointClass) {
        this.endpointClass = endpointClass;
    }

    static Class<?> getHandlerType(MessageHandler handler) {
        if (handler instanceof TypeMessageHandler) {
            return ((TypeMessageHandler) handler).getType();
        }
        Class<?> result = ClassUtils.getGenericClass(handler.getClass());
        return result == null ? Object.class : result;
    }

    /**
     * <p>checkMessageSize.</p>
     *
     * @param message        a {@link java.lang.Object} object.
     * @param maxMessageSize a long.
     */
    protected static void checkMessageSize(Object message, long maxMessageSize) {
        if (maxMessageSize != -1) {
            final long messageSize =
                    (message instanceof String ? ((String) message).getBytes(Charset.defaultCharset()).length
                            : ((ByteBuffer) message).remaining());

            if (messageSize > maxMessageSize) {
                throw new MessageTooBigException(
                        Messages.get("web.socket.error.message.too.long", maxMessageSize, messageSize)
                );
            }
        }
    }

    /**
     * <p>Getter for the field <code>endpointClass</code>.</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public Class getEndpointClass() {
        return endpointClass;
    }

    /**
     * <p>getEndpoint.</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public abstract Object getEndpoint();

    /**
     * <p>getOnCloseHandle.</p>
     *
     * @return a {@link java.lang.invoke.MethodHandle} object.
     */
    public abstract MethodHandle getOnCloseHandle();

    /**
     * <p>getOnErrorHandle.</p>
     *
     * @return a {@link java.lang.invoke.MethodHandle} object.
     */
    public abstract MethodHandle getOnErrorHandle();

    /**
     * <p>getOnOpenHandle.</p>
     *
     * @return a {@link java.lang.invoke.MethodHandle} object.
     */
    public abstract MethodHandle getOnOpenHandle();

    /**
     * <p>getOnOpenParameters.</p>
     *
     * @return an array of {@link ameba.websocket.internal.EndpointMeta.ParameterExtractor} objects.
     */
    public abstract ParameterExtractor[] getOnOpenParameters();

    /**
     * <p>getOnCloseParameters.</p>
     *
     * @return an array of {@link ameba.websocket.internal.EndpointMeta.ParameterExtractor} objects.
     */
    public abstract ParameterExtractor[] getOnCloseParameters();

    /**
     * <p>getOnErrorParameters.</p>
     *
     * @return an array of {@link ameba.websocket.internal.EndpointMeta.ParameterExtractor} objects.
     */
    public abstract ParameterExtractor[] getOnErrorParameters();

    /**
     * <p>callMethod.</p>
     *
     * @param method a {@link java.lang.invoke.MethodHandle} object.
     * @param extractors an array of {@link ameba.websocket.internal.EndpointMeta.ParameterExtractor} objects.
     * @param session a {@link javax.websocket.Session} object.
     * @param callOnError a boolean.
     * @param params a {@link java.lang.Object} object.
     * @return a {@link java.lang.Object} object.
     */
    protected Object callMethod(MethodHandle method, ParameterExtractor[] extractors, Session session,
                                boolean callOnError, Object... params) {
        Object[] paramValues = new Object[extractors.length + 1];

        try {
            // TYRUS-325: Server do not close session properly if non-instantiable endpoint class is provided
            if (callOnError && getEndpoint() == null) {
                try {
                    session.close(CloseReasons.UNEXPECTED_CONDITION.getCloseReason());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                return null;
            }
            paramValues[0] = getEndpoint();
            for (int i = 0; i < extractors.length; i++) {
                paramValues[i + 1] = extractors[i].value(session, params);
            }

            return method.invokeWithArguments(paramValues);
        } catch (Throwable e) {
            if (callOnError) {
                onError(session, (e instanceof InvocationTargetException ? e.getCause() : e));
            } else {
                logger.error(Messages.get("web.socket.error.endpoint"), e);
            }
        }

        return null;
    }

    /**
     * <p>onOpen.</p>
     *
     * @param session a {@link javax.websocket.Session} object.
     * @param configuration a {@link javax.websocket.EndpointConfig} object.
     */
    @SuppressWarnings("unchecked")
    public void onOpen(Session session, EndpointConfig configuration) {
        for (MessageHandlerFactory f : messageHandlerFactories) {
            MessageHandler handler = f.create(session);
            final Class<?> handlerClass = getHandlerType(handler);

            if (handler instanceof MessageHandler.Whole) { //WHOLE MESSAGE HANDLER
                session.addMessageHandler(handlerClass, (MessageHandler.Whole) handler);
            } else if (handler instanceof MessageHandler.Partial) { // PARTIAL MESSAGE HANDLER
                session.addMessageHandler(handlerClass, (MessageHandler.Partial) handler);
            }
        }

        if (getOnOpenHandle() != null) {
            callMethod(getOnOpenHandle(), getOnOpenParameters(), session, true);
        }
    }

    /**
     * <p>onClose.</p>
     *
     * @param session a {@link javax.websocket.Session} object.
     * @param closeReason a {@link javax.websocket.CloseReason} object.
     */
    public void onClose(Session session, CloseReason closeReason) {
        if (getOnCloseHandle() != null) {
            callMethod(getOnCloseHandle(), getOnCloseParameters(), session, true, closeReason);
        }
    }

    /**
     * <p>onError.</p>
     *
     * @param session a {@link javax.websocket.Session} object.
     * @param thr a {@link java.lang.Throwable} object.
     */
    public void onError(Session session, Throwable thr) {
        if (getOnErrorHandle() != null) {
            callMethod(getOnErrorHandle(), getOnErrorParameters(), session, false, thr);
        } else {
            logger.error(Messages.get("web.socket.error"), thr);
        }
    }

    private Class getMessageType(Class type) {
        return type == ameba.websocket.PongMessage.class ? PongMessage.class : type;
    }

    protected interface ParameterExtractor {
        Object value(Session session, Object... paramValues) throws DecodeException;
    }

    protected static class ParamValue implements ParameterExtractor {
        private final int index;

        public ParamValue(int index) {
            this.index = index;
        }

        @Override
        public Object value(Session session, Object... paramValues) {
            return paramValues[index];
        }
    }

    protected abstract class MessageHandlerFactory {
        final MethodHandle method;
        final ParameterExtractor[] extractors;
        final Class<?> type;
        final long maxMessageSize;

        MessageHandlerFactory(MethodHandle method, ParameterExtractor[] extractors, Class<?> type, long maxMessageSize) {
            this.method = method;
            this.extractors = extractors;
            this.type = Primitives.isWrapperType(type)
                    ? type
                    : Primitives.wrap(type);
            this.maxMessageSize = maxMessageSize;
        }

        abstract MessageHandler create(Session session);

        protected void sendObject(final Session session, Object msg) {
            session.getAsyncRemote().sendObject(msg, result -> {
                Throwable e = result.getException();
                if (e != null) {
                    onError(session, e);
                }
            });
        }
    }

    protected class WholeHandler extends MessageHandlerFactory {
        public WholeHandler(MethodHandle method, ParameterExtractor[] extractors, Class<?> type, long maxMessageSize) {
            super(method, extractors, type, maxMessageSize);
        }

        @Override
        public MessageHandler create(final Session session) {
            return new BasicMessageHandler() {
                @Override
                public void onMessage(Object message) {
                    checkMessageSize(message, getMaxMessageSize());
                    Object result = callMethod(method, extractors, session, true, message);
                    if (result != null) {
                        sendObject(session, result);
                    }
                }

                @Override
                public Class<?> getType() {
                    return getMessageType(type);
                }

                @Override
                public long getMaxMessageSize() {
                    return maxMessageSize;
                }
            };
        }
    }

    protected class PartialHandler extends MessageHandlerFactory {
        public PartialHandler(MethodHandle method, ParameterExtractor[] extractors, Class<?> type, long maxMessageSize) {
            super(method, extractors, type, maxMessageSize);
        }

        @Override
        public MessageHandler create(final Session session) {
            return new AsyncMessageHandler() {

                @Override
                public void onMessage(Object partialMessage, boolean last) {
                    checkMessageSize(partialMessage, getMaxMessageSize());
                    Object result = callMethod(method, extractors, session, true, partialMessage, last);
                    if (result != null) {
                        sendObject(session, result);
                    }
                }

                @Override
                public Class<?> getType() {
                    return getMessageType(type);
                }

                @Override
                public long getMaxMessageSize() {
                    return maxMessageSize;
                }
            };
        }
    }
}
