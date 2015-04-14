package ameba.mvc.assets;

import ameba.util.IOUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
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
import java.util.List;
import java.util.Map;

/**
 * <p>AssetsFeature class.</p>
 *
 * @author ICode
 * @since 13-8-17 下午2:55
 */
@ConstrainedTo(RuntimeType.SERVER)
public class AssetsFeature implements Feature {

    private static final Map<String, String[]> assetsMap = Maps.newLinkedHashMap();
    private static final String ASSETS_CONF_PREFIX = "resource.assets.";

    /**
     * <p>getAssetMap.</p>
     *
     * @param configuration a {@link javax.ws.rs.core.Configuration} object.
     * @return a {@link java.util.Map} object.
     */
    public static Map<String, String[]> getAssetMap(Configuration configuration) {
        Map<String, String[]> assetsMap = Maps.newLinkedHashMap();
        for (String key : configuration.getPropertyNames()) {
            if (key.startsWith(ASSETS_CONF_PREFIX) || key.equals("resource.assets")) {
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
                String[] uris = value.split(",");
                List<String> uriList = Lists.newArrayList();
                for (String uri : uris) {
                    uriList.add(uri.endsWith("/") ? uri : uri + "/");
                }
                if (StringUtils.isNotBlank(value)) {
                    String[] _uris = assetsMap.get(routePath);
                    if (_uris == null) {
                        assetsMap.put(routePath, uriList.toArray(uris));
                    } else {
                        assetsMap.put(routePath, ArrayUtils.addAll(_uris, uriList.toArray(uris)));
                    }
                }

            }
        }
        return assetsMap;
    }

    /**
     * <p>Getter for the field <code>assetsMap</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public static Map<String, String[]> getAssetsMap() {
        return Maps.newHashMap(assetsMap);
    }

    /**
     * <p>findAsset.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param file a {@link java.lang.String} object.
     * @return a {@link java.io.InputStream} object.
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(FeatureContext context) {

        Configuration configuration = context.getConfiguration();

        assetsMap.putAll(getAssetMap(configuration));

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
}
