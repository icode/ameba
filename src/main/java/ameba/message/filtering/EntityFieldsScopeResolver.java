package ameba.message.filtering;

import jersey.repackaged.com.google.common.collect.Sets;
import org.glassfish.jersey.internal.util.Tokenizer;
import org.glassfish.jersey.message.filtering.spi.ScopeResolver;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>EntityFieldsScopeResolver class.</p>
 *
 * @author icode
 *
 */
@Singleton
public class EntityFieldsScopeResolver implements ScopeResolver {

    /**
     * Prefix for all fields scopes
     */
    public static final String PREFIX = EntityFieldsScopeResolver.class.getName() + "_";

    /**
     * Scope used for selecting all fields, i.e.: when no filter is applied
     */
    public static final String DEFAULT_SCOPE = PREFIX + "*";

    /**
     * Query parameter name for fields feature, set to default value
     */
    static String FIELDS_PARAM_NAME = "fields";

    @Context
    private Configuration configuration;

    @Context
    private UriInfo uriInfo;

    @PostConstruct
    private void init() {
        final String paramName = (String) configuration.getProperty(EntityFieldsFilteringFeature.QUERY_FIELDS_PARAM_NAME);
        FIELDS_PARAM_NAME = paramName != null ? paramName : FIELDS_PARAM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> resolve(final Annotation[] annotations) {
        final Set<String> scopes = new HashSet<>();

        final List<String> fields = uriInfo.getQueryParameters().get(FIELDS_PARAM_NAME);
        if (fields != null && !fields.isEmpty()) {
            for (final String field : fields) {
                scopes.addAll(getScopesForField(field));
            }
        } else {
            scopes.add(DEFAULT_SCOPE);
        }
        return scopes;
    }

    private Set<String> getScopesForField(final String fieldName) {
        final Set<String> scopes = Sets.newHashSet();

        // add specific scope in case of specific request
        final String[] fields = Tokenizer.tokenize(fieldName, ",");
        for (final String field : fields) {
            final String[] subfields = Tokenizer.tokenize(field, ".");
            // in case of nested path, add first level as stand-alone to ensure subgraph is added
            scopes.add(EntityFieldsScopeResolver.PREFIX + subfields[0]);
            if (subfields.length > 1) {
                scopes.add(EntityFieldsScopeResolver.PREFIX + field);
            }
        }

        return scopes;
    }
}
