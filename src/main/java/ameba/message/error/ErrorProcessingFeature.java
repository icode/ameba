package ameba.message.error;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author icode
 */
public class ErrorProcessingFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        context.register(DefaultExceptionMapper.class)
                .register(StatusMapper.class);
        return true;
    }
}
