package ameba.websocket.internal;

import ameba.util.ClassUtils;
import ameba.websocket.WebSocketException;
import com.google.common.collect.Lists;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.MethodHandler;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ParameterValueHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.security.PrivilegedAction;
import java.util.List;

/**
 * @author icode
 */
public abstract class EndpointDelegate extends Endpoint {

    public static final String MESSAGE_HANDLER_WHOLE_OR_PARTIAL = "MessageHandler must implement MessageHandler.Whole or MessageHandler.Partial.";
    protected static final InvocationHandler DEFAULT_HANDLER = new InvocationHandler() {
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
    static final Type MESSAGE_STATE_TYPE = (new TypeLiteral<Ref<MessageState>>() {
    }).getType();
    private static final Logger logger = LoggerFactory.getLogger(EndpointDelegate.class);
    protected List<EventInvocation> onErrorList;
    protected List<EventInvocation> onCloseList;
    protected List<EventInvocation> onOpenList;
    protected Session session;
    protected EndpointConfig endpointConfig;
    @Inject
    private ServiceLocator serviceLocator;
    @Inject
    private MessageScope messageScope;
    @Inject
    private RequestScope requestScope;
    private RequestScope.Instance reqInstance;

    public RequestScope getRequestScope() {
        return requestScope;
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public MessageScope getMessageScope() {
        return messageScope;
    }

    protected Ref<MessageState> getMessageStateRef() {
        return serviceLocator.<Ref<MessageState>>getService(MESSAGE_STATE_TYPE);
    }

    private void initMessageScope(MessageState messageState) {
        getMessageStateRef().set(messageState);
    }

    @Override
    public final void onOpen(final Session session, final EndpointConfig config) {
        this.session = session;
        this.endpointConfig = config;
        reqInstance = getRequestScope().createInstance();
        runInScope(new Runnable() {
            @Override
            public void run() {
                onOpen();
            }
        });
    }

    protected void runInScope(final Runnable task) {
        requestScope.runInScope(reqInstance, new Runnable() {
            @Override
            public void run() {
                MessageScope.Instance instance = getMessageScope().suspendCurrent();
                if (instance == null) {
                    instance = getMessageScope().createInstance();
                } else {
                    instance.release();
                }
                getMessageScope().runInScope(instance, task);
            }
        });
    }

    protected abstract void onOpen();

    protected MessageState getMessageState() {
        return getMessageStateRef().get();
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


    protected void addMessageHandler(MessageHandler handler) {
        final Class<?> handlerClass = ClassUtils.getGenericClass(handler.getClass());
        addMessageHandler(handlerClass, handler);
    }

    @SuppressWarnings("unchecked")
    protected <T> void addMessageHandler(Class<T> messageClass, final MessageHandler handler) {
        if (handler instanceof MessageHandler.Whole) { //WHOLE MESSAGE HANDLER
            serviceLocator.inject(handler);
            serviceLocator.postConstruct(handler);
            getMessageState().getSession().addMessageHandler(messageClass,
                    new BasicMessageHandler<T>((MessageHandler.Whole<T>) handler));
        } else if (handler instanceof MessageHandler.Partial) { // PARTIAL MESSAGE HANDLER
            serviceLocator.inject(handler);
            serviceLocator.postConstruct(handler);
            getMessageState().getSession().addMessageHandler(messageClass,
                    new AsyncMessageHandler<T>((MessageHandler.Partial<T>) handler));
        } else {
            throw new WebSocketException(MESSAGE_HANDLER_WHOLE_OR_PARTIAL);
        }
    }

    protected void initEventList(Object... instance) {
        onOpenList = findEventInvocation(OnOpen.class, instance);
        onErrorList = findEventInvocation(OnError.class, instance);
        onCloseList = findEventInvocation(OnClose.class, instance);
    }

    protected void emmit(List<EventInvocation> eventInvocations, boolean isException) {
        try {
            if (eventInvocations != null)
                for (EventInvocation invocation : eventInvocations) {
                    if (isException) {
                        if (getMessageState().getThrowable() == null)
                            break;
                        boolean find = false;
                        for (Parameter parameter : invocation.getInvocable().getParameters()) {
                            if (parameter.getRawType().isAssignableFrom(getMessageState().getThrowable().getClass())) {
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
                onError(getMessageState().getSession(), t);
            else
                logger.error("web socket onError has a error", t);
        }
    }

    @Override
    public final void onClose(Session session, final CloseReason closeReason) {
        try {
            runInScope(new Runnable() {
                @Override
                public void run() {
                    getMessageState().change().closeReason(closeReason);
                    try {
                        onClose();
                    } finally {
                        emmit(onCloseList, false);
                    }
                }
            });
        } finally {
            reqInstance.release();
            reqInstance = null;
        }
    }

    protected void onClose() {

    }

    @Override
    public final void onError(Session session, Throwable thr) {
        if (thr instanceof InvocationTargetException) {
            thr = thr.getCause();
        }
        final Throwable finalThr = thr;
        runInScope(new Runnable() {
            @Override
            public void run() {
                getMessageState().change().throwable(finalThr);
                try {
                    onError();
                } finally {
                    emmit(onErrorList, true);
                    logger.error("web socket has a err", finalThr);
                }
            }
        });
    }

    protected void onError() {

    }

    private MessageState.Builder createMessageStateBuilder() {
        return MessageState.builder(session, endpointConfig);
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

    protected class AsyncMessageHandler<M> implements MessageHandler.Partial<M> {


        private Partial<M> handler;

        public AsyncMessageHandler(Partial<M> handler) {
            this.handler = handler;
        }

        @Override
        public void onMessage(final M partialMessage, final boolean last) {
            messageScope.runInScope(new Runnable() {
                @Override
                public void run() {
                    MessageState state = createMessageStateBuilder()
                            .message(partialMessage)
                            .last(last).build();
                    initMessageScope(state);
                    handler.onMessage(partialMessage, last);
                }
            });
        }
    }

    protected class BasicMessageHandler<M> implements MessageHandler.Whole<M> {
        private Whole<M> handler;

        public BasicMessageHandler(Whole<M> handler) {
            this.handler = handler;
        }

        @Override
        public void onMessage(final M partialMessage) {
            messageScope.runInScope(new Runnable() {
                @Override
                public void run() {
                    MessageState state = createMessageStateBuilder()
                            .message(partialMessage).build();
                    initMessageScope(state);
                    handler.onMessage(partialMessage);
                }
            });
        }
    }

}
