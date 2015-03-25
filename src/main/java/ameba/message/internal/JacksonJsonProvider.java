package ameba.message.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

/**
 * @author icode
 */
@Consumes({MediaType.TEXT_PLAIN, MediaType.WILDCARD})
@Produces({MediaType.TEXT_PLAIN, MediaType.WILDCARD})
public class JacksonJsonProvider extends JacksonJaxbJsonProvider {
    public JacksonJsonProvider() {
        this(new ObjectMapper(), DEFAULT_ANNOTATIONS);
    }

    public JacksonJsonProvider(ObjectMapper objectMapper, Annotations[] annotationses) {
        super(objectMapper, annotationses);
        JacksonUtils.configureMapper(objectMapper);
    }
}