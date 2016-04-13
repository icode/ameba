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

        if (!context.getConfiguration().isRegistered(CaptchaWriterInterceptor.class)) {
            context.register(CaptchaWriterInterceptor.class);
        }

        if (!context.getConfiguration().isRegistered(PathMessageBodyWriter.class)) {
            context.register(PathMessageBodyWriter.class);
        }

        if (!context.getConfiguration().isRegistered(ContentLengthWriterInterceptor.class)) {
            context.register(ContentLengthWriterInterceptor.class);
        }

        return false;
    }
}
