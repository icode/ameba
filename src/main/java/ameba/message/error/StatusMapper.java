package ameba.message.error;

import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.server.ContainerResponse;

import javax.annotation.Priority;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
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
@Priority(Priorities.HEADER_DECORATOR - 1)
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

        List<MediaType> types = null;
        // 找不到 writer/reader, 提供系统有的 writer/reader 输出错误
        if (responseContext.getStatus() == 406) {
            types = workersProvider.get()
                    .getMessageBodyWriterMediaTypes(
                            responseContext.getEntityClass(),
                            responseContext.getEntityType(),
                            responseContext.getEntityAnnotations());
        } else if (responseContext.getStatus() == 415) {
            types = workersProvider.get()
                    .getMessageBodyReaderMediaTypes(
                            responseContext.getEntityClass(),
                            responseContext.getEntityType(),
                            responseContext.getEntityAnnotations());
        }
        if (types != null) {
            ((ContainerResponse) responseContext).setMediaType(parseMediaType(types));
        }
    }

    private MediaType parseMediaType(List<MediaType> types) {
        if (types != null && types.size() > 0) {
            return types.get(0);
        } else {
            return MediaType.TEXT_HTML_TYPE;
        }
    }
}
