package ameba.core.ws.rs;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author icode
 */
public class RsAddOnFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {

        if (!context.getConfiguration().isRegistered(OptionsAcceptPatchHeaderFilter.class)) {
            context.register(OptionsAcceptPatchHeaderFilter.class);
        }
        if (!context.getConfiguration().isRegistered(PatchingInterceptor.class)) {
            context.register(PatchingInterceptor.class);
        }
        if (!context.getConfiguration().isRegistered(DefaultContentTypeFilter.class)) {
            context.register(DefaultContentTypeFilter.class);
        }
        return false;
    }
}
