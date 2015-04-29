package ameba.message.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.jaxrs.cfg.EndpointConfigBase;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterModifier;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

/**
 * <p>FilteringObjectWriterModifier class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public class FilteringObjectWriterModifier extends ObjectWriterModifier {

    private final ObjectWriterModifier original;
    private final FilterProvider filterProvider;

    /**
     * <p>Constructor for FilteringObjectWriterModifier.</p>
     *
     * @param filterProvider a {@link com.fasterxml.jackson.databind.ser.FilterProvider} object.
     * @param original       a {@link com.fasterxml.jackson.jaxrs.cfg.ObjectWriterModifier} object.
     */
    public FilteringObjectWriterModifier(final FilterProvider filterProvider, final ObjectWriterModifier original) {
        this.original = original;
        this.filterProvider = filterProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectWriter modify(final EndpointConfigBase<?> endpoint,
                               final MultivaluedMap<String, Object> responseHeaders,
                               final Object valueToWrite,
                               final ObjectWriter w,
                               final JsonGenerator g) throws IOException {
        final ObjectWriter writer = original == null ? w : original.modify(endpoint, responseHeaders, valueToWrite, w, g);
        final FilterProvider customFilterProvider = writer.getConfig().getFilterProvider();

        // Try the custom (user) filter provider first.
        return customFilterProvider == null
                ? writer.with(filterProvider)
                : writer.with(new FilterProvider() {
            @Override
            public BeanPropertyFilter findFilter(final Object filterId) {
                return customFilterProvider.findFilter(filterId);
            }

            @Override
            public PropertyFilter findPropertyFilter(final Object filterId, final Object valueToFilter) {
                final PropertyFilter filter = customFilterProvider.findPropertyFilter(filterId, valueToFilter);
                if (filter != null) {
                    return filter;
                }

                return filterProvider.findPropertyFilter(filterId, valueToFilter);
            }
        });
    }
}
