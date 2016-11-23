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
 * <p>RouteFeature class.</p>
 *
 * @author icode
 * @since 2013-08-07
 * @version $Id: $Id
 */
@Singleton
@ConstrainedTo(RuntimeType.SERVER)
public class RouteFeature implements Feature {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(final FeatureContext context) {

        final String routePath = (String) context.getConfiguration().getProperty("resource.helper.route.path");
        if (StringUtils.isNotBlank(routePath)) {
            context.register(new ModelProcessor() {
                @Override
                public ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {
                    ResourceModel.Builder resourceModelBuilder = new ResourceModel.Builder(resourceModel, false);
                    Resource resource = Resource.builder(RouteHelper.class).path(routePath).build();
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

        return false;
    }
}
