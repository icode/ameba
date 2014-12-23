package ameba.mvc.route;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceModel;

import javax.inject.Singleton;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
@Singleton
@ConstrainedTo(RuntimeType.SERVER)
public class RouteFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(new ModelProcessor() {
            @Override
            public ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {
                ResourceModel.Builder resourceModelBuilder = new ResourceModel.Builder(resourceModel, false);
                String routePath = (String) configuration.getProperty("resource.helper.route.path");
                Resource.Builder resourceBuilder = Resource.builder(RouteHelper.class);
                if (StringUtils.isNotBlank(routePath)) {
                    resourceBuilder.path(routePath);
                }
                Resource resource = resourceBuilder.build();
                resourceModelBuilder.addResource(resource);
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