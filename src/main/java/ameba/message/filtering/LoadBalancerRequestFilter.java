package ameba.message.filtering;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.List;

/**
 * <p>LoadBalancerRequestFilter class.</p>
 *
 * @author icode
 *
 */
@PreMatching
@Priority(500)
@Singleton
public class LoadBalancerRequestFilter implements ContainerRequestFilter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {

        String scheme = getValue(ctx.getHeaders(), "x-forwarded-proto");
        String port = getValue(ctx.getHeaders(), "x-forwarded-port");
        if (scheme == null && port == null)
            return;

        UriBuilder baseBuilder = ctx.getUriInfo().getBaseUriBuilder();
        UriBuilder requestBuilder = ctx.getUriInfo().getRequestUriBuilder();
        if (scheme != null) {
            baseBuilder.scheme(scheme);
            requestBuilder.scheme(scheme);
            baseBuilder.port(443);
            requestBuilder.port(443);
        }

        if (port != null) {
            int nPort = Integer.parseInt(port);
            baseBuilder.port(nPort);
            requestBuilder.port(nPort);
        }

        ctx.setRequestUri(baseBuilder.build(), requestBuilder.build());
    }

    private String getValue(MultivaluedMap<String, String> headers, String header) {
        List<String> values = headers.get(header);
        if (values == null || values.isEmpty())
            return null;

        return values.get(0);
    }
}
