package ameba.mvc.template.internal;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * 404 跳转到模板
 *
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-27
 */
public class NotFoundForward implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (responseContext.getStatus() == Response.Status.NOT_FOUND.getStatusCode() || responseContext.getStatus() == Response.Status.METHOD_NOT_ALLOWED.getStatusCode()) {
            String path = requestContext.getUriInfo().getPath();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            responseContext.setStatus(Response.Status.OK.getStatusCode());
            responseContext.setEntity(Viewables.newDefaultViewable(path.equals("/") ? "/index" : path));
        }
    }
}
