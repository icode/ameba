package ameba.message.internal;

import org.glassfish.jersey.message.internal.ReaderWriter;

import javax.inject.Singleton;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author icode
 */
@Singleton
@ConstrainedTo(RuntimeType.SERVER)
final class TextMessageBodyWriter implements MessageBodyWriter<Object> {

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType) {
        return type != null
                && mediaType.getType().equals(MediaType.TEXT_PLAIN_TYPE.getType())
                && mediaType.getSubtype().equals(MediaType.TEXT_PLAIN_TYPE.getSubtype());
    }

    @Override
    public long getSize(final Object viewable, final Class<?> type, final Type genericType,
                        final Annotation[] annotations, final MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(final Object entity,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {

        if (entity != null)
            ReaderWriter.writeToAsString(entity.toString(), entityStream, mediaType);
    }
}