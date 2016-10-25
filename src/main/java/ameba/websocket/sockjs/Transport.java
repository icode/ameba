package ameba.websocket.sockjs;

import org.glassfish.jersey.process.Inflector;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

/**
 * @author icode
 */
public interface Transport extends Inflector<ContainerRequestContext, Response> {

}