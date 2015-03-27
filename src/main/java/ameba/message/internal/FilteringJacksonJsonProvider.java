package ameba.message.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.jaxrs.json.JsonEndpointConfig;
import org.glassfish.jersey.message.filtering.spi.ObjectProvider;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author icode
 */
@Singleton
@Consumes({MediaType.TEXT_PLAIN, MediaType.WILDCARD})
@Produces({MediaType.TEXT_PLAIN, MediaType.WILDCARD})
public class FilteringJacksonJsonProvider extends JacksonJsonProvider {

    @Inject
    private Provider<ObjectProvider<FilterProvider>> provider;

    @Override
    protected JsonEndpointConfig _configForWriting(final ObjectMapper mapper, final Annotation[] annotations,
                                                   final Class<?> defaultView) {

        return super._configForWriting(
                JacksonUtils.configFilterIntrospector(mapper)
                , annotations, defaultView);
    }

    @Override
    public void writeTo(final Object value,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final javax.ws.rs.core.MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException {

        JacksonUtils.setObjectWriterInjector(provider, genericType, annotations);
        super.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

}
