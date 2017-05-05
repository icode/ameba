package ameba.message.jackson.internal;


import com.fasterxml.jackson.databind.ser.FilterProvider;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.message.filtering.spi.ObjectGraphTransformer;
import org.glassfish.jersey.message.filtering.spi.ObjectProvider;

import javax.inject.Singleton;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.GenericType;

/**
 * {@link javax.ws.rs.core.Feature} adding support for Entity Data Filtering into Jackson media module.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 * @author icode
 * @since 0.1.6e
 *
 */
public class JacksonFilteringFeature implements Feature {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        if (!config.isRegistered(JacksonFilteringFeature.Binder.class)) {
            context.register(new Binder());
            return true;
        }
        return false;
    }

    private static final class Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bindAsContract(JacksonObjectProvider.class)
                    // FilteringObjectProvider.
                    .to(new GenericType<ObjectProvider<FilterProvider>>() {
                    })
                            // FilteringGraphTransformer.
                    .to(new GenericType<ObjectGraphTransformer<FilterProvider>>() {
                    })
                            // Scope.
                    .in(Singleton.class);
        }
    }
}
