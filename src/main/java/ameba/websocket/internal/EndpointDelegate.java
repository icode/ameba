package ameba.websocket.internal;

import ameba.websocket.WebSocketException;
import com.google.common.collect.Lists;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.jersey.internal.util.collection.Ref;
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

    static final Type MESSAGE_STATE_TYPE = (new TypeLiteral<Ref<MessageState>>() {
    }).getType();
    private static final Logger logger = LoggerFactory.getLogger(EndpointDelegate.class);
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
    @Inject
    private ServiceLocator serviceLocator;
    @Inject
    private MessageScope messageScope;
    protected List<EventInvocation> onErrorList;
    protected List<EventInvocation> onCloseList;
    protected List<EventInvocation> onOpenList;

    protected void initMessageScope(final Session session, final EndpointConfig config) {
        getMessageStateRef().set(MessageState.builder(session, config).build());
    }

    protected void initMessageScope(MessageState messageState) {
        getMessageStateRef().set(MessageState.from(messageState).build());
    }

    protected Ref<MessageState> getMessageStateRef() {
        return serviceLocator.<Ref<MessageState>>getService(MESSAGE_STATE_TYPE);
    }

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
    public void onClose(Session session, CloseReason closeReason) {
        try {
            getMessageState().change().closeReason(closeReason);
            emmit(onCloseList, false);
        } finally {
            serviceLocator.shutdown();
        }
    }

    @Override
    public void onError(Session session, Throwable thr) {
        if (thr instanceof InvocationTargetException) {
            thr = thr.getCause();
        }

        getMessageState().change().throwable(thr);
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

}
