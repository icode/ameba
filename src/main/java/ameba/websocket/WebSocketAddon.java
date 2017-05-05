package ameba.websocket;

import ameba.core.Addon;
import ameba.core.Application;
import ameba.core.ServiceLocators;
import ameba.i18n.Messages;
import ameba.scanner.ClassFoundEvent;
import ameba.websocket.internal.DefaultServerEndpointConfig;
import ameba.websocket.internal.WebSocketBinder;
import com.google.common.collect.Lists;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.lang.annotation.Annotation;
import java.util.List;


/**
 * WebSocket add on
 *
 * @author icode
 */
public class WebSocketAddon extends Addon {
    /**
     * Constant <code>WEB_SOCKET_ENABLED_CONF="websocket.enabled"</code>
     */
    public static final String WEB_SOCKET_ENABLED_CONF = "websocket.enabled";
    private static final Logger logger = LoggerFactory.getLogger(WebSocketAddon.class);
    private static final List<Class> endpointClasses = Lists.newArrayList();
    private static Boolean enabled = null;

    /**
     * <p>isEnabled.</p>
     *
     * @return a boolean.
     */
    public static Boolean isEnabled() {
        return enabled;
    }

    /**
     * <p>_getAnnotation.</p>
     *
     * @param annotationClass a {@link java.lang.Class} object.
     * @param endpointClass   a {@link java.lang.Class} object.
     * @param <A>             a A object.
     * @return a A object.
     */
    protected static <A> A _getAnnotation(Class<A> annotationClass, Class endpointClass) {
        return annotationClass.cast(endpointClass.getAnnotation(annotationClass));
    }

    /**
     * <p>getAnnotation.</p>
     *
     * @param annotationClass a {@link java.lang.Class} object.
     * @param endpointClass   a {@link java.lang.Class} object.
     * @param <A>             a A object.
     * @return a A object.
     */
    protected static <A> A getAnnotation(Class<A> annotationClass, Class endpointClass) {
        if (endpointClass == Object.class || endpointClass == null) return null;
        A annotation = _getAnnotation(annotationClass, endpointClass);
        if (annotation == null) {
            Class sCls = endpointClass.getSuperclass();
            if (sCls != null) {
                annotation = _getAnnotation(annotationClass, sCls);
            }

            if (annotation == null) {
                Class[] inces = endpointClass.getInterfaces();
                for (Class infc : inces) {
                    annotation = _getAnnotation(annotationClass, infc);
                    if (annotation != null) {
                        return annotation;
                    }
                }
                annotation = getAnnotation(annotationClass, sCls);
                if (annotation == null) {
                    for (Class infc : inces) {
                        annotation = getAnnotation(annotationClass, infc);
                        if (annotation != null) {
                            return annotation;
                        }
                    }
                }
            }
        }
        return annotation;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEnabled(Application application) {
        if (enabled == null) {
            enabled = !"false".equals(application.getSrcProperties().get(WEB_SOCKET_ENABLED_CONF));

            if (!enabled) {
                logger.info(Messages.get("web.socket.info.disabled"));
            }
        }

        return enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setup(final Application application) {
        endpointClasses.clear();
        subscribeSystemEvent(ClassFoundEvent.class, event -> event.accept(info -> {
            if (info.accpet(ctClass -> ctClass.hasAnnotation(WebSocket.class))) {
                endpointClasses.add(info.toClass());
                return true;
            }
            return false;
        }));

        application.register(WebSocketFeature.class);
    }

    private static class WebSocketFeature implements Feature {
        @Inject
        private InjectionManager injectionManager;
        @Inject
        private ServerContainer serverContainer;

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public boolean configure(FeatureContext context) {
            context.register(new WebSocketBinder());
            if (serverContainer == null) {
                logger.warn(Messages.get("web.socket.server.unsupported"));
            }

            for (Class endpointClass : endpointClasses) {
                WebSocket webSocket = getAnnotation(WebSocket.class, endpointClass);
                if (webSocket == null) continue;
                Class<? extends Annotation> scope = getScope(endpointClass);
                final Binding binding = Bindings.service(endpointClass).to(endpointClass).in(scope);
                ServiceLocators.getProviderContracts(endpointClass).forEach(binding::to);
                injectionManager.register(binding);
                DefaultServerEndpointConfig endpointConfig = new DefaultServerEndpointConfig(
                        injectionManager,
                        endpointClass,
                        webSocket
                );
                if (serverContainer != null) {
                    try {
                        serverContainer.addEndpoint(endpointConfig);
                    } catch (DeploymentException e) {
                        throw new WebSocketException(e);
                    }
                }
                if (webSocket.withSockJS()) {
                    // create resource use modelProcessor
                }
            }

            return true;
        }

        private Class<? extends Annotation> getScope(final Class<?> clazz) {
            Class<? extends Annotation> hk2Scope = RequestScoped.class;
            if (clazz.isAnnotationPresent(Singleton.class)) {
                hk2Scope = Singleton.class;
            } else if (clazz.isAnnotationPresent(PerLookup.class)) {
                hk2Scope = PerLookup.class;
            }
            return hk2Scope;
        }
    }
}
