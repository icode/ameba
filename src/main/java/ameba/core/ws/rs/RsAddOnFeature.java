package ameba.core.ws.rs;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.model.ModelProcessor;

import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author icode
 */
public class RsAddOnFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {

        if (!context.getConfiguration().isRegistered(JsonPatchInterceptor.class)) {
            context.register(JsonPatchInterceptor.class);
        }

        if (!context.getConfiguration().isRegistered(DefaultContentTypeFilter.class)) {
            context.register(DefaultContentTypeFilter.class);
        }

        context.register(new AddOnBinder());
        return false;
    }

    private static class AddOnBinder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(OptionsMethodProcessor.DefaultOptionsResponseGenerator.class)
                    .to(OptionsResponseGenerator.class)
                    .in(Singleton.class);

            bind(OptionsMethodProcessor.class)
                    .to(ModelProcessor.class)
                    .in(Singleton.class);
        }
    }
}
