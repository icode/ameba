package ameba.core.ws.rs;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.model.ModelProcessor;

import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ParamConverterProvider;

/**
 * <p>RsAddonFeature class.</p>
 *
 * @author icode
 * @since 0.1.6e
 *
 */
public class RsAddonFeature implements Feature {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(FeatureContext context) {

//        if (!context.getConfiguration().isRegistered(JsonPatchInterceptor.class)) {
//            context.register(JsonPatchInterceptor.class);
//        }

        context.register(new AddonBinder());
        return false;
    }

    private static class AddonBinder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(OptionsMethodProcessor.DefaultOptionsResponseGenerator.class)
                    .to(OptionsResponseGenerator.class)
                    .in(Singleton.class);

            bind(OptionsMethodProcessor.class)
                    .to(ModelProcessor.class)
                    .in(Singleton.class);

            bind(ParamConverters.TypeFromStringEnum.class)
                    .to(ParamConverterProvider.class)
                    .in(Singleton.class).ranked(10);

            bind(ParamConverters.DateProvider.class)
                    .to(ParamConverterProvider.class)
                    .in(Singleton.class).ranked(10);

            bind(ParamConverters.BooleanProvider.class)
                    .to(ParamConverterProvider.class)
                    .in(Singleton.class).ranked(10);
        }
    }
}
