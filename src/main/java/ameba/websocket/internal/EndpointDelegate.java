package ameba.websocket.internal;

import ameba.util.ClassUtils;
import ameba.util.IOUtils;
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
import java.io.InputStream;
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
    private MessageState messageState;
    @Inject
    private ServiceLocator serviceLocator;
    @Inject
    private Provider<ConfiguredValidator> validatorProvider;
    private ResourceMethod resourceMethod;
    private Invocable invocable;
    private List<Factory<?>> onMessageValueProviders;
    private Object resourceInstance;
    private Method method;
    private Class<?> messageType;

    protected void setResourceMethod(ResourceMethod resourceMethod) {
        this.resourceMethod = resourceMethod;
    }

    private void bindLocator() {
        serviceLocator = Injections.createLocator(serviceLocator,
                new ParameterInjectionBinder(messageState));
    }

    @Override
    public void onOpen(final Session session, final EndpointConfig config) {
        try {
            this.messageState = MessageState.builder(session, config).build();

            bindLocator();

            invocable = resourceMethod.getInvocable();
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
                MessageHandler handler;
                int index = -1;
                boolean isAsync = false;
                boolean needMsg = false;
                onMessageValueProviders = invocable.getValueProviders(serviceLocator);

                for (Factory factory : onMessageValueProviders) {
                    if (factory.getClass().equals(ParameterInjectionBinder.MessageEndFactory.class)) {
                        isAsync = true;
                    }
                    if (!needMsg)
                        if (!factory.getClass().equals(ParameterInjectionBinder.MessageFactory.class)) {
                            index++;
                        } else {
                            needMsg = true;
                        }
                }
                messageType = needMsg ? method.getParameterTypes()[index < 0 ? 0 : index] : String.class;

                messageState.change().async(isAsync);

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
                    } else if (InputStream.class.equals(messageType)) {
                        handler = new AsyncMessageHandler<InputStream>() {
                        };
                    } else {
                        throw new WebSocketExcption("Async message handler arguments can't be of type: " + messageType
                                + ". Must be String, ByteBuffer, byte[] or InputStream.");
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
        } catch (Throwable e) {
            IOUtils.closeQuietly(session);
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            throw new WebSocketExcption(e);
        }
    }

    private void onClose() {

    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        try {
            messageState.change().closeReason(closeReason);
            onClose();
        } finally {
            serviceLocator.shutdown();
        }
    }

    private void addMessageHandler(MessageHandler handler) {
        messageType = ClassUtils.getGenericClass(handler.getClass());
        messageState.getSession().addMessageHandler(handler);
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

        if (onMessageValueProviders == null) onMessageValueProviders = invocable.getValueProviders(serviceLocator);
        final Object[] args = ParameterValueHelper.getParameterValues(onMessageValueProviders);

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
                    throw new WebSocketExcption(t.getMessage(), t);
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
        if (thr instanceof InvocationTargetException) {
            thr = thr.getCause();
        }

        logger.error("web socket has a err", thr);
    }

    private class AsyncMessageHandler<M> implements MessageHandler.Partial<M> {

        @Override
        public void onMessage(M partialMessage, boolean last) {
            messageState.change().message(partialMessage)
                    .last(last).build()
                    .getSession().getAsyncRemote().sendObject(processHandler());
        }
    }

    private class BasicMessageHandler<M> implements MessageHandler.Whole<M> {
        @Override
        public void onMessage(M partialMessage) {
            messageState.change().message(partialMessage).build()
                    .getSession().getAsyncRemote().sendObject(processHandler());
        }
    }
}
