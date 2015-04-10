package ameba.websocket.internal;

import org.glassfish.hk2.api.*;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.internal.inject.JerseyClassAnalyzer;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.websocket.CloseReason;
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
 * 参数注入实现
 *
 * @author icode
 */
public class ParameterInjectionBinder extends AbstractBinder {

    public static final String CLASS_ANALYZER_NAME = "AmebaWebSocketClassAnalyzer";
    private static final String PATH_PARAM_ERR_MSG = "@PathParam parameter class must be String.";
    private static final String QUERY_PARAM_ERR_MSG = "@QueryParam parameter class must be String, String[] or List<String>";

    @Override
    protected void configure() {

        bindFactory(PrincipalFactory.class).to(Principal.class).in(Singleton.class);
        bindFactory(SessionFactory.class).to(Session.class).in(Singleton.class);
        bindFactory(EndpointConfigFactory.class).to(EndpointConfig.class).in(Singleton.class);
        bindFactory(AsyncRemoteEndpointFactory.class).to(RemoteEndpoint.Async.class).to(RemoteEndpoint.class).in(Singleton.class);
        bindFactory(BasicRemoteEndpointFactory.class).to(RemoteEndpoint.Basic.class).in(Singleton.class);
        bindFactory(CloseReasonFactory.class).to(CloseReason.class).in(Singleton.class);

        bind(MessageStateValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
        bind(ErrorValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
        bind(BasicRemoteEndpointValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
        bind(AsyncRemoteEndpointValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
        bind(EndpointConfigValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
        bind(SessionValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
        bind(PrincipalValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
        bind(PathParamValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
        bind(QueryParamValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
        bind(QueryStringValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
        bind(CloseReasonValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);

        bind(MessageEndValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
        bind(MessageValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);

        bind(PathParamValueFactoryProvider.InjectResolver.class).to(new TypeLiteral<InjectionResolver<javax.ws.rs.PathParam>>() {
        }).in(Singleton.class);
        bind(QueryParamValueFactoryProvider.InjectResolver.class).to(new TypeLiteral<InjectionResolver<QueryParam>>() {
        }).in(Singleton.class);
        bind(QueryStringValueFactoryProvider.InjectResolver.class).to(new TypeLiteral<InjectionResolver<QueryString>>() {
        }).in(Singleton.class);

        bind(JerseyClassAnalyzer.class)
                .analyzeWith(JerseyClassAnalyzer.NAME)
                .named(CLASS_ANALYZER_NAME)
                .to(ClassAnalyzer.class)
                .in(Singleton.class);
    }

    private static class MessageEndValueFactoryProvider extends WebSocketValueFactoryProvider {

        @Inject
        protected MessageEndValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(Boolean.class) || type.equals(boolean.class))
                return new MessageEndFactory();

            return null;
        }
    }

    private static class PrincipalValueFactoryProvider extends WebSocketValueFactoryProvider {

        @Inject
        protected PrincipalValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(Principal.class))
                return getLocator().createAndInitialize(PrincipalFactory.class);

            return null;
        }
    }

    private static class SessionValueFactoryProvider extends WebSocketValueFactoryProvider {

        @Inject
        protected SessionValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(Session.class))
                return getLocator().createAndInitialize(SessionFactory.class);

            return null;
        }
    }

    private static class EndpointConfigValueFactoryProvider extends WebSocketValueFactoryProvider {

        @Inject
        protected EndpointConfigValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(EndpointConfig.class))
                return getLocator().createAndInitialize(EndpointConfigFactory.class);

            return null;
        }
    }

    private static class AsyncRemoteEndpointValueFactoryProvider extends WebSocketValueFactoryProvider {

        @Inject
        protected AsyncRemoteEndpointValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();

            if (type.equals(RemoteEndpoint.Async.class)
                    || type.equals(RemoteEndpoint.class))
                return getLocator().createAndInitialize(AsyncRemoteEndpointFactory.class);

            return null;
        }
    }

    private static class BasicRemoteEndpointValueFactoryProvider extends WebSocketValueFactoryProvider {

        @Inject
        protected BasicRemoteEndpointValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();
            if (type.equals(RemoteEndpoint.Basic.class))
                return getLocator().createAndInitialize(BasicRemoteEndpointFactory.class);

            return null;
        }
    }

    private static class MessageStateValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        Provider<MessageState> messageState;

        @Inject
        protected MessageStateValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();
            if (type.equals(MessageState.class))
                return new AbstractValueFactory<MessageState>() {
                    @Override
                    public MessageState provide() {
                        return messageState.get();
                    }
                };

            return null;
        }

    }

    static class CloseReasonValueFactoryProvider extends WebSocketValueFactoryProvider {

        @Inject
        protected CloseReasonValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();
            if (type.equals(CloseReason.class))
                return getLocator().createAndInitialize(CloseReasonFactory.class);

            return null;
        }
    }

    static class ErrorValueFactoryProvider extends AbstractValueFactoryProvider {

        @Inject
        protected ErrorValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Factory<?> createValueFactory(Parameter parameter) {
            Class type = parameter.getRawType();
            if (Throwable.class.isAssignableFrom(type))
                return getLocator().createAndInitialize(ErrorFactory.class);

            return null;
        }

    }

    private static abstract class WebSocketValueFactoryProvider extends AbstractValueFactoryProvider {
        @Inject
        MessageState state;

        protected WebSocketValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator, Parameter.Source... sources) {
            super(mpep, locator, sources);
        }
    }

    private static class PathParamValueFactoryProvider extends WebSocketValueFactoryProvider {

        @Inject
        protected PathParamValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.PATH, Parameter.Source.UNKNOWN);
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

        static class InjectResolver extends ParamInjectionResolver<javax.ws.rs.PathParam> {
            public InjectResolver() {
                super(PathParamValueFactoryProvider.class);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static class QueryParamValueFactoryProvider extends AbstractValueFactoryProvider {

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
                        return new QueryParamListFactory(queryParam.value(),
                                getLocator().<Ref<MessageState>>getService(EndpointDelegate.MESSAGE_STATE_TYPE).get());
                    } else {
                        throw new IllegalArgumentException(QUERY_PARAM_ERR_MSG);
                    }
                } else {
                    throw new IllegalArgumentException(QUERY_PARAM_ERR_MSG);
                }
            return null;
        }

        static class InjectResolver extends ParamInjectionResolver<QueryParam> {
            public InjectResolver() {
                super(QueryParamValueFactoryProvider.class);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static class QueryStringValueFactoryProvider extends AbstractValueFactoryProvider {

        QueryStringFactory queryStringFactory;

        @Inject
        protected QueryStringValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.UNKNOWN);
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

        @Singleton
        static class InjectResolver extends ParamInjectionResolver<QueryString> {
            public InjectResolver() {
                super(QueryStringValueFactoryProvider.class);
            }
        }
    }

    private static class MessageValueFactoryProvider extends AbstractValueFactoryProvider {

        MessageFactory messageFactory;

        @Inject
        protected MessageValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
            super(mpep, locator, Parameter.Source.ENTITY);
            messageFactory = locator.createAndInitialize(MessageFactory.class);
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
        Provider<MessageState> messageState;

        @Override
        public void dispose(V instance) {
            // not use
        }
    }

    private static class QueryParamsFactory extends AbstractValueFactory<String[]> {

        QueryParamListFactory factory;
        String key;

        QueryParamsFactory(String key) {
            this.key = key;
        }

        @Override
        public String[] provide() {
            if (factory == null) {
                factory = new QueryParamListFactory(key, messageState.get());
            }
            List<String> params = factory.provide();
            return params != null ? params.toArray(new String[params.size()]) : null;
        }
    }

    private static class QueryParamListFactory implements Factory<List<String>> {

        String key;
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

    private static class QueryParamFactory extends AbstractValueFactory<String> {

        QueryParamListFactory factory;
        String key;

        QueryParamFactory(String key) {
            this.key = key;
        }

        @Override
        public String provide() {

            if (factory == null) {
                factory = new QueryParamListFactory(key, messageState.get());
            }

            List<String> params = factory.provide();
            if (params != null && params.size() > 0) {
                return params.get(0);
            }
            return null;
        }
    }

    private static class PathParamFactory extends AbstractValueFactory<String> {

        String key;

        PathParamFactory(String key) {
            this.key = key;
        }

        @Override
        public String provide() {
            return messageState.get().getSession().getPathParameters().get(key);
        }
    }

    private static class QueryStringFactory extends AbstractValueFactory<String> {
        @Override
        public String provide() {
            return messageState.get().getSession().getQueryString();
        }
    }

    static class MessageFactory extends AbstractValueFactory<Object> {
        @Override
        public Object provide() {
            return messageState.get().getMessage();
        }
    }

    private static class PrincipalFactory extends AbstractValueFactory<Principal> {
        @Override
        public Principal provide() {
            return messageState.get().getSession().getUserPrincipal();
        }
    }

    private static class BasicRemoteEndpointFactory extends AbstractValueFactory<RemoteEndpoint.Basic> {
        @Override
        public RemoteEndpoint.Basic provide() {
            return messageState.get().getSession().getBasicRemote();
        }
    }

    private static class AsyncRemoteEndpointFactory extends AbstractValueFactory<RemoteEndpoint.Async> {
        @Override
        public RemoteEndpoint.Async provide() {
            return messageState.get().getSession().getAsyncRemote();
        }
    }

    private static class EndpointConfigFactory extends AbstractValueFactory<EndpointConfig> {
        @Override
        public EndpointConfig provide() {
            return messageState.get().getEndpointConfig();
        }
    }

    private static class SessionFactory extends AbstractValueFactory<Session> {
        @Override
        public Session provide() {
            return messageState.get().getSession();
        }
    }

    static class MessageEndFactory extends AbstractValueFactory<Boolean> {
        @Override
        public Boolean provide() {
            return messageState.get().getLast();
        }
    }

    private static class ErrorFactory extends AbstractValueFactory<Throwable> {
        @Override
        public Throwable provide() {
            return messageState.get().getThrowable();
        }
    }

    private static class CloseReasonFactory extends AbstractValueFactory<CloseReason> {
        @Override
        public CloseReason provide() {
            return messageState.get().getCloseReason();
        }
    }
}