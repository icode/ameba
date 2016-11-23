package ameba.message.filtering;

import org.glassfish.jersey.server.filter.UriConnegFilter;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * <p>SysFilteringFeature class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class SysFilteringFeature implements Feature {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(FeatureContext context) {
        Configuration configuration = context.getConfiguration();
        if (!configuration.isRegistered(UriConnegFilter.class)) {
            context.register(UriConnegFilter.class);
        }

        if (!context.getConfiguration().isRegistered(DownloadEntityFilter.class)) {
            context.register(DownloadEntityFilter.class);
        }

        if (!configuration.isRegistered(LoadBalancerRequestFilter.class)) {
            context.register(LoadBalancerRequestFilter.class);
        }
        return true;
    }
}
