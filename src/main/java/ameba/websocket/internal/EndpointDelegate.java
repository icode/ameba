package ameba.websocket.internal;

import ameba.util.ClassUtils;
import ameba.util.IOUtils;
import ameba.websocket.WebSocketException;
import com.google.common.collect.Lists;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.MethodHandler;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.spi.internal.ParameterValueHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.websocket.*;
import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
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
            boolean isPrivate = Modifier.isPrivate(method.getModifiers());
            try {
                if (isPrivate)
                    method.setAccessible(true);
                return method.invoke(target, args);
            } finally {
                if (isPrivate)
                    method.setAccessible(false);
            }
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
    private List<EventInvocation> onErrorList;
    private List<EventInvocation> onCloseList;
    private List<EventInvocation> onOpenList;

    protected void setResourceMethod(ResourceMethod resourceMethod) {
        this.resourceMethod = resourceMethod;
    }

    private void bindLocator() {
        serviceLocator = Injections.createLocator(serviceLocator,
                new ParameterInjectionBinder(messageState));
        serviceLocator.setDefaultClassAnalyzerName(ParameterInjectionBinder.CLASS_ANALYZER_NAME);
    }

    private List<EventInvocation> findEventInvocation(Class<? extends Annotation> ann, Object... instance) {
        List<EventInvocation> invocations = Lists.newArrayList();
        if (instance != null) {
            for (Object obj : instance) {
                Class clazz = obj.getClass();
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(ann)) {
                        invocations.add(EventInvocation.create(m, obj, serviceLocator));
                    }
                }
            }
        }
        return invocations;
    }

    private void initEventList(Object... instance) {
        onOpenList = findEventInvocation(OnOpen.class, instance);
        onErrorList = findEventInvocation(OnError.class, instance);
        onCloseList = findEventInvocation(OnClose.class, instance);
    }

    @Override
    public void onOpen(final Session session, final EndpointConfig config) {
        try {
            this.messageState = MessageState.builder(session, config).build();

            bindLocator();

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

    private void emmit(List<EventInvocation> eventInvocations, boolean isException) {
        try {
            if (eventInvocations != null)
                for (EventInvocation invocation : eventInvocations) {
                    if (isException) {
                        if (messageState.getThrowable() == null)
                            break;
                        boolean find = false;
                        for (Parameter parameter : invocation.getInvocable().getParameters()) {
                            if (parameter.getRawType().isAssignableFrom(messageState.getThrowable().getClass())) {
                                find = true;
                                break;
                            }
                        }
                        if (!find)
                            continue;
                    }
                    invocation.invoke();
                }
        } catch (Throwable t) {
            if (!isException)
                onError(messageState.getSession(), t);
            else
                logger.error("web socket onError has a error", t);
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        try {
            messageState.change().closeReason(closeReason);
            emmit(onCloseList, false);
        } finally {
            serviceLocator.shutdown();
        }
    }

    private void addMessageHandler(MessageHandler handler) {
        if (messageState.getSession().isOpen()) {
            Class clazz = handler.getClass();
            messageType = ClassUtils.getGenericClass(clazz);
            messageState.getSession().addMessageHandler(handler);
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

    @Override
    public void onError(Session session, Throwable thr) {
        if (thr instanceof InvocationTargetException) {
            thr = thr.getCause();
        }

        messageState.change().throwable(thr);
        emmit(onErrorList, true);

        logger.error("web socket has a err", thr);
    }

    private static class EventInvocation {
        private Invocable invocable;
        private List<Factory<?>> argsProviders;
        private ServiceLocator serviceLocator;
        private Object instance;

        private EventInvocation(Invocable invocable, ServiceLocator serviceLocator) {
            this.invocable = invocable;
            this.serviceLocator = serviceLocator;
        }

        private static EventInvocation create(Method method, Object parentInstance, ServiceLocator serviceLocator) {
            Invocable invo = Invocable.create(MethodHandler.create(parentInstance), method);
            return new EventInvocation(invo, serviceLocator);
        }

        public Invocable getInvocable() {
            return invocable;
        }

        public ServiceLocator getServiceLocator() {
            return serviceLocator;
        }

        public Object getInstance() {
            if (instance == null) {
                instance = invocable.getHandler().getInstance(serviceLocator);
            }
            return instance;
        }

        public List<Factory<?>> getArgsProviders() {
            if (argsProviders == null) {
                argsProviders = invocable.getValueProviders(serviceLocator);
            }
            return argsProviders;
        }

        public void invoke() throws InvocationTargetException, IllegalAccessException {
            final Object[] args = ParameterValueHelper.getParameterValues(getArgsProviders());
            final Method m = invocable.getHandlingMethod();
            new PrivilegedAction() {
                @Override
                public Object run() {
                    try {
                        return DEFAULT_HANDLER.invoke(getInstance(), m, args);
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
            }.run();
        }
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
