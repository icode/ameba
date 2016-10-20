package ameba.websocket;

import ameba.i18n.Messages;
import ameba.util.ClassUtils;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * @author icode
 */
public abstract class EndpointMeta {
    private static final Logger logger = LoggerFactory.getLogger(EndpointMeta.class);
    protected final Set<MessageHandlerFactory> messageHandlerFactories = Sets.newLinkedHashSet();
    private Class endpointClass;

    public EndpointMeta(Class endpointClass) {
        this.endpointClass = endpointClass;
    }

    static Class<?> getHandlerType(MessageHandler handler) {
        if (handler instanceof AsyncMessageHandler) {
            return ((AsyncMessageHandler) handler).getType();
        } else if (handler instanceof BasicMessageHandler) {
            return ((BasicMessageHandler) handler).getType();
        }
        Class<?> result = ClassUtils.getGenericClass(handler.getClass());
        return result == null ? Object.class : result;
    }

    public Class getEndpointClass() {
        return endpointClass;
    }

    public abstract Object getEndpoint();

    public abstract MethodHandle getOnCloseHandle();

    public abstract MethodHandle getOnErrorHandle();

    public abstract MethodHandle getOnOpenHandle();

    public abstract ParameterExtractor[] getOnOpenParameters();

    public abstract ParameterExtractor[] getOnCloseParameters();

    public abstract ParameterExtractor[] getOnErrorParameters();

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

    public void onClose(Session session, CloseReason closeReason) {
        if (getOnCloseHandle() != null) {
            callMethod(getOnCloseHandle(), getOnCloseParameters(), session, true, closeReason);
        }
    }

    public void onError(Session session, Throwable thr) {
        if (getOnErrorHandle() != null) {
            callMethod(getOnErrorHandle(), getOnErrorParameters(), session, false, thr);
        } else {
            logger.error(Messages.get("web.socket.error"), thr);
        }
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
            session.getAsyncRemote().sendObject(msg, new SendHandler() {
                @Override
                public void onResult(SendResult result) {
                    Throwable e = result.getException();
                    if (e != null) {
                        onError(session, e);
                    }
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
                    Object result = callMethod(method, extractors, session, true, message);
                    if (result != null) {
                        sendObject(session, result);
                    }
                }

                @Override
                public Class<?> getType() {
                    return type;
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
                    Object result = callMethod(method, extractors, session, true, partialMessage, last);
                    if (result != null) {
                        sendObject(session, result);
                    }
                }

                @Override
                public Class<?> getType() {
                    return type;
                }

                @Override
                public long getMaxMessageSize() {
                    return maxMessageSize;
                }
            };
        }
    }
}
