package ameba.websocket.internal;

import ameba.util.ClassUtils;
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
    @Inject
    private ResourceMethod resourceMethod;
    private Invocable invocable;
    private List<Factory<?>> valueProviders;
    private Object resourceInstance;
    private Method method;
    private EndpointConfig endpointConfig;
    private Session session;
    private Class<?> messageType;

    protected void setResourceMethod(ResourceMethod resourceMethod) {
        this.resourceMethod = resourceMethod;
    }

    private void bindLocator() {
        serviceLocator = Injections.createLocator(serviceLocator,
                new ParameterInjectionBinder(session, endpointConfig, messageState));
    }

    @Override
    public void onOpen(final Session session, final EndpointConfig config) {
        this.endpointConfig = config;
        this.session = session;
        bindLocator();

        invocable = resourceMethod.getInvocable();
        valueProviders = invocable.getValueProviders(serviceLocator);
        method = invocable.getHandlingMethod();
        Class resourceClass = method.getDeclaringClass();
        resourceInstance = serviceLocator.createAndInitialize(resourceClass);
        Class returnType = method.getReturnType();

        if (isMessageHandler(returnType)) {// 返回的是消息处理对象，添加之
            MessageHandler handler = this.processHandler();
            addMessageHandler(handler);
        } else if (returnType.isArray() && isMessageHandler(returnType.getComponentType())) {// 返回的是一组消息处理对象，全部添加
            MessageHandler[] handlers = processHandler();
            for (MessageHandler handler : handlers) {
                addMessageHandler(handler);
            }
        } else if (isMessageHandlerCollection(returnType)) {// 返回的是一组消息处理对象，全部添加
            Collection<MessageHandler> handlers = processHandler();
            for (MessageHandler handler : handlers) {
                addMessageHandler(handler);
            }
        } else {
            MessageHandler handler = null;
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
            messageType = needMsg ? method.getParameterTypes()[index < 0 ? 0 : index] : String.class;

            if (isAsync) {
                if (String.class.equals(messageType)) {
                    handler = new AsyncMessageHandler<String>() {
                    };
                } else if (ByteBuffer.class.equals(messageType)) {
                    handler = new AsyncMessageHandler<ByteBuffer>() {
                    };
                } else if (byte.class.equals(messageType.getComponentType())) {
                    handler = new AsyncMessageHandler<byte[]>() {
                    };
                } else {
                    handler = new AsyncMessageHandler<Object>() {
                    };
                }
            } else {
                if (String.class.equals(messageType)) {
                    handler = new BasicMessageHandler<String>() {
                    };
                } else if (Reader.class.equals(messageType)) {
                    handler = new BasicMessageHandler<Reader>() {
                    };
                } else if (ByteBuffer.class.equals(messageType)) {
                    handler = new BasicMessageHandler<ByteBuffer>() {
                    };
                } else if (byte.class.equals(messageType.getComponentType())) {
                    handler = new BasicMessageHandler<byte[]>() {
                    };
                } else {
                    handler = new BasicMessageHandler<Object>() {
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

    private void addMessageHandler(MessageHandler handler){
        messageType = ClassUtils.getGenericClass(handler.getClass());
        session.addMessageHandler(handler);
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
            validator.validateResourceAndInputParams(resourceInstance, invocable, args);
        }

        final PrivilegedAction invokeMethodAction = new PrivilegedAction() {
            @Override
            public Object run() {
                try {
                    return DEFAULT_HANDLER.invoke(resourceInstance, method, args);
                } catch (Throwable t) {
                    throw new WebSocketExcption(t);
                }
            }
        };

        final Object invocationResult = invokeMethodAction.run();

        // Validate response entity.
        if (validator != null) {
            validator.validateResult(resourceInstance, invocable, invocationResult);
        }

        return (H) invocationResult;
    }

    @Override
    public void onError(Session session, Throwable thr) {
        logger.error("web socket has a err", thr);
    }

    private class AsyncMessageHandler<M> implements MessageHandler.Partial<M> {

        @Override
        public void onMessage(M partialMessage, boolean last) {
            messageState.set(new MessageState(partialMessage, last));
            session.getAsyncRemote().sendObject(processHandler());
        }
    }

    private class BasicMessageHandler<M> implements MessageHandler.Whole<M> {
        @Override
        public void onMessage(M partialMessage) {
            messageState.set(new MessageState(partialMessage));
            session.getAsyncRemote().sendObject(processHandler());
        }
    }

    class MessageState {
        private Object message;
        private Boolean last;
        private Throwable throwable;

        MessageState(Object message, Boolean last) {
            this.message = message;
            this.last = last;
        }

        MessageState(Object message) {
            this.message = message;
        }

        MessageState(MessageState state, Throwable throwable) {
            this.message = state.message;
            this.last = state.last;
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public Object getMessage() {
            return message;
        }

        public Boolean getLast() {
            return last;
        }

        public Session getSession() {
            return session;
        }

        public EndpointConfig getEndpointConfig() {
            return endpointConfig;
        }
    }
}
