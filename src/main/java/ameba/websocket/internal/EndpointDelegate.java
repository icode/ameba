package ameba.websocket.internal;

import ameba.websocket.EndpointMeta;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

/**
 * @author icode
 */
public class EndpointDelegate extends Endpoint {
    private EndpointMeta meta;

    public EndpointDelegate(EndpointMeta meta) {
        this.meta = meta;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        meta.onOpen(session, config);
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        meta.onClose(session, closeReason);
    }

    @Override
    public void onError(Session session, Throwable thr) {
        meta.onError(session, thr);
    }
}