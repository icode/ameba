package ameba.message.jackson.internal;

import ameba.core.Application;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;

import javax.inject.Inject;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>JacksonXMLProvider class.</p>
 *
 * @author icode
 * @since 0.1.6e
 * @version $Id: $Id
 */
@ConstrainedTo(RuntimeType.SERVER)
@Consumes({"text/xml", "application/xml"})
@Produces({"text/xml", "application/xml"})
public class JacksonXMLProvider extends JacksonJaxbXMLProvider {
    @Context
    private UriInfo uriInfo;
    @Inject
    private Application.Mode mode;

    /**
     * {@inheritDoc}
     */
    @Override
    protected JsonGenerator _createGenerator(ObjectWriter writer, OutputStream rawStream, JsonEncoding enc) throws IOException {
        JsonGenerator generator = super._createGenerator(writer, rawStream, enc);
        JacksonUtils.configureGenerator(uriInfo, generator, mode.isDev());
        return generator;
    }

}
