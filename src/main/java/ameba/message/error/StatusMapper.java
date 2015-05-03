package ameba.message.error;

import org.glassfish.jersey.server.ContainerResponse;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * @author icode
 */
@Singleton
@Priority(Integer.MAX_VALUE)
public class StatusMapper implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        if (!responseContext.hasEntity()
                && !((ContainerResponse) responseContext).isMappedFromException()
                && responseContext.getStatus() >= 400
                && responseContext.getStatus() < 600) {
            throw new WebApplicationException(responseContext.getStatus());
        }
    }
}
