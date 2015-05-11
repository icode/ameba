package ameba.message.jackson.internal;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.server.ContainerRequest;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>JacksonJsonProvider class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@Singleton
public class JacksonJsonProvider extends JacksonJaxbJsonProvider {

    @Inject
    private Provider<UriInfo> uriInfoProvider;

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
        JacksonUtils.configureGenerator(uriInfoProvider.get(), generator);
        return generator;
    }
}
