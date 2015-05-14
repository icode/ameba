package ameba.message.internal;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author icode
 */
public class ExtendedMessageFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        if (!context.getConfiguration().isRegistered(TextMessageBodyWriter.class)) {
            context.register(TextMessageBodyWriter.class);
        }
        return false;
    }
}
