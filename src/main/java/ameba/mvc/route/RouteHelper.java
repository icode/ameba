package ameba.mvc.route;

import com.google.common.collect.Lists;
import org.glassfish.jersey.server.ExtendedResourceContext;
import org.glassfish.jersey.server.model.Resource;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
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
