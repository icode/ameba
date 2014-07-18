package ameba.mvc.template;

import ameba.mvc.ErrorPageFeature;
import ameba.mvc.template.internal.HttlViewProcessor;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.FeatureContext;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
@ConstrainedTo(RuntimeType.SERVER)
public class HttlErrorPageFeature extends ErrorPageFeature {
    protected void registGenerator(final FeatureContext context, final Class generatorClazz) {
        if (!context.getConfiguration().isRegistered(HttlViewProcessor.class)) {
            context.register(HttlViewProcessor.class);
        }

        context.register(generatorClazz);
    }
}