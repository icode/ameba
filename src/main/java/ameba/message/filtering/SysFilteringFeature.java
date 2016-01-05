package ameba.message.filtering;

import org.glassfish.jersey.server.filter.UriConnegFilter;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author icode
 */
public class SysFilteringFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        Configuration configuration = context.getConfiguration();
        if (!configuration.isRegistered(UriConnegFilter.class)) {
            context.register(UriConnegFilter.class);
        }

        if (!configuration.isRegistered(RangeResponseFilter.class)) {
            context.register(RangeResponseFilter.class);
        }
        return true;
    }
}
