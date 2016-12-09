package ameba.message.jackson.internal;

import ameba.core.Application;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.message.filtering.spi.ObjectProvider;

import javax.inject.Provider;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * <p>JacksonUtils class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public class JacksonUtils {

    private JacksonUtils() {
    }

    /**
     * <p>configFilterIntrospector.</p>
     *
     * @param mapper a {@link com.fasterxml.jackson.databind.ObjectMapper} object.
     * @return a {@link com.fasterxml.jackson.databind.ObjectMapper} object.
     */
    public static ObjectMapper configFilterIntrospector(final ObjectMapper mapper) {
        final AnnotationIntrospector customIntrospector = mapper.getSerializationConfig().getAnnotationIntrospector();
        // Set the custom (user) introspector to be the primary one.
        return mapper.setAnnotationIntrospector(AnnotationIntrospector.pair(customIntrospector,
                new JacksonAnnotationIntrospector() {
                    @Override
                    public Object findFilterId(final Annotated a) {
                        final Object filterId = super.findFilterId(a);

                        if (filterId != null) {
                            return filterId;
                        }

                        if (a instanceof AnnotatedMethod) {
                            final Method method = ((AnnotatedMethod) a).getAnnotated();

                            // Interested only in getters - trying to obtain "field" name from them.
                            if (ReflectionHelper.isGetter(method)) {
                                return ReflectionHelper.getPropertyName(method);
                            }
                        }
                        if (a instanceof AnnotatedField || a instanceof AnnotatedClass) {
                            return a.getName();
                        }

                        return null;
                    }
                }));

    }

    /**
     * <p>setObjectWriterInjector.</p>
     *
     * @param provider    a {@link javax.inject.Provider} object.
     * @param genericType a {@link java.lang.reflect.Type} object.
     * @param annotations an array of {@link java.lang.annotation.Annotation} objects.
     * @throws java.io.IOException if any.
     */
    public static void setObjectWriterInjector(Provider<ObjectProvider<FilterProvider>> provider,
                                               final Type genericType,
                                               final Annotation[] annotations) throws IOException {
        final FilterProvider filterProvider = provider.get().getFilteringObject(genericType, true, annotations);
        if (filterProvider != null) {
            ObjectWriterInjector.set(new FilteringObjectWriterModifier(filterProvider, ObjectWriterInjector.getAndClear()));
        }
    }

    /**
     * <p>configureMapper.</p>
     *
     * @param mapper a {@link com.fasterxml.jackson.databind.ObjectMapper} object.
     * @param mode   App mode
     */
    public static void configureMapper(ObjectMapper mapper, Application.Mode mode) {
        mapper.registerModule(new GuavaModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .enable(SerializationFeature.WRITE_ENUMS_USING_INDEX)
                .disable(
                        SerializationFeature.WRITE_NULL_MAP_VALUES,
                        SerializationFeature.FAIL_ON_EMPTY_BEANS
                );
        if (!mode.isDev()) {
            mapper.disable(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
            );
        }
    }

    /**
     * <p>configureGenerator.</p>
     *
     * @param uriInfo   a {@link javax.ws.rs.core.UriInfo} object.
     * @param generator a {@link com.fasterxml.jackson.core.JsonGenerator} object.
     * @param isDev     a boolean.
     */
    public static void configureGenerator(UriInfo uriInfo, JsonGenerator generator, boolean isDev) {
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        String pretty = params.getFirst("pretty");
        if ("false".equalsIgnoreCase(pretty)) {
            generator.setPrettyPrinter(null);
        } else if (pretty != null && !"false".equalsIgnoreCase(pretty) || isDev) {
            generator.useDefaultPrettyPrinter();
        }
        String unicode = params.getFirst("unicode");
        if (unicode != null && !"false".equalsIgnoreCase(unicode)) {
            generator.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        }
    }
}
