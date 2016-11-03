package ameba.websocket.sockjs.transport;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ChunkedOutput;

import javax.ws.rs.container.ContainerRequestContext;

/**
 * @author icode
 */
public interface Transport extends Inflector<ContainerRequestContext, ChunkedOutput> {

}