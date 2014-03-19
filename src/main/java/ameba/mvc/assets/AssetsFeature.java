package ameba.mvc.assets;

import ameba.util.IOUtils;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceModel;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.io.InputStream;
import java.util.Map;

/**
 * @author: ICode
 * @since: 13-8-17 下午2:55
 */
@ConstrainedTo(RuntimeType.SERVER)
public class AssetsFeature implements Feature {

    private static final Map<String, String[]> assetsMap = Maps.newLinkedHashMap();

    @Override
    public boolean configure(FeatureContext context) {

        Configuration configuration = context.getConfiguration();

        for (String key : configuration.getPropertyNames()) {
            if (key.startsWith("resource.assets.") || key.equals("resource.assets")) {
                String routePath = key.replaceFirst("^resource\\.assets", "");
                if (routePath.startsWith(".")) {
                    routePath = routePath.substring(1);
                } else if (StringUtils.isBlank(routePath)) {
                    routePath = "assets";
                }

                if (routePath.endsWith("/")) {
                    routePath = routePath.substring(0, routePath.lastIndexOf("/"));
                }

                String value = (String) configuration.getProperty(key);
                if (StringUtils.isNotBlank(value))
                    assetsMap.put(routePath, value.split(","));
            }
        }

        context.register(new ModelProcessor() {
            @Override
            public ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {
                ResourceModel.Builder resourceModelBuilder = new ResourceModel.Builder(resourceModel, false);

                for (String routePath : assetsMap.keySet()) {
                    Resource.Builder resourceBuilder = Resource.builder(AssetsResource.class);
                    resourceBuilder.path(routePath);
                    Resource resource = resourceBuilder.build();
                    resourceModelBuilder.addResource(resource);
                }

                return resourceModelBuilder.build();
            }

            @Override
            public ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration) {
                return subResourceModel;
            }
        });
        return true;
    }

    public static Map<String, String[]> getAssetsMap() {
        return Maps.newHashMap(assetsMap);
    }

    public static InputStream findAsset(String name, String file) {
        InputStream in = null;

        if (name.startsWith("/")) {
            name = name.substring(1);
        }

        if (name.endsWith("/")) {
            name = name.substring(0, name.lastIndexOf("/"));
        }

        String[] dirs = assetsMap.get(name);
        if (dirs != null) {
            for (String dir : dirs) {
                if (!dir.endsWith("/") && !file.startsWith("/")) {
                    dir = dir + "/";
                }
                in = IOUtils.getResourceAsStream(dir + file);
                if (in != null) {
                    break;
                }
            }
        }
        return in;
    }
}
