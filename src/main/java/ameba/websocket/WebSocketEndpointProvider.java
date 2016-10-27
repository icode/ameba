package ameba.websocket;

import ameba.websocket.internal.EndpointMeta;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author icode
 */
@Contract
public interface WebSocketEndpointProvider {
    EndpointMeta parseMeta(Class endpointClass, WebSocket webSocketConf);
}