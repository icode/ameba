package ameba.websocket.internal;

import ameba.websocket.WebSocket;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.model.ResourceMethod;

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
 * @author icode
 */
public class DefaultServerEndpointConfig implements ServerEndpointConfig {

    private String path;
    private Class<?> endpointClass;
    private List<String> subprotocols;
    private List<Extension> extensions = Lists.newArrayList();
    private List<Class<? extends Encoder>> encoders;
    private List<Class<? extends Decoder>> decoders;
    private Map<String, Object> userProperties = Maps.newHashMap();
    private ServerEndpointConfig.Configurator serverEndpointConfigurator;

    public DefaultServerEndpointConfig(final ServiceLocator serviceLocator,
                                       final ResourceMethod resourceMethod,
                                       Class<?> endpointClass, String path,
                                       final WebSocket webSocketConf) {
        this.path = path;
        this.endpointClass = endpointClass;
        this.subprotocols = Arrays.asList(webSocketConf.subprotocols());
        encoders = Lists.newArrayList(webSocketConf.encoders());
        decoders = Lists.newArrayList(webSocketConf.decoders());
        for (Class<? extends Extension> extensionClass : webSocketConf.extensions()) {
            extensions.add(serviceLocator.createAndInitialize(extensionClass));
        }
        serverEndpointConfigurator = new Configurator() {
            Configurator configurator = serviceLocator.createAndInitialize(webSocketConf.configurator());

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
                T endpoint = configurator.getEndpointInstance(endpointClass);
                if (endpoint instanceof EndpointDelegate) {
                    ((EndpointDelegate) endpoint).setResourceMethod(resourceMethod);
                }
                return endpoint;
            }
        };
    }

    public DefaultServerEndpointConfig(ServiceLocator serviceLocator, Class<?> endpointClass, String path, WebSocket webSocketConf) {
        this(serviceLocator, null, endpointClass, path, webSocketConf);
    }

    public DefaultServerEndpointConfig(ServiceLocator serviceLocator, ResourceMethod resourceMethod, String path, WebSocket webSocketConf) {
        this(serviceLocator, resourceMethod, EndpointDelegate.class, path, webSocketConf);
    }


    @Override
    public Class<?> getEndpointClass() {
        return endpointClass;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public List<String> getSubprotocols() {
        return subprotocols;
    }

    @Override
    public List<Extension> getExtensions() {
        return extensions;
    }

    @Override
    public Configurator getConfigurator() {
        return serverEndpointConfigurator;
    }

    @Override
    public List<Class<? extends Encoder>> getEncoders() {
        return encoders;
    }

    @Override
    public List<Class<? extends Decoder>> getDecoders() {
        return decoders;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }
}
