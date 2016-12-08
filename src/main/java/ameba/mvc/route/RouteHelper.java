package ameba.mvc.route;

import com.google.common.collect.Lists;
import org.glassfish.jersey.server.ExtendedResourceContext;
import org.glassfish.jersey.server.model.Resource;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 路由器帮助类
 *
 * @author icode
 * @since 13-8-9 下午7:45
 */
@Singleton
@Path("route")
public class RouteHelper {
    @Context
    private ExtendedResourceContext resourceContext;

    /**
     * <p>getRoutes.</p>
     *
     * @return a {@link java.util.List} object.
     */
    @GET
    public List<String> getRoutes() {
        List<Resource> resourceList = resourceContext.getResourceModel().getResources();
        List<String> routeList = Lists.newArrayList();
        for (Resource resource : resourceList) {
            String path = resource.getPath().startsWith("/") ? "" : "/" + resource.getPath();
            if (resource.getAllMethods().size() > 0) {
                routeList.add(path);
            }
            routeList.addAll(
                    resource.getChildResources()
                            .stream()
                            .map(res -> path + (res.getPath().startsWith("/") ? "" : "/") + res.getPath())
                            .collect(Collectors.toList())
            );
        }
        return routeList;
    }

}
