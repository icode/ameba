package ameba.feature;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;
import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.internal.util.PropertiesHelper;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * @author ICode
 * @since 13-8-11 上午6:00
 */
public class JacksonFeature implements Feature {

    private final static String JSON_FEATURE = JacksonFeature.class.getSimpleName();

    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        context.property(PropertiesHelper.getPropertyNameForRuntime(InternalProperties.JSON_FEATURE, config.getRuntimeType()),
                JSON_FEATURE);

        context.register(JsonParseExceptionMapper.class);
        context.register(JsonMappingException.class);
        context.register(JacksonJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
        context.register(JacksonXMLProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
        return true;
    }


    public static class JacksonJsonProvider extends JacksonJaxbJsonProvider {
        public JacksonJsonProvider() {
            this(new ObjectMapper(), DEFAULT_ANNOTATIONS);
        }

        public JacksonJsonProvider(ObjectMapper objectMapper, Annotations[] annotationses) {
            super(objectMapper, annotationses);
            configureMapper(objectMapper);
        }
    }

    public static void configureMapper(ObjectMapper mapper) {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public static class JacksonXMLProvider extends JacksonJaxbXMLProvider {

        protected static XmlMapper createDefaultMapper() {
            JacksonXmlModule module = new JacksonXmlModule();
            module.setDefaultUseWrapper(false);
            return new XmlMapper(module);
        }

        public JacksonXMLProvider() {
            this(createDefaultMapper(), DEFAULT_ANNOTATIONS);
        }

        public JacksonXMLProvider(XmlMapper mapper, Annotations[] annotationsToUse) {
            super(mapper, annotationsToUse);
            configureMapper(mapper);
        }
    }
}