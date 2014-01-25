package ameba.mvc.template.internal;

import ameba.mvc.route.RouteHelper;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;

/**
 * 404 跳转到模板
 *
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-27
 */
@Provider
public class NotFoundForward implements ExceptionMapper<NotFoundException> {

    @Inject
    private UriInfo uriInfo;

    @Override
    public Response toResponse(NotFoundException exception) {
        String path = uriInfo.getPath();
        return Response.ok(Viewables.newDefaultViewable(path.equals("/") ? "/index" : path)).build();
    }

}
