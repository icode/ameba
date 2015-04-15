package ameba.message.internal;

import ameba.core.Application;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
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
    @Inject
    private Provider<ContainerRequest> requestProvider;

    /**
     * <p>Constructor for JacksonJsonProvider.</p>
     *
     * @param app          a {@link ameba.core.Application} object.
     * @param objectMapper a {@link com.fasterxml.jackson.databind.ObjectMapper} object.
     */
    @Inject
    public JacksonJsonProvider(Application app, ObjectMapper objectMapper) {
        this(app, objectMapper, DEFAULT_ANNOTATIONS);
    }

    /**
     * <p>Constructor for JacksonJsonProvider.</p>
     *
     * @param app           a {@link ameba.core.Application} object.
     * @param objectMapper  a {@link com.fasterxml.jackson.databind.ObjectMapper} object.
     * @param annotationses an array of {@link com.fasterxml.jackson.jaxrs.cfg.Annotations} objects.
     */
    public JacksonJsonProvider(Application app, ObjectMapper objectMapper, Annotations[] annotationses) {
        super(objectMapper, annotationses);
        JacksonUtils.configureMapper(app.getMode().isDev(), objectMapper);
    }

    @Override
    protected JsonGenerator _createGenerator(ObjectWriter writer, OutputStream rawStream, JsonEncoding enc) throws IOException {
        JsonGenerator generator = super._createGenerator(writer, rawStream, enc);
        if (requestProvider.get().getMethod().equalsIgnoreCase("get")) {
            JacksonUtils.configureGenerator(uriInfoProvider.get(), generator);
        }
        return generator;
    }
}
