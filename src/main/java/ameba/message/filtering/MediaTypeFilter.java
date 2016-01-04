package ameba.message.filtering;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.message.internal.MediaTypes;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.Produces;
import javax.ws.rs.container.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * fix request media type
 *
 * @author icode
 */
@PreMatching
@Singleton
@Priority(Priorities.HEADER_DECORATOR)
public class MediaTypeFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final String LOW_IE_MEDIA = MediaTypeFilter.class + ".LOW_IE";
    private static final MediaType LOW_IE_REQ_TYPE = new MediaType("application", "x-ms-application");

    private void applyMediaType(ContainerRequestContext requestContext) {
        if (requestContext != null)
            requestContext.getHeaders().putSingle(HttpHeaders.ACCEPT, MediaType.TEXT_HTML);
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        List<MediaType> types = requestContext.getAcceptableMediaTypes();
        if (types.size() > 0) {
            MediaType type = types.get(0);
            if (LOW_IE_REQ_TYPE.getType().equals(type.getType())
                    && LOW_IE_REQ_TYPE.getSubtype().equals(type.getSubtype())) {
                applyMediaType(requestContext);
                requestContext.setProperty(LOW_IE_MEDIA, true);
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        Boolean lowIe = (Boolean) requestContext.getProperty(LOW_IE_MEDIA);
        if (lowIe == null) {
            List<MediaType> types = requestContext.getAcceptableMediaTypes();
            if (types == null || (types.size() > 0 && MediaTypes.isWildcard(types.get(0)))) {
                Annotation[] annotations = responseContext.getEntityAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation instanceof Produces) {
                        Produces produces = (Produces) annotation;
                        for (String produce : produces.value()) {
                            if (StringUtils.isNotBlank(produce)
                                    && !MediaTypes.isWildcard(MediaType.valueOf(produce))) {
                                return;
                            }
                        }
                    }
                }
                applyMediaType(requestContext);
                responseContext.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML);
            }
        }
    }
}
