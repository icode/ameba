package ameba.websocket.internal;

import ameba.util.IOUtils;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import javax.inject.Inject;
import javax.websocket.EndpointConfig;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.security.Principal;

/**
 * @author icode
 */
public class ParameterInjectionBinder extends AbstractBinder {

    private Session session;
    private EndpointConfig config;
    private ThreadLocal<EndpointDelegate.MessageState> messageState;

    public ParameterInjectionBinder(Session session, EndpointConfig config, ThreadLocal<EndpointDelegate.MessageState> messageState) {
        this.session = session;
        this.config = config;
        this.messageState = messageState;
    }

    @Override
    protected void configure() {
        bindFactory(new SessionFactory()).to(Session.class);

        bind(SessionValueFactoryProvider.class).to(ValueFactoryProvider.class);

        bindFactory(new EndpointConfigFactory()).to(EndpointConfig.class);

        bind(EndpointConfigValueFactoryProvider.class).to(ValueFactoryProvider.class);

        bindFactory(new RemoteEndpointAsyncFactory())
                .to(RemoteEndpoint.Async.class)
                .to(RemoteEndpoint.class);

        bind(RemoteEndpointAsyncValueFactoryProvider.class)
                .to(ValueFactoryProvider.class);


        bindFactory(new RemoteEndpointBasicFactory()).to(RemoteEndpoint.Basic.class);
        bind(RemoteEndpointBasicValueFactoryProvider.class)
                .to(ValueFactoryProvider.class);

        bindFactory(new PrincipalFactory()).to(Principal.class);

        bind(PrincipalValueFactoryProvider.class).to(ValueFactoryProvider.class);

        bindFactory(new MessageEndFactory()).to(boolean.class).to(Boolean.class);

        bind(MessageEndValueFactoryProvider.class).to(ValueFactoryProvider.class);

        bindFactory(new MessageFactory()).to(Object.class);

        bind(MessageValueFactoryProvider.class).to(ValueFactoryProvider.class);
    }

    static class MessageValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        MessageFactory factory;

        @Inject
        protected MessageValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            return factory;
        }
    }

    static class MessageEndValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        MessageEndFactory factory;

        @Inject
        protected MessageEndValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            if (parameter.getRawType().equals(Boolean.class) || parameter.getRawType().equals(boolean.class))
                return factory;
            return null;
        }
    }


    static class SessionValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        SessionFactory factory;

        @Inject
        protected SessionValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<Session> createValueFactory(Parameter parameter) {
            if (parameter.getRawType().equals(Session.class))
                return factory;
            return null;
        }
    }

    static class EndpointConfigValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        EndpointConfigFactory factory;

        @Inject
        protected EndpointConfigValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            if (parameter.getRawType().equals(EndpointConfig.class))
                return factory;
            return null;
        }
    }

    static class RemoteEndpointAsyncValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        RemoteEndpointAsyncFactory factory;

        @Inject
        protected RemoteEndpointAsyncValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            if (parameter.getRawType().equals(RemoteEndpoint.Async.class)
                    || parameter.getRawType().equals(RemoteEndpoint.class))
                return factory;
            return null;
        }
    }

    static class RemoteEndpointBasicValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        RemoteEndpointBasicFactory factory;

        @Inject
        protected RemoteEndpointBasicValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            if (parameter.getRawType().equals(RemoteEndpoint.Basic.class))
                return factory;
            return null;
        }
    }


    static class PrincipalValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        PrincipalFactory factory;

        @Inject
        protected PrincipalValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            if (parameter.getRawType().equals(Principal.class))
                return factory;
            return null;
        }
    }


    class PrincipalFactory implements Factory<Principal> {
        @Override
        public Principal provide() {
            return session.getUserPrincipal();
        }

        @Override
        public void dispose(Principal instance) {

        }
    }

    class RemoteEndpointBasicFactory implements Factory<RemoteEndpoint.Basic> {
        @Override
        public RemoteEndpoint.Basic provide() {
            return session.getBasicRemote();
        }

        @Override
        public void dispose(RemoteEndpoint.Basic instance) {

        }
    }

    class RemoteEndpointAsyncFactory implements Factory<RemoteEndpoint.Async> {
        @Override
        public RemoteEndpoint.Async provide() {
            return session.getAsyncRemote();
        }

        @Override
        public void dispose(RemoteEndpoint.Async instance) {

        }
    }

    class EndpointConfigFactory implements Factory<EndpointConfig> {
        @Override
        public EndpointConfig provide() {
            return config;
        }

        @Override
        public void dispose(EndpointConfig instance) {

        }
    }

    class SessionFactory implements Factory<Session> {
        @Override
        public Session provide() {
            return session;
        }

        @Override
        public void dispose(Session instance) {
            if (instance.isOpen())
                IOUtils.closeQuietly(instance);
        }
    }

    class MessageFactory implements Factory<Object> {
        @Override
        public Object provide() {
            return messageState.get().getMessage();
        }

        @Override
        public void dispose(Object instance) {

        }
    }

    class MessageEndFactory implements Factory<Object> {
        @Override
        public Object provide() {
            return messageState.get().getLast();
        }

        @Override
        public void dispose(Object instance) {

        }
    }
}