package ameba.message.filtering;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;

/**
 * @author icode
 */
public class RangeResponseFilter implements ContainerResponseFilter {

    private static final String ACCEPT_RANGES = "Accept-Ranges";

    private static final String BYTES_RANGE = "bytes";

    private static final String RANGE = "Range";

    private static final String IF_RANGE = "If-Range";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        if (isWriteable(responseContext)) {
            responseContext.getHeaders().add(ACCEPT_RANGES, BYTES_RANGE);

            if (requestContext.getHeaders().containsKey(RANGE)) {
                if (requestContext.getHeaders().containsKey(IF_RANGE)) {
                    String ifRangeHeader = requestContext.getHeaderString(IF_RANGE);
                    if (responseContext.getHeaders().containsKey(HttpHeaders.ETAG)) {
                        if (responseContext.getHeaderString(HttpHeaders.ETAG).equals(ifRangeHeader)) {
                            this.applyFilter(requestContext, responseContext);
                            return;
                        }
                    }
                    if (responseContext.getHeaders().containsKey(HttpHeaders.LAST_MODIFIED)) {
                        if (responseContext.getHeaderString(HttpHeaders.LAST_MODIFIED).equals(ifRangeHeader)) {
                            this.applyFilter(requestContext, responseContext);
                        }
                    }
                } else {
                    this.applyFilter(requestContext, responseContext);
                }
            }
        }
    }

    public boolean isWriteable(ContainerResponseContext responseContext) {
        Object entity = responseContext.getEntity();
        return entity != null &&
                (entity instanceof File
                        || entity instanceof InputStream
                        || entity instanceof StreamingOutput
                        || entity instanceof Reader
                        || entity.getClass() == byte[].class
                );
    }

    private void applyFilter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {

        String rangeHeader = requestContext.getHeaderString(RANGE);
        String contentType = responseContext.getMediaType().toString();
        OutputStream originOutputStream = responseContext.getEntityStream();
        RangedOutputStream rangedOutputStream = new RangedOutputStream(originOutputStream, rangeHeader, contentType,
                responseContext.getHeaders());
        responseContext.setStatus(Status.PARTIAL_CONTENT.getStatusCode());
        responseContext.setEntityStream(rangedOutputStream);

    }

}