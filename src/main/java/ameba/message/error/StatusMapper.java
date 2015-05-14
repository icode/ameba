package ameba.message.error;

import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.server.ContainerResponse;

import javax.annotation.Priority;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

/**
 * @author icode
 */
@Singleton
@Priority(Integer.MAX_VALUE)
public class StatusMapper implements ContainerResponseFilter {

    @Context
    private Provider<MessageBodyWorkers> workersProvider;

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        if (!responseContext.hasEntity()
                && !((ContainerResponse) responseContext).isMappedFromException()
                && responseContext.getStatus() >= 400
                && responseContext.getStatus() < 600) {
            throw new WebApplicationException(responseContext.getStatus());
        }

        if (responseContext.getStatus() == 406
                || responseContext.getStatus() == 415) {
            List<MediaType> types = workersProvider.get().getMessageBodyWriterMediaTypesByType(Object.class);
            MediaType mediaType;
            if (types.size() > 0) {
                mediaType = types.get(0);
            } else {
                mediaType = MediaType.WILDCARD_TYPE;
            }
            ((ContainerResponse) responseContext).setMediaType(mediaType);
        }
    }
}
