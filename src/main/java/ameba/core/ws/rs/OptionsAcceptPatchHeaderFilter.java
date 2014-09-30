package ameba.core.ws.rs;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

/**
 * @author icode
 */
public class OptionsAcceptPatchHeaderFilter implements ContainerResponseFilter {
    private static final String ACCEPT_PATCH_HEADER = "Accept-Patch";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        if (HttpMethod.OPTIONS.equals(requestContext.getMethod())) {
            final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
            if (!headers.containsKey(ACCEPT_PATCH_HEADER)) {
                headers.putSingle(ACCEPT_PATCH_HEADER, PatchingInterceptor.PATCH_MEDIA_TYPE);
            }
        }
    }
}
