package ameba.websocket.internal;

import ameba.util.ClassUtils;
import ameba.util.IOUtils;
import ameba.websocket.WebSocketException;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.spi.internal.ParameterValueHelper;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.List;

/**
 * @author icode
 */
public class ResourceMethodEndpointDelegate extends EndpointDelegate {

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

    @Override
    public void onOpen(final Session session, final EndpointConfig config) {
        try {
            initMessageScope(session, config);

            invocable = resourceMethod.getInvocable();
            method = invocable.getHandlingMethod();
            Class resourceClass = invocable.getHandler().getHandlerClass();
            resourceInstance = serviceLocator.getService(resourceClass);
            Class returnType = method.getReturnType();

            if (isMessageHandler(returnType)) {// 返回的是消息处理对象，添加之
                MessageHandler handler = processHandler();
                if (handler != null) {
                    initEventList(handler);
                    addMessageHandler(handler);
                }
            } else if (returnType.isArray() && isMessageHandler(returnType.getComponentType())) {// 返回的是一组消息处理对象，全部添加
                MessageHandler[] handlers = processHandler();
                if (handlers != null) {
                    initEventList(handlers);
                    for (MessageHandler handler : handlers) {
                        addMessageHandler(handler);
                    }
                }
            } else if (isMessageHandlerCollection(returnType)) {// 返回的是一组消息处理对象，全部添加
                Collection<MessageHandler> handlers = processHandler();
                if (handlers != null) {
                    initEventList(handlers.toArray());
                    for (MessageHandler handler : handlers) {
                        addMessageHandler(handler);
                    }
                }
            } else {
                initEventList(resourceInstance);

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
                        throw new WebSocketException("Async message handler arguments can't be of type: " + messageType
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

            if (session.isOpen())
                emmit(onOpenList, false);
        } catch (Throwable e) {
            if (session.isOpen())
                IOUtils.closeQuietly(session);
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            throw new WebSocketException(e);
        }
    }

    private void addMessageHandler(MessageHandler handler) {
        if (getMessageState().getSession().isOpen()) {
            Class clazz = handler.getClass();
            messageType = ClassUtils.getGenericClass(clazz);
            serviceLocator.inject(handler);
            serviceLocator.postConstruct(handler);
            getMessageState().getSession().addMessageHandler(handler);
        }
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
                    if (t instanceof InvocationTargetException) {
                        t = t.getCause();
                    }
                    if (t instanceof RuntimeException) {
                        throw (RuntimeException) t;
                    }
                    throw new WebSocketException(t.getMessage(), t);
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

    private class AsyncMessageHandler<M> implements MessageHandler.Partial<M> {
        @Override
        public void onMessage(M partialMessage, boolean last) {
            initMessageScope(getMessageState());
            getMessageState().change().message(partialMessage)
                    .last(last).build()
                    .getSession().getAsyncRemote().sendObject(processHandler());
        }
    }

    private class BasicMessageHandler<M> implements MessageHandler.Whole<M> {
        @Override
        public void onMessage(M partialMessage) {
            initMessageScope(getMessageState());
            getMessageState().change().message(partialMessage).build()
                    .getSession().getAsyncRemote().sendObject(processHandler());
        }
    }
}
