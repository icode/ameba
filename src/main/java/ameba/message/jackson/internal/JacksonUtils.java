package ameba.message.jackson.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfigBase;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import org.apache.commons.lang3.StringUtils;
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
     * @param isDev  a boolean.
     * @param mapper a {@link com.fasterxml.jackson.databind.ObjectMapper} object.
     */
    public static void configureMapper(boolean isDev, ObjectMapper mapper) {
        mapper.registerModule(new JodaModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(SerializationFeature.FAIL_ON_SELF_REFERENCES)
                .disable(SerializationFeature.WRITE_NULL_MAP_VALUES)
                .disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        if (isDev)
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static void configureGenerator(UriInfo uriInfo, JsonGenerator generator) {
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        String pretty = params.getFirst("pretty");
        if (pretty != null && !pretty.equalsIgnoreCase("false")) {
            generator.useDefaultPrettyPrinter();
        } else if ("false".equalsIgnoreCase(pretty)) {
            generator.setPrettyPrinter(null);
        }
    }

    /**
     * get naming query param for PropertyNamingStrategy
     * <p/>
     * u : change set {@link PropertyNamingStrategy#CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES}
     * <p/>
     * l : change set {@link PropertyNamingStrategy#LOWER_CASE}
     * <p/>
     * p : change set {@link PropertyNamingStrategy#PASCAL_CASE_TO_CAMEL_CASE}
     *
     * @param uriInfo    UriInfo
     * @param configBase MapperConfigBase
     */
    public static void configurePropertyNamingStrategy(UriInfo uriInfo, MapperConfigBase configBase) {
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        String naming = params.getFirst("naming");
        if (StringUtils.isNotBlank(naming)) {
            switch (naming) {
                case "u":
                    configBase.with(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
                    break;
                case "l":
                    configBase.with(PropertyNamingStrategy.LOWER_CASE);
                    break;
                case "p":
                    configBase.with(PropertyNamingStrategy.PASCAL_CASE_TO_CAMEL_CASE);
            }
        }
    }
}
