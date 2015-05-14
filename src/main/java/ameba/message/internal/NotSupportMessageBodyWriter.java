package ameba.message.internal;

import ameba.message.error.ErrorMessage;
import ameba.util.IOUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.glassfish.jersey.message.internal.ReaderWriter;

import javax.annotation.Priority;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * @author icode
 */
@Singleton
@ConstrainedTo(RuntimeType.SERVER)
@Priority(Integer.MAX_VALUE)
@Produces("*/*")
final class NotSupportMessageBodyWriter implements MessageBodyWriter<Object> {

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType) {
        return true;
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

        Charset charset;
        try {
            charset = ReaderWriter.getCharset(mediaType);
        } catch (Exception e) {
            charset = Charsets.UTF_8;
        }
        httpHeaders.put("Content-Type", Lists.<Object>newArrayList(MediaType.TEXT_PLAIN));
        IOUtils.write(ErrorMessage.getLocaleMessage(406), entityStream, charset);
    }
}