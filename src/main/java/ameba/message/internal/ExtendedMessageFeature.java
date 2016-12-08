package ameba.message.internal;

import ameba.message.internal.streaming.*;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * <p>ExtendedMessageFeature class.</p>
 *
 * @author icode
 *
 */
public class ExtendedMessageFeature implements Feature {
    /**
     * {@inheritDoc}
     */
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

        if (!context.getConfiguration().isRegistered(StreamingWriterInterceptor.class)) {
            context.register(StreamingWriterInterceptor.class);
            // streaming process
            context.register(BlobStreamingProcess.class);
            context.register(BytesStreamingProcess.class);
            context.register(ClobStreamingProcess.class);
            context.register(FileStreamingProcess.class);
            context.register(InputStreamingProcess.class);
            context.register(PathStreamingProcess.class);
        }

        return false;
    }
}
