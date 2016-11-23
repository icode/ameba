package ameba.websocket;

import ameba.websocket.internal.EndpointMeta;
import org.jvnet.hk2.annotations.Contract;

/**
 * <p>WebSocketEndpointProvider interface.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
@Contract
public interface WebSocketEndpointProvider {
    /**
     * <p>parseMeta.</p>
     *
     * @param endpointClass a {@link java.lang.Class} object.
     * @param webSocketConf a {@link ameba.websocket.WebSocket} object.
     * @return a {@link ameba.websocket.internal.EndpointMeta} object.
     */
    EndpointMeta parseMeta(Class endpointClass, WebSocket webSocketConf);
}
