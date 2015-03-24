package ameba.message.internal;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;

/**
 * @author icode
 */
public class JacksonXMLProvider extends JacksonJaxbXMLProvider {

    protected static XmlMapper createDefaultMapper() {
        return new XmlMapper();
    }

    public JacksonXMLProvider() {
        this(createDefaultMapper(), DEFAULT_ANNOTATIONS);
    }

    public JacksonXMLProvider(XmlMapper mapper, Annotations[] annotationsToUse) {
        super(mapper, annotationsToUse);
        setAnnotationsToUse(annotationsToUse);
        JacksonUtils.configureMapper(mapper);
    }
}