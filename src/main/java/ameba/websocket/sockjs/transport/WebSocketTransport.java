package ameba.websocket.sockjs.transport;

import org.glassfish.jersey.server.ChunkedOutput;

import javax.ws.rs.container.ContainerRequestContext;

/**
 * <p>WebSocketTransport class.</p>
 *
 * @author icode
 */
public class WebSocketTransport implements Transport {
    /**
     * {@inheritDoc}
     */
    @Override
    public ChunkedOutput apply(ContainerRequestContext containerRequestContext) {
        return null;
    }
}
