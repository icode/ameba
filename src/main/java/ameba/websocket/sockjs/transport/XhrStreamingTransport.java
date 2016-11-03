package ameba.websocket.sockjs.transport;

import org.glassfish.jersey.server.ChunkedOutput;

import javax.ws.rs.container.ContainerRequestContext;

/**
 * @author icode
 */
public class XhrStreamingTransport implements Transport {
    @Override
    public ChunkedOutput apply(ContainerRequestContext containerRequestContext) {
        return null;
    }
}
