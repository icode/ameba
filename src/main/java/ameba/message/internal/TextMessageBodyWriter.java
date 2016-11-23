package ameba.message.internal;

import com.google.common.base.Charsets;

import javax.inject.Singleton;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Produces;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * @author icode
 */
@Produces({"application/octet-stream", "*/*"})
@Singleton
@ConstrainedTo(RuntimeType.SERVER)
final class TextMessageBodyWriter implements MessageBodyWriter<Object> {

    /**
     * <p>getCharset.</p>
     *
     * @param m a {@link javax.ws.rs.core.MediaType} object.
     * @return a {@link java.nio.charset.Charset} object.
     */
    public static Charset getCharset(MediaType m) {
        String name = (m == null) ? null : m.getParameters().get(MediaType.CHARSET_PARAMETER);
        try {
            return (name == null) ? Charsets.UTF_8 : Charset.forName(name);
        } catch (Exception e) {
            return Charsets.UTF_8;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType) {
        return type != null
                && mediaType.getType().equals(MediaType.TEXT_PLAIN_TYPE.getType())
                && mediaType.getSubtype().equals(MediaType.TEXT_PLAIN_TYPE.getSubtype());
    }

    /** {@inheritDoc} */
    @Override
    public long getSize(final Object viewable, final Class<?> type, final Type genericType,
                        final Annotation[] annotations, final MediaType mediaType) {
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public void writeTo(final Object entity,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {

        if (entity != null) {
            Writer osw = new OutputStreamWriter(entityStream, getCharset(mediaType));
            String s = entity.toString();
            osw.write(s, 0, s.length());
            osw.flush();
        }
    }
}
