package ameba.websocket.internal;

import org.apache.commons.lang3.StringUtils;
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
import javax.websocket.server.PathParam;
import java.security.Principal;

/**
 * @author icode
 */
public class ParameterInjectionBinder extends AbstractBinder {

    private MessageState messageState;

    public ParameterInjectionBinder(MessageState messageState) {
        this.messageState = messageState;
    }

    @Override
    protected void configure() {
        bindFactory(new MessageStateFactory(messageState)).to(MessageState.class);

        bind(PathParamValueFactoryProvider.class).to(ValueFactoryProvider.class);
        bind(MessageStateValueFactoryProvider.class).to(ValueFactoryProvider.class);
        bind(BasicRemoteEndpointValueFactoryProvider.class).to(ValueFactoryProvider.class);
        bind(AsyncRemoteEndpointValueFactoryProvider.class).to(ValueFactoryProvider.class);
        bind(EndpointConfigValueFactoryProvider.class).to(ValueFactoryProvider.class);
        bind(SessionValueFactoryProvider.class).to(ValueFactoryProvider.class);
        bind(PrincipalValueFactoryProvider.class).to(ValueFactoryProvider.class);
        bind(MessageEndValueFactoryProvider.class).to(ValueFactoryProvider.class);

        bind(MessageValueFactoryProvider.class).to(ValueFactoryProvider.class);
    }


    static class MessageEndValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        MessageState state;

        MessageEndFactory messageEndFactory;

