package ameba.websocket.internal;

import ameba.websocket.WebSocket;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.glassfish.hk2.api.ServiceLocator;

import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>DefaultServerEndpointConfig class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public class DefaultServerEndpointConfig implements ServerEndpointConfig {

    private String path;
    private Class<?> endpointClass;
    private List<String> subprotocols;
    private List<Extension> extensions = Lists.newArrayList();
    private List<Class<? extends Encoder>> encoders;
    private List<Class<? extends Decoder>> decoders;
    private Map<String, Object> userProperties = Maps.newConcurrentMap();
    private ServerEndpointConfig.Configurator serverEndpointConfigurator;

    /**
     * <p>Constructor for DefaultServerEndpointConfig.</p>
     *
     * @param serviceLocator a {@link org.glassfish.hk2.api.ServiceLocator} object.
     * @param endpointClass  a {@link java.lang.Class} object.
     * @param webSocketConf  a {@link ameba.websocket.WebSocket} object.
     */
    public DefaultServerEndpointConfig(final ServiceLocator serviceLocator,
                                       Class<?> endpointClass,
                                       final WebSocket webSocketConf) {
//        this.path = webSocketConf.path();
        this.endpointClass = endpointClass;
        this.subprotocols = Arrays.asList(webSocketConf.subprotocols());
        encoders = Lists.newArrayList(webSocketConf.encoders());
        decoders = Lists.newArrayList(webSocketConf.decoders());
        for (Class<? extends Extension> extensionClass : webSocketConf.extensions()) {
            extensions.add(serviceLocator.createAndInitialize(extensionClass));
        }
        ServerEndpointConfig.Configurator configurator = serviceLocator.createAndInitialize(webSocketConf.configurator());
        serverEndpointConfigurator = new Configurator(configurator, endpointClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getEndpointClass() {
        return endpointClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSubprotocols() {
        return subprotocols;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Extension> getExtensions() {
        return extensions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServerEndpointConfig.Configurator getConfigurator() {
        return serverEndpointConfigurator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Class<? extends Encoder>> getEncoders() {
        return encoders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Class<? extends Decoder>> getDecoders() {
        return decoders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    private class Configurator extends ServerEndpointConfig.Configurator {
        private ServerEndpointConfig.Configurator configurator;
        private Class<?> endpointClass;

        public Configurator(ServerEndpointConfig.Configurator configurator, Class<?> endpointClass) {
            this.configurator = configurator;
            this.endpointClass = endpointClass;
        }

        @Override
        public String getNegotiatedSubprotocol(List<String> supported, List<String> requested) {
            return configurator.getNegotiatedSubprotocol(supported, requested);
        }

        @Override
        public List<Extension> getNegotiatedExtensions(List<Extension> installed, List<Extension> requested) {
            return configurator.getNegotiatedExtensions(installed, requested);
        }

        @Override
        public boolean checkOrigin(String originHeaderValue) {
            return configurator.checkOrigin(originHeaderValue);
        }

        @Override
        public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            configurator.modifyHandshake(sec, request, response);
        }

        @Override
        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
            return configurator.getEndpointInstance(endpointClass);
        }
    }
}
