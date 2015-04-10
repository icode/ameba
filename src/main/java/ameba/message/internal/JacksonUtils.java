package ameba.message.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import com.google.common.collect.Sets;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.message.filtering.spi.ObjectProvider;

import javax.inject.Provider;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author icode
 */
public class JacksonUtils {

    private static Set<Module> defaultModules = Sets.newLinkedHashSet();

    private JacksonUtils() {
    }

    public static void addDefaultModule(Module module) {
        defaultModules.add(module);
    }

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

    public static void setObjectWriterInjector(Provider<ObjectProvider<FilterProvider>> provider,
                                               final Type genericType,
                                               final Annotation[] annotations) throws IOException {
        final FilterProvider filterProvider = provider.get().getFilteringObject(genericType, true, annotations);
        if (filterProvider != null) {
            ObjectWriterInjector.set(new FilteringObjectWriterModifier(filterProvider, ObjectWriterInjector.getAndClear()));
        }
    }

    public static void configureMapper(ObjectMapper mapper) {
        mapper.registerModules(defaultModules);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
    }
}
