package ameba.message.jackson.internal;

import ameba.core.Application;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.jaxrs.xml.XMLEndpointConfig;
import org.glassfish.jersey.message.filtering.spi.ObjectProvider;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * <p>FilteringJacksonXMLProvider class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@Singleton
public class FilteringJacksonXMLProvider extends JacksonXMLProvider {

    @Inject
    private Provider<ObjectProvider<FilterProvider>> provider;
    @Context
    private UriInfo uriInfo;
    @Inject
    private Application.Mode mode;

    @Override
    protected JsonGenerator _createGenerator(ObjectWriter writer, OutputStream rawStream, JsonEncoding enc) throws IOException {
        JsonGenerator generator = super._createGenerator(writer, rawStream, enc);
        JacksonUtils.configureGenerator(uriInfo, generator, mode.isDev());
        return generator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected XMLEndpointConfig _configForWriting(final XmlMapper mapper, final Annotation[] annotations,
                                                  final Class<?> defaultView) {

        return super._configForWriting(
                (XmlMapper) JacksonUtils.configFilterIntrospector(mapper)
                , annotations, defaultView);
    }

    /**
     * {@inheritDoc}
     */
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
