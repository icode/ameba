package ameba.core.ws.rs;

import ameba.message.internal.MediaType;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.HttpHeaders;

/**
 * @author icode
 */
@PreMatching
@Priority(Priorities.HEADER_DECORATOR)
public class DefaultContentTypeFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext request) {

        String accept = request.getHeaderString(HttpHeaders.ACCEPT);

        if (accept == null || accept.equals(MediaType.WILDCARD)) {
            request.getHeaders().putSingle(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        }

        String contentType = request.getHeaderString(HttpHeaders.CONTENT_TYPE);

        if (contentType == null
                || contentType.equals(MediaType.WILDCARD)
                || contentType.contains(MediaType.TEXT_PLAIN)) {
            request.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        }
    }
}