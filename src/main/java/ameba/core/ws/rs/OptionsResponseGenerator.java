package ameba.core.ws.rs;

import org.glassfish.jersey.server.ExtendedUriInfo;
import org.jvnet.hk2.annotations.Contract;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

/**
 * <p>OptionsResponseGenerator interface.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@Contract
public interface OptionsResponseGenerator {
    /**
     * <p>generate.</p>
     *
     * @param allowedMethods          a {@link java.util.Set} object.
     * @param mediaType               a {@link javax.ws.rs.core.MediaType} object.
     * @param extendedUriInfo         a {@link org.glassfish.jersey.server.ExtendedUriInfo} object.
     * @param containerRequestContext a {@link javax.ws.rs.container.ContainerRequestContext} object.
     * @param response                a {@link javax.ws.rs.core.Response} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    Response generate(Set<String> allowedMethods,
                      MediaType mediaType,
                      ExtendedUriInfo extendedUriInfo,
                      ContainerRequestContext containerRequestContext,
                      Response response);
}
