package ameba.message.internal;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;

/**
 * @author icode
 */
@Singleton
@Priority(Integer.MIN_VALUE + 100)
public class StreamingWriterInterceptor implements WriterInterceptor, ContainerResponseFilter {

    private static final String ACCEPT_RANGES = "Accept-Ranges";
    private static final String BYTES_RANGE = "bytes";
    private static final String IF_RANGE = "If-Range";
    private static final String RESP_PROP_N = StreamingWriterInterceptor.class.getName() + ".responseContext";
    @Inject
    private Provider<ContainerRequestContext> requestProvider;
    @Inject
    private ServiceLocator locator;

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        if (isWritable(context)) {
            MultivaluedMap<String, Object> respHeaders = context.getHeaders();
            ContainerRequestContext requestContext = requestProvider.get();
            MultivaluedMap<String, String> reqHeaders = requestContext.getHeaders();
            if (reqHeaders.containsKey(MediaStreaming.RANGE)) {
                if (reqHeaders.containsKey(IF_RANGE)) {
                    String ifRangeHeader = reqHeaders.getFirst(IF_RANGE);
                    if (StringUtils.isBlank(ifRangeHeader)) {
                        return;
                    }
                    if (respHeaders.containsKey(HttpHeaders.ETAG)) {
                        if (MessageHelper.getHeaderString(respHeaders, HttpHeaders.ETAG)
                                .equals(ifRangeHeader)) {
                            applyStreaming(requestContext, context);
                            return;
                        }
                    }
                    if (respHeaders.containsKey(HttpHeaders.LAST_MODIFIED)) {
                        if (MessageHelper.getHeaderString(respHeaders, HttpHeaders.LAST_MODIFIED)
                                .equals(ifRangeHeader)) {
                            applyStreaming(requestContext, context);
                        }
                    }
                } else {
                    applyStreaming(requestContext, context);
                }
            }
        }
        context.proceed();
    }

    protected boolean isWritable(WriterInterceptorContext context) {
        return context.getEntity() != null
                && !Boolean.FALSE.equals(requestProvider.get().getProperty(MessageHelper.STREAMING_RANGE_ENABLED))
                && context.getHeaders().containsKey(HttpHeaders.CONTENT_LENGTH)
                && !(context.getEntity() instanceof MediaStreaming);
    }

    protected void applyStreaming(ContainerRequestContext requestContext, WriterInterceptorContext context)
            throws IOException {

        Object entity = context.getEntity();
        StreamingProcess<Object> process = MessageHelper.getStreamingProcess(context.getEntity(), locator);

        if (process != null) {
            ContainerResponseContext responseContext =
                    (ContainerResponseContext) requestContext.getProperty(RESP_PROP_N);
            responseContext.setStatusInfo(Response.Status.PARTIAL_CONTENT);
            context.getHeaders().putSingle(ACCEPT_RANGES, BYTES_RANGE);
            context.setType(StreamingOutput.class);
            context.setEntity(new MediaStreaming(
                            entity,
                    requestContext.getHeaderString(MediaStreaming.RANGE),
                            process,
                            context.getMediaType(),
                            context.getHeaders()
                    )
            );
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        requestContext.setProperty(RESP_PROP_N, responseContext);
    }
}