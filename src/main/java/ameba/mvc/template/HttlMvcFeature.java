package ameba.mvc.template;

import ameba.mvc.template.internal.AmebaMvcFeature;
import ameba.mvc.template.internal.HttlViewProcessor;
import ameba.mvc.template.internal.NotFoundForward;
import org.glassfish.jersey.server.mvc.MvcFeature;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
@ConstrainedTo(RuntimeType.SERVER)
public class HttlMvcFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        if (!context.getConfiguration().isRegistered(MvcFeature.class)) {
            context.register(AmebaMvcFeature.class);
        }

        if (!context.getConfiguration().isRegistered(NotFoundForward.class)) {
            context.register(NotFoundForward.class);
        }

        context.register(HttlViewProcessor.class);
        return true;
    }
}