package ameba.websocket.internal;

import ameba.websocket.WebSocket;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.glassfish.hk2.api.ServiceLocator;

import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
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

    public DefaultServerEndpointConfig(ServiceLocator serviceLocator, Class<?> endpointClass, String path, WebSocket webSocketConf) {
        this.path = path;
        this.endpointClass = endpointClass;
        this.subprotocols = Arrays.asList(webSocketConf.subprotocols());
        encoders = Arrays.asList(webSocketConf.encoders());
        decoders = Arrays.asList(webSocketConf.decoders());
        for (Class<? extends Extension> extensionClass : webSocketConf.extensions()){
            extensions.add(serviceLocator.createAndInitialize(extensionClass));
        }
        serverEndpointConfigurator = serviceLocator.createAndInitialize(webSocketConf.configurator());
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
