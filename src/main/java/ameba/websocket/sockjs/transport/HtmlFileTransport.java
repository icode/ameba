package ameba.websocket.sockjs.transport;

import org.glassfish.jersey.server.ChunkedOutput;

import javax.ws.rs.container.ContainerRequestContext;

/**
 * <p>HtmlFileTransport class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class HtmlFileTransport implements Transport {

    /**
     * {@inheritDoc}
     */
    @Override
    public ChunkedOutput apply(ContainerRequestContext containerRequestContext) {
        return null;
    }
}
