package ameba.message.jackson;

import ameba.core.Application;
import ameba.core.ws.rs.HttpPatchProperties;
import ameba.message.internal.MediaType;
import ameba.message.jackson.internal.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;

import javax.inject.Inject;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.Collections;

/**
 * <p>JacksonFeature class.</p>
 *
 * @author ICode
 * @since 13-8-11 上午6:00
 */
@ConstrainedTo(RuntimeType.SERVER)
public class JacksonFeature implements Feature {

    private final static String JSON_FEATURE = JacksonFeature.class.getSimpleName();

    @Inject
    private ServiceLocator locator;
    @Inject
    private Application.Mode mode;

    /**
     * {@inheritDoc}
     */
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

        if (!config.isRegistered(JacksonJsonProvider.class)) {
            ServiceLocatorUtilities.bind(locator, new AbstractBinder() {
                @Override
                protected void configure() {
                    {
                        final XmlMapper xmlMapper = new XmlMapper();
                        JacksonUtils.configureMapper(xmlMapper, mode);
                        // 不能用 jdk lambda，否则无法绑定
                        bind(new ContextResolver<XmlMapper>() {
                            @Override
                            public XmlMapper getContext(Class<?> type) {
                                return xmlMapper;
                            }
                        }).to(new TypeLiteral<ContextResolver<XmlMapper>>() {
                        });
                        bind(xmlMapper).to(XmlMapper.class).proxy(false);
                    }
                    {
                        final ObjectMapper objectMapper = new ObjectMapper();
                        JacksonUtils.configureMapper(objectMapper, mode);
                        // 不能用 jdk lambda，否则无法绑定
                        bind(new ContextResolver<ObjectMapper>() {
                            @Override
                            public ObjectMapper getContext(Class<?> type) {
                                return objectMapper;
                            }
                        }).to(new TypeLiteral<ContextResolver<ObjectMapper>>() {
                        });
                        bind(objectMapper).to(ObjectMapper.class).proxy(false);
                    }
                }
            });

            context.register(JsonProcessingExceptionMapper.class);
            if (EntityFilteringFeature.enabled(config)) {
                context.register(JacksonFilteringFeature.class);
                context.register(FilteringJacksonJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
                context.register(FilteringJacksonXMLProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
            } else {
                context.register(JacksonJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
                context.register(JacksonXMLProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
            }
            Collections.addAll(HttpPatchProperties.SUPPORT_PATCH_MEDIA_TYPES,
                    MediaType.APPLICATION_JSON,
                    MediaType.APPLICATION_XML,
                    MediaType.TEXT_XML);
            return true;
        }
        return false;
    }

}
