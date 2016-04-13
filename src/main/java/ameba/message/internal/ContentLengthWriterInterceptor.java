package ameba.message.internal;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author icode
 */
@Singleton
@Priority(Integer.MIN_VALUE)
public class ContentLengthWriterInterceptor implements WriterInterceptor {
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        Object entity = context.getEntity();
        if (entity != null) {
            Long size = null;
            if (entity instanceof Path) {
                size = Files.size(((Path) entity));
            } else if (entity instanceof File) {
                size = ((File) entity).length();
            } else if (entity.getClass() == byte[].class) {
                size = (long) ((byte[]) entity).length;
            }
            if (size != null) {
                context.getHeaders().putSingle(HttpHeaders.CONTENT_LENGTH, size);
            }
        }
        context.proceed();
    }
}
