package ameba.message.internal;

import org.glassfish.jersey.message.internal.ReaderWriter;

import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author icode
 */
@Produces({"application/octet-stream", "*/*"})
@Singleton
public class PathMessageBodyWriter implements MessageBodyWriter<Path> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Path.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Path path, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Path path, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        try (InputStream in = Files.newInputStream(path)) {
            ReaderWriter.writeTo(in, entityStream);
        }
    }
}
