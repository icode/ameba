package ameba.message.filtering;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.message.internal.AcceptableMediaType;
import org.glassfish.jersey.message.internal.HttpHeaderReader;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
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
@PreMatching
@Priority(Priorities.HEADER_DECORATOR)
public class MediaTypeFilter implements ContainerRequestFilter {
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
                    if (type.isWildcardType() && type.isWildcardSubtype()) {
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
        requestContext.getHeaders().putSingle(HttpHeaders.ACCEPT, MediaType.TEXT_HTML);
    }
}
