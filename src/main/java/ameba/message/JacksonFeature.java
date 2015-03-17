package ameba.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;
import org.glassfish.jersey.CommonProperties;
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

        final String jsonFeature = CommonProperties.getValue(config.getProperties(), config.getRuntimeType(),
                InternalProperties.JSON_FEATURE, JSON_FEATURE, String.class);
        // Other JSON providers registered.
        if (!JSON_FEATURE.equalsIgnoreCase(jsonFeature)) {
            return false;
        }

        // Disable other JSON providers.
        context.property(PropertiesHelper.getPropertyNameForRuntime(InternalProperties.JSON_FEATURE, config.getRuntimeType()),
                JSON_FEATURE);

        if (!config.isRegistered(JacksonJaxbJsonProvider.class)) {
            context.register(JsonParseExceptionMapper.class);
            context.register(JsonMappingExceptionMapper.class);
            context.register(JacksonJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
            context.register(JacksonXMLProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
        }
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
        mapper.findAndRegisterModules();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
    }

    public static class JacksonXMLProvider extends JacksonJaxbXMLProvider {

        protected static XmlMapper createDefaultMapper() {
            return new XmlMapper();
        }

        public JacksonXMLProvider() {
            this(createDefaultMapper(), DEFAULT_ANNOTATIONS);
        }

        public JacksonXMLProvider(XmlMapper mapper, Annotations[] annotationsToUse) {
            super(mapper, annotationsToUse);
            setAnnotationsToUse(annotationsToUse);
            configureMapper(mapper);
        }
    }
}