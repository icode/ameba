package ameba.message.filtering;

import jersey.repackaged.com.google.common.collect.Sets;
import org.glassfish.jersey.message.filtering.spi.AbstractEntityProcessor;
import org.glassfish.jersey.message.filtering.spi.EntityGraph;
import org.glassfish.jersey.message.filtering.spi.EntityProcessor;
import org.glassfish.jersey.message.filtering.spi.FilteringHelper;

import javax.annotation.Priority;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * <p>EntityFieldsProcessor class.</p>
 *
 * @author icode
 *
 */
@Singleton
@Priority(Integer.MAX_VALUE - 5000)
public class EntityFieldsProcessor extends AbstractEntityProcessor {

    /**
     * {@inheritDoc}
     */
    protected Result process(final String fieldName, final Class<?> fieldClass, final Annotation[] fieldAnnotations,
                             final Annotation[] annotations, final EntityGraph graph) {

        if (fieldName != null) {
            final Set<String> scopes = Sets.newHashSet();

            // add default fields scope in case of none requested
            scopes.add(EntityFieldsScopeResolver.DEFAULT_SCOPE);

            // add specific scope in case of specific request
            scopes.add(EntityFieldsScopeResolver.PREFIX + fieldName);

            if (FilteringHelper.filterableEntityClass(fieldClass)) {
                if (Collection.class.isAssignableFrom(fieldClass)
                        || Map.class.isAssignableFrom(fieldClass)) {

                    throw new IllegalArgumentException("field name: " + fieldName + " type: " + fieldClass
                            + " must be have a explicit generics; eg. List<MyEntity> list= ...");
                }
            }
            addFilteringScopes(fieldName, fieldClass, scopes, graph);
        }

        return EntityProcessor.Result.APPLY;
    }

}
