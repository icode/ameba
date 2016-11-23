package ameba.message.filtering;


import org.glassfish.jersey.message.filtering.EntityFilteringFeature;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * <p>
 * {@link javax.ws.rs.core.Feature} used to add support for custom query parameter filtering for
 * Entity Data Filtering feature. </p>
 * Note: This feature also registers the
 * {@link org.glassfish.jersey.message.filtering.EntityFilteringFeature}.
 *
 * @author Andy Pemberton (pembertona at gmail.com)
 * @author icode
 * @see org.glassfish.jersey.message.filtering.EntityFilteringFeature
 * @version $Id: $Id
 */
public final class EntityFieldsFilteringFeature implements Feature {

    /**
     * Constant <code>FIELDS_PARAM_NAME="model.query.param.fields"</code>
     */
    public static final String QUERY_FIELDS_PARAM_NAME = "model.query.param.fields";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        if (!config.isRegistered(EntityFieldsProcessor.class)) {

            // register EntityFilteringFeature
            if (!config.isRegistered(EntityFilteringFeature.class)) {
                context.register(EntityFilteringFeature.class);
            }
            // Entity Processors.
            context.register(EntityFieldsProcessor.class);
            // Scope Resolver.
            context.register(EntityFieldsScopeResolver.class);

            return true;
        }
        return false;
    }
}
