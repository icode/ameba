package ameba.message.jackson.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.glassfish.jersey.message.filtering.spi.AbstractObjectProvider;
import org.glassfish.jersey.message.filtering.spi.ObjectGraph;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 * @author icode
 */
final class JacksonObjectProvider extends AbstractObjectProvider<FilterProvider> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FilterProvider transform(final ObjectGraph graph) {
        // Root entity.
        final FilteringPropertyFilter root = new FilteringPropertyFilter(graph.getEntityClass(),
                graph.getFields(),
                createSubfilters(graph.getEntityClass(), graph.getSubgraphs()));

        return new FilteringFilterProvider(root);
    }

    private Map<String, FilteringPropertyFilter> createSubfilters(final Class<?> entityClass,
                                                                  final Map<String, ObjectGraph> entitySubgraphs) {
        final Set<String> processed = Sets.newHashSet();
        final Map<String, FilteringPropertyFilter> subfilters = Maps.newHashMap();

        for (final Map.Entry<String, ObjectGraph> entry : entitySubgraphs.entrySet()) {
            final String fieldName = entry.getKey();

            if (fieldName.startsWith("_")) continue;

            final ObjectGraph graph = entry.getValue();

            // Subgraph Fields.
            final Map<String, ObjectGraph> subgraphs = graph.getSubgraphs(fieldName);

            Map<String, FilteringPropertyFilter> subSubfilters = Maps.newHashMap();
            if (!subgraphs.isEmpty()) {
                final Class<?> subEntityClass = graph.getEntityClass();

                processed.add(getProcessedSubgraph(entityClass, fieldName, subEntityClass));
                subSubfilters = createSubfilters(fieldName, subEntityClass, subgraphs, processed);
            }

            final FilteringPropertyFilter filter = new FilteringPropertyFilter(graph.getEntityClass(),
                    graph.getFields(fieldName), subSubfilters);

            subfilters.put(fieldName, filter);
        }

        return subfilters;
    }

    private Map<String, FilteringPropertyFilter> createSubfilters(final String parent, final Class<?> entityClass,
                                                                  final Map<String, ObjectGraph> entitySubgraphs,
                                                                  final Set<String> processed) {
        final Map<String, FilteringPropertyFilter> subfilters = Maps.newHashMap();

        for (final Map.Entry<String, ObjectGraph> entry : entitySubgraphs.entrySet()) {
            final String fieldName = entry.getKey();

            if (fieldName.startsWith("_")) continue;

            final ObjectGraph graph = entry.getValue();

            final String path = parent + "." + fieldName;

            // Subgraph Fields.
            final Map<String, ObjectGraph> subgraphs = graph.getSubgraphs(path);

            final Class<?> subEntityClass = graph.getEntityClass();
            final String processedSubgraph = getProcessedSubgraph(entityClass, fieldName, subEntityClass);

            Map<String, FilteringPropertyFilter> subSubfilters = Maps.newHashMap();
            if (!subgraphs.isEmpty() && !processed.contains(processedSubgraph)) {
                processed.add(processedSubgraph);
                subSubfilters = createSubfilters(path, subEntityClass, subgraphs, processed);
            }

            subfilters.put(fieldName, new FilteringPropertyFilter(graph.getEntityClass(), graph.getFields(path), subSubfilters));
        }

        return subfilters;
    }

    private String getProcessedSubgraph(final Class<?> parent, final String field, final Class<?> fieldClass) {
        return parent.getName() + "_" + field + "_" + fieldClass.getName();
    }

    private static class FilteringFilterProvider extends FilterProvider {

        private final FilteringPropertyFilter root;
        private final Stack<FilteringPropertyFilter> stack = new Stack<FilteringPropertyFilter>();

        public FilteringFilterProvider(final FilteringPropertyFilter root) {
            this.root = root;
        }

        @Override
        public BeanPropertyFilter findFilter(final Object filterId) {
            throw new UnsupportedOperationException("Access to deprecated filters not supported");
        }

        @Override
        public PropertyFilter findPropertyFilter(final Object filterId, final Object valueToFilter) {
            if (filterId instanceof String) {
                final String id = (String) filterId;

                // FilterId should represent a class only in case of root entity is marshalled.
                if (id.equals(root.getEntityClass().getName())) {
                    stack.clear();
                    return stack.push(root);
                }

                while (!stack.isEmpty()) {
                    final FilteringPropertyFilter peek = stack.peek();
                    final FilteringPropertyFilter subfilter = peek.findSubfilter(id);

                    if (subfilter != null) {
                        stack.push(subfilter);

                        // Need special handling for maps here - map keys can be filtered as well so we just say that every key is
                        // allowed.
                        if (valueToFilter instanceof Map) {
                            final Map<String, ?> map = (Map<String, ?>) valueToFilter;
                            return new FilteringPropertyFilter(Map.class, map.keySet(),
                                    Collections.<String, FilteringPropertyFilter>emptyMap());
                        }
                        return subfilter;
                    } else {
                        stack.pop();
                    }
                }
            }
            return SimpleBeanPropertyFilter.filterOutAllExcept();
        }
    }

    private static final class FilteringPropertyFilter implements PropertyFilter {

        private final Class<?> entityClass;

        private final Set<String> fields;
        private final Map<String, FilteringPropertyFilter> subfilters;

        private FilteringPropertyFilter(final Class<?> entityClass,
                                        final Set<String> fields, final Map<String, FilteringPropertyFilter> subfilters) {
            this.entityClass = entityClass;

            this.fields = fields;
            this.subfilters = subfilters;
        }

        private boolean include(final String fieldName) {
            return fields.contains(fieldName) || subfilters.containsKey(fieldName);
        }

        @Override
        public void serializeAsField(final Object pojo,
                                     final JsonGenerator jgen,
                                     final SerializerProvider prov,
                                     final PropertyWriter writer) throws Exception {
            if (include(writer.getName())) {
                writer.serializeAsField(pojo, jgen, prov);
            }
        }

        @Override
        public void serializeAsElement(final Object elementValue,
                                       final JsonGenerator jgen,
                                       final SerializerProvider prov,
                                       final PropertyWriter writer) throws Exception {
            if (include(writer.getName())) {
                writer.serializeAsElement(elementValue, jgen, prov);
            }
        }

        @Override
        public void depositSchemaProperty(final PropertyWriter writer,
                                          final ObjectNode propertiesNode,
                                          final SerializerProvider provider) throws JsonMappingException {
            if (include(writer.getName())) {
                writer.depositSchemaProperty(propertiesNode, provider);
            }
        }

        @Override
        public void depositSchemaProperty(final PropertyWriter writer,
                                          final JsonObjectFormatVisitor objectVisitor,
                                          final SerializerProvider provider) throws JsonMappingException {
            if (include(writer.getName())) {
                writer.depositSchemaProperty(objectVisitor);
            }
        }

        public FilteringPropertyFilter findSubfilter(final String fieldName) {
            return subfilters.get(fieldName);
        }

        public Class<?> getEntityClass() {
            return entityClass;
        }
    }
}
