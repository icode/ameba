package ameba.websocket.internal;

import org.apache.commons.lang3.ArrayUtils;
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
import javax.ws.rs.QueryParam;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.List;

/**
 * @author icode
 */
public class ParameterInjectionBinder extends AbstractBinder {

    private static final String PATH_PARAM_ERR_MSG = "@PathParam parameter class must be String.";
    private static final String QUERY_PARAM_ERR_MSG = "@QueryParam parameter class must be String, String[] or List<String>";
    private MessageState messageState;

    public ParameterInjectionBinder(MessageState messageState) {
        this.messageState = messageState;
    }

    private static Parameter.Source[] addDefaultSources(Parameter.Source... sources) {
        Parameter.Source[] defaults = new Parameter.Source[]{Parameter.Source.ENTITY, Parameter.Source.UNKNOWN};
        return sources == null || sources.length == 0 ? defaults : ArrayUtils.addAll(defaults, sources);
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
        bind(QueryParamValueFactoryProvider.class).to(ValueFactoryProvider.class);
        bind(QueryStringValueFactoryProvider.class).to(ValueFactoryProvider.class);
    }

    static void createMessageValueLocal(){

    }

    static class MessageEndValueFactoryProvider extends WebSocketValueFactoryProvider {

        MessageEndFactory messageEndFactory;

        @Inject
        protected MessageEndValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(Boolean.class) || type.equals(boolean.class))
                return messageEndFactory == null ? (messageEndFactory = new MessageEndFactory()) : messageEndFactory;

