package ameba.message.filtering;

import ameba.message.internal.PathProperties;
import ameba.util.bean.BeanInvoker;
import ameba.util.bean.BeanMap;
import ameba.util.bean.BeanTransformer;

import java.util.Set;

/**
 * @author icode
 */
public class FilteringBeanMap<T> extends BeanMap<T> {
    private static final BeanTransformer<FilteringBeanMap> TRANSFORMER = new Transform();
    private PathProperties pathProperties;

    public FilteringBeanMap(T bean, final PathProperties pathProperties) {
        transformer = TRANSFORMER;
        this.pathProperties = pathProperties;
        this.bean = bean;
        initialise();
    }

    private FilteringBeanMap(T bean) {
        this(bean, null);
    }

    public static Object from(Object src, final PathProperties pathProperties) {
        return new Transform() {
            @Override
            protected FilteringBeanMap onTransform(Object obj) {
                return new FilteringBeanMap<>(obj, pathProperties);
            }
        }.transform(src);
    }

    @Override
    protected Object transform(BeanInvoker invoker) throws Throwable {
        Object o = super.transform(invoker);
        if (o instanceof FilteringBeanMap) {
            final PathProperties pathProperties = new PathProperties();
            if (this.pathProperties != null) {
                pathProperties.put(null, pathProperties.get(invoker.getPropertyName()));
                ((FilteringBeanMap) o).pathProperties = pathProperties;
            }
        }
        return o;
    }

    @Override
    protected String transformPropertyName(final String name) {

        if (pathProperties != null) {
            Set<String> props = pathProperties.get(null);
            if (!props.contains("*") && !props.contains(name)) {
                return null;
            }
        }

        return super.transformPropertyName(name);
    }

    private static class Transform extends BeanTransformer<FilteringBeanMap> {
        @Override
        protected FilteringBeanMap onTransform(Object obj) {
            return new FilteringBeanMap<>(obj);
        }
    }
}