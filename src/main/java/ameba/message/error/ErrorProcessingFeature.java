package ameba.message.error;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * <p>ErrorProcessingFeature class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class ErrorProcessingFeature implements Feature {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(FeatureContext context) {
        context.register(DefaultExceptionMapper.class)
                .register(StatusMapper.class);
        return true;
    }
}
