package ameba.websocket.internal;

import ameba.util.IOUtils;
import ameba.websocket.WebSocketExcption;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.spi.internal.ParameterValueHelper;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.websocket.*;
import java.lang.reflect.*;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Collection;

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
    @Inject
    private ServiceLocator serviceLocator;
    @Inject
    private Provider<ConfiguredValidator> validatorProvider;
    private ResourceMethod resourceMethod;
    private Invocable invocable;
    private static final ThreadLocal<Object> messageLocal = new ThreadLocal<Object>();

    public ResourceMethod getResourceMethod() {
        return resourceMethod;
    }

    protected void setResourceMethod(ResourceMethod resourceMethod) {
        this.resourceMethod = resourceMethod;
    }

    private void bindLocator(final Session session, final EndpointConfig config) {
        serviceLocator = Injections.createLocator(serviceLocator, new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(new Factory<Session>() {
                    @Override
                    public Session provide() {
                        return session;
                    }

                    @Override
                    public void dispose(Session instance) {
                        if (instance.isOpen())
                            IOUtils.closeQuietly(instance);
                    }
                });

                bindFactory(new Factory<EndpointConfig>() {
                    @Override
                    public EndpointConfig provide() {
                        return config;
                    }

                    @Override
                    public void dispose(EndpointConfig instance) {

                    }
                });


                bindFactory(new Factory<RemoteEndpoint.Async>() {
                    @Override
                    public RemoteEndpoint.Async provide() {
                        return session.getAsyncRemote();
                    }

                    @Override
                    public void dispose(RemoteEndpoint.Async instance) {

                    }
                });

                bindFactory(new Factory<RemoteEndpoint>() {
                    @Override
                    public RemoteEndpoint provide() {
                        return session.getAsyncRemote();
                    }

                    @Override
                    public void dispose(RemoteEndpoint instance) {

                    }
                });

                bindFactory(new Factory<RemoteEndpoint.Basic>() {
                    @Override
                    public RemoteEndpoint.Basic provide() {
                        return session.getBasicRemote();
                    }

                    @Override
                    public void dispose(RemoteEndpoint.Basic instance) {

                    }
                });

                bindFactory(new Factory<Principal>() {
                    @Override
                    public Principal provide() {
                        return session.getUserPrincipal();
                    }

                    @Override
                    public void dispose(Principal instance) {

                    }
                });

                bindFactory(new MessageFactory());

                bind(MessageValueFactoryProvider.class).to(ValueFactoryProvider.class).ranked(100);
            }
        });
    }

    @Override
    public void onOpen(final Session session, final EndpointConfig config) {
        bindLocator(session, config);

        invocable = resourceMethod.getInvocable();
        final Method method = invocable.getHandlingMethod();
        final Class clazz = method.getDeclaringClass();
        Class returnType = method.getReturnType();

        if (isMessageHandler(returnType)) {// 返回的是消息处理对象，添加之
            session.addMessageHandler(this.<MessageHandler>processHandler(clazz, method));
        } else if (returnType.isArray() && isMessageHandler(returnType.getComponentType())) {// 返回的是一组消息处理对象，全部添加
            MessageHandler[] handlers = processHandler(clazz, method);
            for (MessageHandler handler : handlers) {
                session.addMessageHandler(handler);
            }
        } else if (isMessageHandlerCollection(returnType, method)) {// 返回的是一组消息处理对象，全部添加
            Collection<MessageHandler> handlers = processHandler(clazz, method);
            for (MessageHandler handler : handlers) {
                session.addMessageHandler(handler);
            }
        } else {
            session.addMessageHandler(new MessageHandler.Whole() {
                @Override
                public void onMessage(Object message) {
                    messageLocal.set(message);
                    session.getAsyncRemote().sendObject(processHandler(clazz, method));
                }
            });
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        serviceLocator.shutdown();
    }

    private boolean isMessageHandler(Class clazz) {
        return MessageHandler.class.isAssignableFrom(clazz);
    }

    private boolean isMessageHandlerCollection(Class clazz, Method method) {
        if (Collection.class.isAssignableFrom(clazz)) {
            Type genericReturnType = method.getGenericReturnType();
            if (genericReturnType instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) genericReturnType).getActualTypeArguments();
                if (types.length > 0 && isMessageHandler((Class) types[0])) {
                    // 返回的是一组消息处理对象
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private <H> H processHandler(Class clazz, final Method method) {

        final Object resource = serviceLocator.createAndInitialize(clazz);
        final ConfiguredValidator validator = validatorProvider.get();

        final Object[] args = ParameterValueHelper.getParameterValues(invocable.getValueProviders(serviceLocator));

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

    static class MessageFactory implements Factory<Object> {
        @Override
        public Object provide() {
            return messageLocal.get();
        }

        @Override
        public void dispose(Object instance) {

        }
    }

    static class MessageValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        protected MessageValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            return new MessageFactory();
        }
    }
}