        @Inject
        protected MessageEndValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY, Parameter.Source.UNKNOWN);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(Boolean.class) || type.equals(boolean.class))
                return messageEndFactory == null ? (messageEndFactory = new MessageEndFactory(state)) : messageEndFactory;

            return null;
        }
    }

    static class PrincipalValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        MessageState state;

        PrincipalFactory principalFactory;

        @Inject
        protected PrincipalValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY, Parameter.Source.UNKNOWN);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(Principal.class))
                return principalFactory == null ? (principalFactory = new PrincipalFactory(state)) : principalFactory;

            return null;
        }
    }

    static class SessionValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        MessageState state;

        SessionFactory sessionFactory;

        @Inject
        protected SessionValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY, Parameter.Source.UNKNOWN);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(Session.class))
                return sessionFactory == null ? (sessionFactory = new SessionFactory(state)) : sessionFactory;

            return null;
        }
    }

    static class EndpointConfigValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        MessageState state;

        EndpointConfigFactory endpointConfigFactory;

        @Inject
        protected EndpointConfigValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY, Parameter.Source.UNKNOWN);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(EndpointConfig.class))
                return endpointConfigFactory == null ? (endpointConfigFactory = new EndpointConfigFactory(state)) : endpointConfigFactory;

            return null;
        }
    }

    static class AsyncRemoteEndpointValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        MessageState state;

        AsyncRemoteEndpointFactory asyncRemoteEndpointFactory;

        @Inject
        protected AsyncRemoteEndpointValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY, Parameter.Source.UNKNOWN);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(RemoteEndpoint.Async.class)
                    || type.equals(RemoteEndpoint.class))
                return asyncRemoteEndpointFactory == null ? (asyncRemoteEndpointFactory = new AsyncRemoteEndpointFactory(state)) : asyncRemoteEndpointFactory;

            return null;
        }
    }

    static class BasicRemoteEndpointValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        MessageState state;

        BasicRemoteEndpointFactory basicRemoteEndpointFactory;

        @Inject
        protected BasicRemoteEndpointValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY, Parameter.Source.UNKNOWN);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();
            if (type.equals(RemoteEndpoint.Basic.class))
                return basicRemoteEndpointFactory == null ? (basicRemoteEndpointFactory = new BasicRemoteEndpointFactory(state)) : basicRemoteEndpointFactory;

            return null;
        }
    }

    static class MessageStateValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        MessageStateFactory messageStatefactory;

        @Inject
        protected MessageStateValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY, Parameter.Source.UNKNOWN);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();
            if (type.equals(MessageState.class))
                return messageStatefactory;

            return null;
        }
    }

    static class PathParamValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        MessageState state;
        PathParamFactory pathParamFactory;

        @Inject
        protected PathParamValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.PATH, Parameter.Source.ENTITY, Parameter.Source.UNKNOWN);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (!type.equals(String.class))
                return null;
            
            javax.ws.rs.PathParam pathParamRs = parameter.getAnnotation(javax.ws.rs.PathParam.class);

            if (pathParamRs != null && StringUtils.isNotBlank(pathParamRs.value()))
                return pathParamFactory == null ? (pathParamFactory = new PathParamFactory(pathParamRs.value(), state)) : pathParamFactory;
            else {
                PathParam pathParam = parameter.getAnnotation(PathParam.class);
                if (pathParam != null && StringUtils.isNotBlank(pathParam.value()))
                    return pathParamFactory == null ? (pathParamFactory = new PathParamFactory(pathParam.value(), state)) : pathParamFactory;
            }
            return null;
        }
    }

    static class MessageValueFactoryProvider extends AbstractValueFactoryProvider {


        MessageFactory messageFactory;

        @Inject
        protected MessageValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator,
                                              MessageState state) {
            super(mpep, locator, Parameter.Source.ENTITY, Parameter.Source.UNKNOWN);
            messageFactory = new MessageFactory(state);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {

            Object msg = messageFactory.provide();
            if (msg == null || parameter.getRawType().isAssignableFrom(msg.getClass()))
                return messageFactory;

            return null;
        }
    }

    private static abstract class AbstractValueFactory<V> implements Factory<V> {
        MessageState messageState;

        AbstractValueFactory(MessageState messageState) {
            this.messageState = messageState;
        }

        @Override
        public void dispose(V instance) {
            // not use
        }
    }

    static class PathParamFactory extends AbstractValueFactory<String> {

        String key;

        PathParamFactory(String key, MessageState messageState) {
            super(messageState);
            this.key = key;
        }

        @Override
        public String provide() {
            return messageState.getSession().getPathParameters().get(key);
        }
    }

    static class MessageFactory extends AbstractValueFactory<Object> {


        MessageFactory(MessageState messageState) {
            super(messageState);
        }

        @Override
        public Object provide() {
            return messageState.getMessage();
        }
    }

    static class PrincipalFactory extends AbstractValueFactory<Principal> {
        PrincipalFactory(MessageState messageState) {
            super(messageState);
        }

        @Override
        public Principal provide() {
            return messageState.getSession().getUserPrincipal();
        }
    }

    static class BasicRemoteEndpointFactory extends AbstractValueFactory<RemoteEndpoint.Basic> {
        BasicRemoteEndpointFactory(MessageState messageState) {
            super(messageState);
        }

        @Override
        public RemoteEndpoint.Basic provide() {
            return messageState.getSession().getBasicRemote();
        }
    }

    static class AsyncRemoteEndpointFactory extends AbstractValueFactory<RemoteEndpoint.Async> {
        AsyncRemoteEndpointFactory(MessageState messageState) {
            super(messageState);
        }

        @Override
        public RemoteEndpoint.Async provide() {
            return messageState.getSession().getAsyncRemote();
        }
    }

    static class EndpointConfigFactory extends AbstractValueFactory<EndpointConfig> {
        EndpointConfigFactory(MessageState messageState) {
            super(messageState);
        }

        @Override
        public EndpointConfig provide() {
            return messageState.getEndpointConfig();
        }
    }

    static class SessionFactory extends AbstractValueFactory<Session> {
        SessionFactory(MessageState messageState) {
            super(messageState);
        }

        @Override
        public Session provide() {
            return messageState.getSession();
        }
    }

    static class MessageEndFactory extends AbstractValueFactory<Object> {
        MessageEndFactory(MessageState messageState) {
            super(messageState);
        }

        @Override
        public Object provide() {
            return messageState.getLast();
        }
    }

    static class MessageStateFactory extends AbstractValueFactory<MessageState> {
        MessageStateFactory(MessageState messageState) {
            super(messageState);
        }

        @Override
        public MessageState provide() {
            return messageState;
        }
    }
}