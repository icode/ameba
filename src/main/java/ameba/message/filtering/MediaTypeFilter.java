package ameba.message.filtering;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.message.internal.AcceptableMediaType;
import org.glassfish.jersey.message.internal.HttpHeaderReader;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * fix request media type
 *
 * @author icode
 */
@Priority(Priorities.HEADER_DECORATOR)
public class MediaTypeFilter implements ContainerRequestFilter {
    private static final MediaType LOW_IE_DEFAULT_REQ_TYPE = new MediaType("application", "x-ms-application");

    @Inject
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String acceptHeader = requestContext.getHeaderString(HttpHeaders.ACCEPT);

        if (StringUtils.isBlank(acceptHeader)) {
            applyHeader(requestContext);
        } else {
            try {
                List<AcceptableMediaType> acceptableMediaTypes = HttpHeaderReader.readAcceptMediaType(acceptHeader);
                if (acceptableMediaTypes.size() > 0) {
                    AcceptableMediaType type = acceptableMediaTypes.get(0);
                    if (LOW_IE_DEFAULT_REQ_TYPE.getType().equals(type.getType())
                            && LOW_IE_DEFAULT_REQ_TYPE.getSubtype().equals(type.getSubtype())) {
                        applyHeader(requestContext, false);
                    } else if (type.isWildcardType() && type.isWildcardSubtype()) {
                        applyHeader(requestContext);
                    }
                } else {
                    applyHeader(requestContext);
                }
            } catch (ParseException e) {
                applyHeader(requestContext);
            }
        }
    }

    private void applyHeader(ContainerRequestContext requestContext) {
        applyHeader(requestContext, true);
    }

    private void applyHeader(ContainerRequestContext requestContext, boolean checkResource) {
        if (checkResource) {
            Produces methodProduces = resourceInfo.getResourceMethod().getAnnotation(Produces.class);
            if (hasProduces(methodProduces)) {
                return;
            }
            Produces classProduces = resourceInfo.getResourceClass().getAnnotation(Produces.class);
            if (hasProduces(classProduces)) {
                return;
            }
        }
        requestContext.getHeaders().putSingle(HttpHeaders.ACCEPT, MediaType.TEXT_HTML);
    }

    private boolean hasProduces(Produces produces) {
        if (produces != null && produces.value().length > 0) {
            String firstType = produces.value()[0];
            if (StringUtils.isNotBlank(firstType) && !"*/*".equals(firstType)) {
                return true;
            }
        }
        return false;
    }
}
