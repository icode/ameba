package ameba.message.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * @author icode
 */
public class JacksonJsonProvider extends JacksonJaxbJsonProvider {
    public JacksonJsonProvider() {
        this(new ObjectMapper(), DEFAULT_ANNOTATIONS);
    }

    public JacksonJsonProvider(ObjectMapper objectMapper, Annotations[] annotationses) {
        super(objectMapper, annotationses);
        JacksonUtils.configureMapper(objectMapper);
    }
}