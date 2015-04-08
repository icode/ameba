package ameba.core.ws.rs;

import ameba.message.internal.MediaType;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.List;

/**
 * @author icode
 */
public class OptionsAcceptPatchHeaderFilter implements ContainerResponseFilter {
    private static final String ACCEPT_PATCH_HEADER = "Accept-Patch";
    public static final List<String> SUPPORT_PATCH_MEDIA_TYPE =
            Lists.newArrayList(
                    MediaType.APPLICATION_JSON_PATCH,
                    MediaType.APPLICATION_JSON,
                    MediaType.APPLICATION_XML,
                    MediaType.TEXT_XML
            );

    private static String SUPPORT_PATCH_MEDIA = null;

    private static String getSupportPatchMedia() {
        if (SUPPORT_PATCH_MEDIA == null) {
            synchronized (SUPPORT_PATCH_MEDIA_TYPE) {
                if (SUPPORT_PATCH_MEDIA == null) {
                    SUPPORT_PATCH_MEDIA = StringUtils.join(SUPPORT_PATCH_MEDIA_TYPE, ",");
                }
            }
        }
        return SUPPORT_PATCH_MEDIA;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (HttpMethod.OPTIONS.equals(requestContext.getMethod())
                && responseContext.getHeaders().getFirst("Allow").toString().contains("PATCH")) {
            final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
            if (!headers.containsKey(ACCEPT_PATCH_HEADER)) {
                headers.putSingle(ACCEPT_PATCH_HEADER, getSupportPatchMedia());
            }
        }
    }
}
