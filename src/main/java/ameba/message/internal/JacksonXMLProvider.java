package ameba.message.internal;

import ameba.core.Application;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * <p>JacksonXMLProvider class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@Singleton
public class JacksonXMLProvider extends JacksonJaxbXMLProvider {

    /**
     * <p>Constructor for JacksonXMLProvider.</p>
     *
     * @param app       a {@link ameba.core.Application} object.
     * @param xmlMapper a {@link com.fasterxml.jackson.dataformat.xml.XmlMapper} object.
     */
    @Inject
    public JacksonXMLProvider(Application app, XmlMapper xmlMapper) {
        this(app, xmlMapper, DEFAULT_ANNOTATIONS);

    }

    /**
     * <p>Constructor for JacksonXMLProvider.</p>
     *
     * @param app              a {@link ameba.core.Application} object.
     * @param mapper           a {@link com.fasterxml.jackson.dataformat.xml.XmlMapper} object.
     * @param annotationsToUse an array of {@link com.fasterxml.jackson.jaxrs.cfg.Annotations} objects.
     */
    public JacksonXMLProvider(Application app, XmlMapper mapper, Annotations[] annotationsToUse) {
        super(mapper, annotationsToUse);
        setAnnotationsToUse(annotationsToUse);
        JacksonUtils.configureMapper(app.getMode().isDev(), mapper);
    }

    /**
     * <p>createDefaultMapper.</p>
     *
     * @return a {@link com.fasterxml.jackson.dataformat.xml.XmlMapper} object.
     */
    protected static XmlMapper createDefaultMapper() {
        return new XmlMapper();
    }
}
