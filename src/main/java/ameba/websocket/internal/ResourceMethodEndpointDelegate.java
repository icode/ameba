package ameba.websocket.internal;

import ameba.util.ClassUtils;
import ameba.util.IOUtils;
import ameba.websocket.WebSocketException;
import org.glassfish.hk2.api.Factory;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.spi.internal.ParameterValueHelper;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.websocket.EncodeException;
import javax.websocket.MessageHandler;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.List;

/**
 * @author icode
 */
public class ResourceMethodEndpointDelegate extends EndpointDelegate {
    @Inject
    private Provider<ConfiguredValidator> validatorProvider;
    private ResourceMethod resourceMethod;
    private Invocable invocable;
    private List<Factory<?>> onMessageValueProviders;
    private Object resourceInstance;
    private Method method;

    protected void setResourceMethod(ResourceMethod resourceMethod) {
        this.resourceMethod = resourceMethod;
    }

    @Override
    protected void onOpen() {
        try {
            invocable = resourceMethod.getInvocable();
            method = invocable.getHandlingMethod();
            final Class resourceClass = invocable.getHandler().getHandlerClass();
            resourceInstance = getServiceLocator().getService(resourceClass);
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

                int index = -1;
                boolean isAsync = false;
                boolean needMsg = false;
                onMessageValueProviders = invocable.getValueProviders(getServiceLocator());

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
                Class<?> messageType = needMsg ? method.getParameterTypes()[index < 0 ? 0 : index] : String.class;
                if (isAsync) {
                    addMessageHandler(messageType, new MessageHandler.Partial() {
                        @Override
                        public void onMessage(Object partialMessage, boolean last) {
                            getMessageState().getSession().getAsyncRemote()
                                    .sendObject(processHandler());
                        }
                    });
                } else {
                    addMessageHandler(messageType, new MessageHandler.Whole() {
                        @Override
                        public void onMessage(Object message) {
                            try {
                                getMessageState().getSession().getBasicRemote()
                                        .sendObject(processHandler());
                            } catch (IOException e) {
                                throw new WebSocketException(e);
                            } catch (EncodeException e) {
                                throw new WebSocketException(e);
                            }
                        }
                    });
                }
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

        if (onMessageValueProviders == null) onMessageValueProviders = invocable.getValueProviders(getServiceLocator());
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
}
