package ameba.websocket;

import org.jvnet.hk2.annotations.Contract;

/**
 * @author icode
 */
@Contract
public interface WebSocketEndpointProvider {
    EndpointMeta parseEndpointMeta(Class endpointClass, WebSocket webSocketConf);
}