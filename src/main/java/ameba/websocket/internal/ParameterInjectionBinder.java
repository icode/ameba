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
    private ThreadLocal<Object> messageLocal;

    public ParameterInjectionBinder(Session session, EndpointConfig config, ThreadLocal<Object> messageLocal) {
        this.session = session;
        this.config = config;
        this.messageLocal = messageLocal;
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

        bindFactory(new MessageFactory()).to(Object.class);

        bind(MessageValueFactoryProvider.class).to(ValueFactoryProvider.class);
    }

    private static class MessageValueFactoryProvider extends AbstractValueFactoryProvider {

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

    private static class SessionValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        SessionFactory factory;

        @Inject
        protected SessionValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            return factory;
        }
    }

    private static class EndpointConfigValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        EndpointConfigFactory factory;

        @Inject
        protected EndpointConfigValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            return factory;
        }
    }

    private static class RemoteEndpointAsyncValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        RemoteEndpointAsyncFactory factory;

        @Inject
        protected RemoteEndpointAsyncValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            return factory;
        }
    }

    private static class RemoteEndpointBasicValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        RemoteEndpointBasicFactory factory;

        @Inject
        protected RemoteEndpointBasicValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            return factory;
        }
    }


    private static class PrincipalValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        PrincipalFactory factory;

        @Inject
        protected PrincipalValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            return factory;
        }
    }


    private class PrincipalFactory implements Factory<Principal> {
        @Override
        public Principal provide() {
            return session.getUserPrincipal();
        }

        @Override
        public void dispose(Principal instance) {

        }
    }

    private class RemoteEndpointBasicFactory implements Factory<RemoteEndpoint.Basic> {
        @Override
        public RemoteEndpoint.Basic provide() {
            return session.getBasicRemote();
        }

        @Override
        public void dispose(RemoteEndpoint.Basic instance) {

        }
    }

    private class RemoteEndpointAsyncFactory implements Factory<RemoteEndpoint.Async> {
        @Override
        public RemoteEndpoint.Async provide() {
            return session.getAsyncRemote();
        }

        @Override
        public void dispose(RemoteEndpoint.Async instance) {

        }
    }

    private class EndpointConfigFactory implements Factory<EndpointConfig> {
        @Override
        public EndpointConfig provide() {
            return config;
        }

        @Override
        public void dispose(EndpointConfig instance) {

        }
    }

    private class SessionFactory implements Factory<Session> {
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

    private class MessageFactory implements Factory<Object> {
        @Override
        public Object provide() {
            return messageLocal.get();
        }

        @Override
        public void dispose(Object instance) {

        }
    }
}