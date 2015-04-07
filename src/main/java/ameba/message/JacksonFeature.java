package ameba.message;

import ameba.message.internal.*;
import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;

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
        context.property(
                PropertiesHelper.getPropertyNameForRuntime(InternalProperties.JSON_FEATURE, config.getRuntimeType()),
                JSON_FEATURE);

        if (!config.isRegistered(JacksonJaxbJsonProvider.class)) {
            context.register(JsonParseExceptionMapper.class);
            context.register(JsonMappingExceptionMapper.class);
            if (EntityFilteringFeature.enabled(config)) {
                context.register(JacksonFilteringFeature.class);
                context.register(FilteringJacksonJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
                context.register(FilteringJacksonXMLProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
            } else {
                context.register(JacksonJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
                context.register(JacksonXMLProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
            }
        }
        return true;
    }

}