            return null;
        }
    }

    static class PrincipalValueFactoryProvider extends WebSocketValueFactoryProvider {
        PrincipalFactory principalFactory;

        @Inject
        protected PrincipalValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(Principal.class))
                return principalFactory == null ? (principalFactory = new PrincipalFactory()) : principalFactory;

            return null;
        }
    }

    static class SessionValueFactoryProvider extends WebSocketValueFactoryProvider {

        SessionFactory sessionFactory;

        @Inject
        protected SessionValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(Session.class))
                return sessionFactory == null ? (sessionFactory = new SessionFactory()) : sessionFactory;

            return null;
        }
    }

    static class EndpointConfigValueFactoryProvider extends WebSocketValueFactoryProvider {

        EndpointConfigFactory endpointConfigFactory;

        @Inject
        protected EndpointConfigValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(EndpointConfig.class))
                return endpointConfigFactory == null ? (endpointConfigFactory = new EndpointConfigFactory()) : endpointConfigFactory;

            return null;
        }
    }

    static class AsyncRemoteEndpointValueFactoryProvider extends WebSocketValueFactoryProvider {

        AsyncRemoteEndpointFactory asyncRemoteEndpointFactory;

        @Inject
        protected AsyncRemoteEndpointValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(RemoteEndpoint.Async.class)
                    || type.equals(RemoteEndpoint.class))
                return asyncRemoteEndpointFactory == null ? (asyncRemoteEndpointFactory = new AsyncRemoteEndpointFactory()) : asyncRemoteEndpointFactory;

            return null;
        }
    }

    static class BasicRemoteEndpointValueFactoryProvider extends WebSocketValueFactoryProvider {

        BasicRemoteEndpointFactory basicRemoteEndpointFactory;

        @Inject
        protected BasicRemoteEndpointValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();
            if (type.equals(RemoteEndpoint.Basic.class))
                return basicRemoteEndpointFactory == null ? (basicRemoteEndpointFactory = new BasicRemoteEndpointFactory()) : basicRemoteEndpointFactory;

            return null;
        }
    }

    static class MessageStateValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        MessageStateFactory messageStatefactory;

        @Inject
        protected MessageStateValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, addDefaultSources());
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();
            if (type.equals(MessageState.class))
                return messageStatefactory;

            return null;
        }
    }

    static abstract class WebSocketValueFactoryProvider extends AbstractValueFactoryProvider {
        @Inject
        MessageState state;

        protected WebSocketValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator, Parameter.Source... sources) {
            super(mpep, locator, addDefaultSources(sources));
        }
    }

    static class PathParamValueFactoryProvider extends WebSocketValueFactoryProvider {

        @Inject
        protected PathParamValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.PATH);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();
            javax.ws.rs.PathParam pathParamRs = parameter.getAnnotation(javax.ws.rs.PathParam.class);

            if (pathParamRs != null) {
                if (!type.equals(String.class))
                    throw new IllegalArgumentException(PATH_PARAM_ERR_MSG);
                return new PathParamFactory(pathParamRs.value());
            } else {
                PathParam pathParam = parameter.getAnnotation(PathParam.class);
                if (pathParam != null) {
                    if (!type.equals(String.class))
                        throw new IllegalArgumentException(PATH_PARAM_ERR_MSG);
                    return new PathParamFactory(pathParam.value());
                }
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    static class QueryParamValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        protected QueryParamValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.QUERY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            QueryParam queryParam = parameter.getAnnotation(QueryParam.class);

            if (queryParam != null)
                if (type.equals(String.class)) {
                    return new QueryParamFactory(queryParam.value());
                } else if (type.isArray()) {
                    if (!type.getComponentType().equals(String.class)) {
                        throw new IllegalArgumentException(QUERY_PARAM_ERR_MSG);
                    }
                    return new QueryParamsFactory(queryParam.value());
                } else if (List.class.isAssignableFrom(type)) {
                    Class gType = null;
                    if (parameter.getType() instanceof ParameterizedType) {
                        Type[] types = ((ParameterizedType) parameter.getType()).getActualTypeArguments();
                        if (types.length == 1)
                            gType = (Class) types[0];
                    }
                    if (String.class.equals(gType)) {
                        return new QueryParamListFactory(queryParam.value(), null);
                    } else {
                        throw new IllegalArgumentException(QUERY_PARAM_ERR_MSG);
                    }
                } else {
                    throw new IllegalArgumentException(QUERY_PARAM_ERR_MSG);
                }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    static class QueryStringValueFactoryProvider extends WebSocketValueFactoryProvider {

        QueryStringFactory queryStringFactory;

        @Inject
        protected QueryStringValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            QueryString queryString = parameter.getAnnotation(QueryString.class);
            if (queryString != null) {
                if (!type.isAssignableFrom(String.class))
                    throw new IllegalArgumentException("@QueryString parameter class must be String.");
                return queryStringFactory == null ? (queryStringFactory = new QueryStringFactory()) : queryStringFactory;
            }
            return null;
        }
    }

    static class MessageValueFactoryProvider extends AbstractValueFactoryProvider {


        MessageFactory messageFactory;

        @Inject
        protected MessageValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator,
                                              MessageState state) {
            super(mpep, locator, addDefaultSources());
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
        @Inject
        MessageState messageState;

        @Override
        public void dispose(V instance) {
            // not use
        }
    }

    static class QueryParamsFactory extends AbstractValueFactory<String[]> {

        QueryParamListFactory factory;
        String key;

        QueryParamsFactory(String key) {
            this.key = key;
        }

        @Override
        public String[] provide() {
            if (factory == null) {
                factory = new QueryParamListFactory(key, messageState);
            }
            List<String> params = factory.provide();
            return params != null ? params.toArray(new String[params.size()]) : null;
        }
    }

    static class QueryParamListFactory implements Factory<List<String>> {

        String key;
        @Inject
        MessageState messageState;

        QueryParamListFactory(String key, MessageState messageState) {
            this.key = key;
            this.messageState = messageState;
        }

        @Override
        public List<String> provide() {
            return messageState.getSession().getRequestParameterMap().get(key);
        }

        @Override
        public void dispose(List<String> instance) {

        }
    }

    static class QueryParamFactory extends AbstractValueFactory<String> {

        QueryParamListFactory factory;
        String key;

        QueryParamFactory(String key) {
            this.key = key;
        }

        @Override
        public String provide() {

            if (factory == null) {
                factory = new QueryParamListFactory(key, messageState);
            }

            List<String> params = factory.provide();
            if (params != null && params.size() > 0) {
                return params.get(0);
            }
            return null;
        }
    }

    static class PathParamFactory extends AbstractValueFactory<String> {

        String key;

        PathParamFactory(String key) {
            this.key = key;
        }

        @Override
        public String provide() {
            return messageState.getSession().getPathParameters().get(key);
        }
    }

    static class QueryStringFactory extends AbstractValueFactory<String> {
        @Override
        public String provide() {
            return messageState.getSession().getQueryString();
        }
    }

    static class MessageFactory implements Factory<Object> {

        MessageState messageState;

        MessageFactory(MessageState state) {
            messageState = state;
        }

        @Override
        public Object provide() {
            return messageState.getMessage();
        }

        @Override
        public void dispose(Object instance) {

        }
    }

    static class PrincipalFactory extends AbstractValueFactory<Principal> {
        @Override
        public Principal provide() {
            return messageState.getSession().getUserPrincipal();
        }
    }

    static class BasicRemoteEndpointFactory extends AbstractValueFactory<RemoteEndpoint.Basic> {
        @Override
        public RemoteEndpoint.Basic provide() {
            return messageState.getSession().getBasicRemote();
        }
    }

    static class AsyncRemoteEndpointFactory extends AbstractValueFactory<RemoteEndpoint.Async> {
        @Override
        public RemoteEndpoint.Async provide() {
            return messageState.getSession().getAsyncRemote();
        }
    }

    static class EndpointConfigFactory extends AbstractValueFactory<EndpointConfig> {
        @Override
        public EndpointConfig provide() {
            return messageState.getEndpointConfig();
        }
    }

    static class SessionFactory extends AbstractValueFactory<Session> {
        @Override
        public Session provide() {
            return messageState.getSession();
        }
    }

    static class MessageEndFactory extends AbstractValueFactory<Object> {
        @Override
        public Object provide() {
            return messageState.getLast();
        }
    }

    static class MessageStateFactory implements Factory<MessageState> {

        MessageState messageState;

        MessageStateFactory(MessageState messageState) {
            this.messageState = messageState;
        }

        @Override
        public MessageState provide() {
            return messageState;
        }

        @Override
        public void dispose(MessageState instance) {
            // not use
        }
    }
}