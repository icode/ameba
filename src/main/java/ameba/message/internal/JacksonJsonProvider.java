package ameba.message.internal;

import ameba.core.Application;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * <p>JacksonJsonProvider class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@Singleton
public class JacksonJsonProvider extends JacksonJaxbJsonProvider {

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
}
