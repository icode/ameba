package ameba.message.jackson.internal;

import ameba.core.Application;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;
import com.fasterxml.jackson.jaxrs.xml.XMLEndpointConfig;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;

/**
 * <p>JacksonXMLProvider class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@Priority(Priorities.ENTITY_CODER - 1)
public class JacksonXMLProvider extends JacksonJaxbXMLProvider {
    @Context
    private UriInfo uriInfo;
    @Inject
    private Application app;

    /**
     * <p>Constructor for JacksonXMLProvider.</p>
     *
     * @param xmlMapper a {@link com.fasterxml.jackson.dataformat.xml.XmlMapper} object.
     */
    @Inject
    public JacksonXMLProvider(XmlMapper xmlMapper) {
        super(xmlMapper, DEFAULT_ANNOTATIONS);
    }

    @Override
    protected XMLEndpointConfig _configForReading(ObjectReader reader, Annotation[] annotations) {
        JacksonUtils.configurePropertyNamingStrategy(uriInfo, reader.getConfig());
        return super._configForReading(reader, annotations);
    }

    @Override
    protected XMLEndpointConfig _configForWriting(ObjectWriter writer, Annotation[] annotations) {
        JacksonUtils.configurePropertyNamingStrategy(uriInfo, writer.getConfig());
        return super._configForWriting(writer, annotations);
    }

    @Override
    protected JsonGenerator _createGenerator(ObjectWriter writer, OutputStream rawStream, JsonEncoding enc) throws IOException {
        JsonGenerator generator = super._createGenerator(writer, rawStream, enc);
        JacksonUtils.configureGenerator(uriInfo, generator, app.getMode().isDev());
        return generator;
    }

}
