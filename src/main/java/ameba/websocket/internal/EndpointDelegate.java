package ameba.websocket.internal;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

/**
 * <p>EndpointDelegate class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class EndpointDelegate extends Endpoint {
    private EndpointMeta meta;

    /**
     * <p>Constructor for EndpointDelegate.</p>
     *
     * @param meta a {@link ameba.websocket.internal.EndpointMeta} object.
     */
    public EndpointDelegate(EndpointMeta meta) {
        this.meta = meta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        meta.onOpen(session, config);
    }

    /** {@inheritDoc} */
    @Override
    public void onClose(Session session, CloseReason closeReason) {
        meta.onClose(session, closeReason);
    }

    /** {@inheritDoc} */
    @Override
    public void onError(Session session, Throwable thr) {
        meta.onError(session, thr);
    }
}
