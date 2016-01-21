package ameba.message.jackson.internal;

import ameba.core.Application;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import javax.inject.Inject;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Produces;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>JacksonJsonProvider class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@ConstrainedTo(RuntimeType.SERVER)
@Produces({"application/json", "text/json"})
public class JacksonJsonProvider extends JacksonJaxbJsonProvider {

    @Context
    private UriInfo uriInfo;
    @Inject
    private Application.Mode mode;

    /**
     * <p>Constructor for JacksonJsonProvider.</p>
     *
     * @param objectMapper a {@link com.fasterxml.jackson.databind.ObjectMapper} object.
     */
    @Inject
    public JacksonJsonProvider(ObjectMapper objectMapper) {
        super(objectMapper, DEFAULT_ANNOTATIONS);
    }

    @Override
    protected JsonGenerator _createGenerator(ObjectWriter writer, OutputStream rawStream, JsonEncoding enc) throws IOException {
        JsonGenerator generator = super._createGenerator(writer, rawStream, enc);
        JacksonUtils.configureGenerator(uriInfo, generator, mode.isDev());
        return generator;
    }
}
