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
import org.glassfish.jersey.CommonProperties;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author: ICode
 * @since: 13-8-11 上午6:00
 */
public class JacksonFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        final String disableMoxy = CommonProperties.MOXY_JSON_FEATURE_DISABLE + '.'
                + context.getConfiguration().getRuntimeType().name().toLowerCase();
        context.property(disableMoxy, true);

        context.register(JacksonJsonProvider.class);
        context.register(JacksonXMLProvider.class);
        context.register(JsonParseExceptionMapper.class);
        context.register(JsonMappingException.class);
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