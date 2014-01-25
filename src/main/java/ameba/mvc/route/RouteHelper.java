package ameba.mvc.route;

import com.google.common.collect.Lists;
import org.glassfish.jersey.server.ExtendedResourceContext;
import org.glassfish.jersey.server.model.Resource;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.List;

/**
 * 路由器帮助类
 *
 * @author: ICode
 * @since: 13-8-9 下午7:45
 */
@Singleton
@Path("route")
public class RouteHelper {

    private static final ThreadLocal<ContainerRequestContext> reqLocal = new ThreadLocal<ContainerRequestContext>();

    public static class RouteRequestFilter implements ContainerRequestFilter, ContainerResponseFilter {

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            reqLocal.set(requestContext);
        }

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            reqLocal.remove();
        }
    }

    public static ContainerRequestContext getCurrentRequestContext() {
        return reqLocal.get();
    }

    @Context
    private ExtendedResourceContext resourceContext;

    @GET
    public List<String> getRoutes() {
        List<Resource> resourceList = resourceContext.getResourceModel().getResources();
        List<String> routeList = Lists.newArrayList();
        for (Resource resource : resourceList) {
            String path = resource.getPath().startsWith("/") ? "" : "/" + resource.getPath();
            if (resource.getAllMethods().size() > 0) {
                routeList.add(path);
            }
            for (Resource res : resource.getChildResources()) {
                routeList.add(path + (res.getPath().startsWith("/") ? "" : "/") + res.getPath());
            }
        }
        return routeList;
    }

}
