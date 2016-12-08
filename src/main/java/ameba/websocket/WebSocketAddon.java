package ameba.websocket;

import ameba.core.Addon;
import ameba.core.Application;
import ameba.i18n.Messages;
import ameba.scanner.ClassFoundEvent;
import ameba.websocket.internal.DefaultServerEndpointConfig;
import ameba.websocket.internal.WebSocketBinder;
import com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Sets;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.ScopedBindingBuilder;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.jvnet.hk2.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;


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
        private ServiceLocator serviceLocator;
        @Inject
        private ServerContainer serverContainer;

        public static Set<Class<?>> getProviderContracts(final Class<?> clazz) {
            final Set<Class<?>> contracts = Sets.newIdentityHashSet();
            computeProviderContracts(clazz, contracts);
            return contracts;
        }

        private static void computeProviderContracts(final Class<?> clazz, final Set<Class<?>> contracts) {
            for (final Class<?> contract : getImplementedContracts(clazz)) {
                if (isSupportedContract(contract)) {
                    contracts.add(contract);
                }
                computeProviderContracts(contract, contracts);
            }
        }

        private static Iterable<Class<?>> getImplementedContracts(final Class<?> clazz) {
            final Collection<Class<?>> list = new LinkedList<>();

            Collections.addAll(list, clazz.getInterfaces());

            final Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                list.add(superclass);
            }

            return list;
        }

        public static boolean isSupportedContract(final Class<?> type) {
            return type.isAnnotationPresent(Contract.class)
                    || type.isAnnotationPresent(org.glassfish.jersey.spi.Contract.class);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public boolean configure(FeatureContext context) {
            context.register(new WebSocketBinder());
            final DynamicConfiguration dc = Injections.getConfiguration(serviceLocator);

            if (serverContainer == null) {
                logger.warn(Messages.get("web.socket.server.unsupported"));
            }

            for (Class endpointClass : endpointClasses) {
                WebSocket webSocket = getAnnotation(WebSocket.class, endpointClass);
                if (webSocket == null) continue;
                Class<? extends Annotation> scope = getScope(endpointClass);
                final ScopedBindingBuilder<?> bindingBuilder = Injections.newBinder(endpointClass)
                        .to(endpointClass).in(scope);
                getProviderContracts(endpointClass).forEach((Consumer<Class>) bindingBuilder::to);
                Injections.addBinding(bindingBuilder, dc);
                DefaultServerEndpointConfig endpointConfig = new DefaultServerEndpointConfig(
                        serviceLocator,
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
            dc.commit();

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
