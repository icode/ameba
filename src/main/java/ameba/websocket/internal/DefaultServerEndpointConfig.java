package ameba.websocket.internal;

import ameba.websocket.WebSocket;
import ameba.websocket.WebSocketEndpointProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;

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
 *
 */
public class DefaultServerEndpointConfig implements ServerEndpointConfig {

    private String path;
    private List<String> subprotocols;
    private List<Extension> extensions = Lists.newArrayList();
    private List<Class<? extends Encoder>> encoders;
    private List<Class<? extends Decoder>> decoders;
    private Map<String, Object> userProperties = Maps.newConcurrentMap();
    private ServerEndpointConfig.Configurator serverEndpointConfigurator;

    /**
     * <p>Constructor for DefaultServerEndpointConfig.</p>
     *
     * @param manager a manager
     * @param endpointClass  a {@link java.lang.Class} endpoint class.
     * @param webSocketConf  a {@link ameba.websocket.WebSocket} object.
     */
    public DefaultServerEndpointConfig(final InjectionManager manager,
                                       Class endpointClass,
                                       final WebSocket webSocketConf) {
        path = webSocketConf.path();
        subprotocols = Arrays.asList(webSocketConf.subprotocols());
        encoders = Lists.newArrayList(webSocketConf.encoders());
        decoders = Lists.newArrayList(webSocketConf.decoders());
        for (Class<? extends Extension> extensionClass : webSocketConf.extensions()) {
            extensions.add(Injections.getOrCreate(manager, extensionClass));
        }
        final WebSocketEndpointProvider provider = manager.getInstance(WebSocketEndpointProvider.class);

        final EndpointMeta endpointMeta = provider.parseMeta(endpointClass, webSocketConf);

        final ServerEndpointConfig.Configurator cfgr =
                Injections.getOrCreate(manager, webSocketConf.configurator());
        serverEndpointConfigurator = new ServerEndpointConfig.Configurator() {

            @Override
            public String getNegotiatedSubprotocol(List<String> supported, List<String> requested) {
                return cfgr.getNegotiatedSubprotocol(supported, requested);
            }

            @Override
            public List<Extension> getNegotiatedExtensions(List<Extension> installed, List<Extension> requested) {
                return cfgr.getNegotiatedExtensions(installed, requested);
            }

            @Override
            public boolean checkOrigin(String originHeaderValue) {
                return cfgr.checkOrigin(originHeaderValue);
            }

            @Override
            public void modifyHandshake(ServerEndpointConfig sec,
                                        HandshakeRequest request, HandshakeResponse response) {
                cfgr.modifyHandshake(sec, request, response);
            }

            @Override
            public <T> T getEndpointInstance(Class<T> eClass) throws InstantiationException {
                if (EndpointDelegate.class.equals(eClass)) {
                    return eClass.cast(new EndpointDelegate(endpointMeta));
                }
                return cfgr.getEndpointInstance(eClass);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getEndpointClass() {
        return EndpointDelegate.class;
    }

    /** {@inheritDoc} */
    @Override
    public String getPath() {
        return path;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getSubprotocols() {
        return subprotocols;
    }

    /** {@inheritDoc} */
    @Override
    public List<Extension> getExtensions() {
        return extensions;
    }

    /** {@inheritDoc} */
    @Override
    public ServerEndpointConfig.Configurator getConfigurator() {
        return serverEndpointConfigurator;
    }

    /** {@inheritDoc} */
    @Override
    public List<Class<? extends Encoder>> getEncoders() {
        return encoders;
    }

    /** {@inheritDoc} */
    @Override
    public List<Class<? extends Decoder>> getDecoders() {
        return decoders;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }
}
