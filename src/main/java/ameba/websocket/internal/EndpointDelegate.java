package ameba.websocket.internal;

import ameba.websocket.WebSocketExcption;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.spi.internal.ParameterValueHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.websocket.*;
import java.io.Reader;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.List;

/**
 * @author icode
 */
public class EndpointDelegate extends Endpoint {

    private static final Logger logger = LoggerFactory.getLogger(EndpointDelegate.class);

    private static final InvocationHandler DEFAULT_HANDLER = new InvocationHandler() {

        @Override
        public Object invoke(Object target, Method method, Object[] args)
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return method.invoke(target, args);
        }
    };
    private final ThreadLocal<MessageState> messageState = new ThreadLocal<MessageState>();
    @Inject
    private ServiceLocator serviceLocator;
    @Inject
    private Provider<ConfiguredValidator> validatorProvider;

    private ResourceMethod resourceMethod;
    private Invocable invocable;
    private List<Factory<?>> valueProviders;
    private Object resource;
    private Method method;
    Class msgType;

    public ResourceMethod getResourceMethod() {
        return resourceMethod;
    }

    protected void setResourceMethod(ResourceMethod resourceMethod) {
        this.resourceMethod = resourceMethod;
    }

    private void bindLocator(final Session session, final EndpointConfig config) {
        serviceLocator = Injections.createLocator(serviceLocator, new ParameterInjectionBinder(session, config, messageState));
    }

    @Override
    public void onOpen(final Session session, final EndpointConfig config) {
        bindLocator(session, config);

        invocable = resourceMethod.getInvocable();
        valueProviders = invocable.getValueProviders(serviceLocator);
        method = invocable.getHandlingMethod();
        final Class resourceClass = method.getDeclaringClass();
        resource = serviceLocator.createAndInitialize(resourceClass);
        Class returnType = method.getReturnType();

        int index = -1;
        boolean isAsync = false;
        boolean needMsg = false;
        for (Factory factory : valueProviders) {
            if (factory.getClass().equals(ParameterInjectionBinder.MessageEndFactory.class)) {
                isAsync = true;
            }
            if (!factory.getClass().equals(ParameterInjectionBinder.MessageFactory.class)) {
                index++;
            } else {
                needMsg = true;
            }
        }
        msgType = needMsg ? method.getParameterTypes()[index < 0 ? 0 : index] : String.class;

        if (isMessageHandler(returnType)) {// 返回的是消息处理对象，添加之
            session.addMessageHandler(this.<MessageHandler>processHandler());
        } else if (returnType.isArray() && isMessageHandler(returnType.getComponentType())) {// 返回的是一组消息处理对象，全部添加
            MessageHandler[] handlers = processHandler();
            for (MessageHandler handler : handlers) {
                session.addMessageHandler(handler);
            }
        } else if (isMessageHandlerCollection(returnType)) {// 返回的是一组消息处理对象，全部添加
            Collection<MessageHandler> handlers = processHandler();
            for (MessageHandler handler : handlers) {
                session.addMessageHandler(handler);
            }
        } else {
            MessageHandler handler = null;

            if (isAsync) {
                if (String.class.equals(msgType)) {
                    handler = new AsyncMessageHandler<String>(session) {
                    };
                } else if (ByteBuffer.class.equals(msgType)) {
                    handler = new AsyncMessageHandler<ByteBuffer>(session) {
                    };
                } else if (byte.class.equals(msgType.getComponentType())) {
                    handler = new AsyncMessageHandler<byte[]>(session) {
                    };
                } else {
                    handler = new AsyncMessageHandler<Object>(session) {
                    };
                }
            } else {
                if (String.class.equals(msgType)) {
                    handler = new BasicMessageHandler<String>(session) {
                    };
                } else if (Reader.class.equals(msgType)) {
                    handler = new BasicMessageHandler<Reader>(session) {
                    };
                } else if (ByteBuffer.class.equals(msgType)) {
                    handler = new BasicMessageHandler<ByteBuffer>(session) {
                    };
                } else if (byte.class.equals(msgType.getComponentType())) {
                    handler = new BasicMessageHandler<byte[]>(session) {
                    };
                } else {
                    handler = new BasicMessageHandler<Object>(session) {
                    };
                }
            }

            session.addMessageHandler(handler);
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        serviceLocator.shutdown();
    }

    private boolean isMessageHandler(Class clazz) {
        return MessageHandler.class.isAssignableFrom(clazz);
    }

    private boolean isMessageHandlerCollection(Class clazz) {
        if (Collection.class.isAssignableFrom(clazz)) {
            Type methodGenericReturnType = method.getGenericReturnType();
            if (methodGenericReturnType instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) methodGenericReturnType).getActualTypeArguments();
                if (types.length > 0 && isMessageHandler((Class) types[0])) {
                    // 返回的是一组消息处理对象
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private <H> H processHandler() {

        final ConfiguredValidator validator = validatorProvider.get();

        final Object[] args = ParameterValueHelper.getParameterValues(valueProviders);

        // Validate resource class & method input parameters.
        if (validator != null) {
            validator.validateResourceAndInputParams(resource, invocable, args);
        }

        final PrivilegedAction invokeMethodAction = new PrivilegedAction() {
            @Override
            public Object run() {
                try {
                    return DEFAULT_HANDLER.invoke(resource, method, args);
                } catch (Throwable t) {
                    throw new WebSocketExcption(t);
                }
            }
        };

        final Object invocationResult = invokeMethodAction.run();

        // Validate response entity.
        if (validator != null) {
            validator.validateResult(resource, invocable, invocationResult);
        }

        return (H) invocationResult;
    }

    @Override
    public void onError(Session session, Throwable thr) {
        logger.error("web socket has a err", thr);
    }

    private class AsyncMessageHandler<M> implements MessageHandler.Partial<M> {

        Session session;

        private AsyncMessageHandler(Session session) {
            this.session = session;
        }

        @Override
        public void onMessage(M partialMessage, boolean last) {
            messageState.set(new MessageState(partialMessage, last));
            session.getAsyncRemote().sendObject(processHandler());
        }
    }

    private class BasicMessageHandler<M> implements MessageHandler.Whole<M> {

        Session session;

        private BasicMessageHandler(Session session) {
            this.session = session;
        }

        @Override
        public void onMessage(M partialMessage) {
            messageState.set(new MessageState(partialMessage));
            session.getAsyncRemote().sendObject(processHandler());
        }
    }

    public class MessageHandlerDelegate {
        boolean async;

        public MessageHandlerDelegate(boolean async) {
            this.async = async;
        }

        public boolean isAsync() {
            return async;
        }

        Object process() {
            return processHandler();
        }
    }

    class MessageState {
        private Object message;
        private Boolean last;

        MessageState(Object message, Boolean last) {
            this.message = message;
            this.last = last;
        }

        MessageState(Object message) {
            this.message = message;
        }

        public Object getMessage() {
            return message;
        }

        public Boolean getLast() {
            return last;
        }
    }
}
