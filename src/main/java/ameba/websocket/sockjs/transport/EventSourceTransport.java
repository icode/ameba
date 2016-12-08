package ameba.websocket.sockjs.transport;

import org.glassfish.jersey.server.ChunkedOutput;

import javax.ws.rs.container.ContainerRequestContext;

/**
 * <p>EventSourceTransport class.</p>
 *
 * @author icode
 *
 */
public class EventSourceTransport implements Transport {
    /**
     * {@inheritDoc}
     */
    @Override
    public ChunkedOutput apply(ContainerRequestContext containerRequestContext) {
        return null;
    }
}
