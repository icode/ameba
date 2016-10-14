package ameba.websocket;

import ameba.core.Addon;
import ameba.core.Application;
import ameba.event.Listener;
import ameba.i18n.Messages;
import ameba.scanner.Acceptable;
import ameba.scanner.ClassFoundEvent;
import ameba.scanner.ClassInfo;
import ameba.websocket.internal.DefaultServerEndpointConfig;
import ameba.websocket.internal.WebSocketBinder;
import com.google.common.collect.Lists;
import javassist.CtClass;
import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.List;


/**
 * WebSocket add on
 *
 * @author icode
 */
public class WebSocketAddon extends Addon {
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

    protected static <A> A _getAnnotation(Class<A> annotationClass, Class endpointClass) {
        return annotationClass.cast(endpointClass.getAnnotation(annotationClass));
    }

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

    public boolean isEnabled(Application application) {
        if (enabled == null) {
            enabled = !"false".equals(application.getSrcProperties().get(WEB_SOCKET_ENABLED_CONF));

            if (!enabled) {
                logger.debug(Messages.get("info.websocket.disabled"));
            }
        }

        return enabled;
    }

    @Override
    public void setup(final Application application) {
        endpointClasses.clear();
        subscribeSystemEvent(ClassFoundEvent.class, new Listener<ClassFoundEvent>() {
            @Override
            public void onReceive(ClassFoundEvent event) {
                event.accept(new Acceptable<ClassInfo>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public boolean accept(final ClassInfo info) {
                        if (info.accpet(new Acceptable<CtClass>() {
                            @Override
                            public boolean accept(CtClass ctClass) {
                                return ctClass.hasAnnotation(WebSocket.class);
                            }
                        })) {
                            endpointClasses.add(info.toClass());
                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        application.register(WebSocketFeature.class);
    }

    private static class WebSocketFeature implements Feature {

        @Inject
        private ServiceLocator serviceLocator;
        @Inject
        private ServerContainer serverContainer;

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean configure(FeatureContext context) {
            context.register(new WebSocketBinder());

            for (Class endpointClass : endpointClasses) {
                WebSocket webSocket = getAnnotation(WebSocket.class, endpointClass);
                if (webSocket == null) continue;
                try {
                    serverContainer.addEndpoint(
                            new DefaultServerEndpointConfig(
                                    serviceLocator,
                                    endpointClass,
                                    webSocket
                            )
                    );
                } catch (DeploymentException e) {
                    throw new WebSocketException(e);
                }
                if (webSocket.withSockJS()) {
                    // create resource use modelProcessor
                }
            }

            return true;
        }
    }
}
