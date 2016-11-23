package ameba.message.internal;

import org.glassfish.hk2.api.ServiceLocator;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;

/**
 * <p>ContentLengthWriterInterceptor class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
@Singleton
@Priority(Integer.MIN_VALUE)
public class ContentLengthWriterInterceptor implements WriterInterceptor {

    @Inject
    private ServiceLocator locator;

    /**
     * {@inheritDoc}
     */
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        if (!context.getHeaders().containsKey(HttpHeaders.CONTENT_LENGTH)) {
            Object entity = context.getEntity();
            StreamingProcess<Object> process = MessageHelper.getStreamingProcess(entity, locator);

            if (process != null) {
                long length = process.length(entity);

                if (length != -1)
                    context.getHeaders().putSingle(HttpHeaders.CONTENT_LENGTH, length);
            }
        }

        context.proceed();
    }
}
