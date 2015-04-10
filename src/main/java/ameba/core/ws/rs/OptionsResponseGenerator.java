package ameba.core.ws.rs;

import org.glassfish.jersey.server.ExtendedUriInfo;
import org.jvnet.hk2.annotations.Contract;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

/**
 * @author icode
 */
@Contract
public interface OptionsResponseGenerator {
    Response generate(Set<String> allowedMethods,
                      MediaType mediaType,
                      ExtendedUriInfo extendedUriInfo,
                      ContainerRequestContext containerRequestContext,
                      Response response);
}