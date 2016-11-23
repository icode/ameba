package ameba.message.filtering;

import ameba.message.internal.BeanPathProperties;
import ameba.util.bean.BeanInvoker;
import ameba.util.bean.BeanMap;
import ameba.util.bean.BeanTransformer;

import java.util.Set;

/**
 * <p>FilteringBeanMap class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class FilteringBeanMap<T> extends BeanMap<T> {
    private static final BeanTransformer<FilteringBeanMap> TRANSFORMER = new Transformer();
    private BeanPathProperties pathProperties;

    /**
     * <p>Constructor for FilteringBeanMap.</p>
     *
     * @param bean           a T object.
     * @param pathProperties a {@link ameba.message.internal.BeanPathProperties} object.
     */
    public FilteringBeanMap(T bean, final BeanPathProperties pathProperties) {
        transformer = TRANSFORMER;
        this.pathProperties = pathProperties;
        this.bean = bean;
        initialise();
    }

    private FilteringBeanMap(T bean) {
        this(bean, null);
    }

    /**
     * <p>from.</p>
     *
     * @param src a {@link java.lang.Object} object.
     * @param pathProperties a {@link ameba.message.internal.BeanPathProperties} object.
     * @return a {@link java.lang.Object} object.
     */
    public static Object from(Object src, final BeanPathProperties pathProperties) {
        return new Transformer() {
            @Override
            protected FilteringBeanMap onTransform(Object obj) {
                return new FilteringBeanMap<>(obj, pathProperties);
            }
        }.transform(src);
    }

    /** {@inheritDoc} */
    @Override
    protected Object transform(BeanInvoker invoker) throws Throwable {
        Object o = super.transform(invoker);
        if (o instanceof FilteringBeanMap) {
            final BeanPathProperties pathProperties = new BeanPathProperties();
            if (this.pathProperties != null) {
                pathProperties.put(null, pathProperties.getProperties(invoker.getPropertyName()));
                ((FilteringBeanMap) o).pathProperties = pathProperties;
            }
        }
        return o;
    }

    /** {@inheritDoc} */
    @Override
    protected String transformPropertyName(final String name) {

        if (pathProperties != null) {
            Set<String> props = pathProperties.getProperties(null);
            if (!props.contains("*") && !props.contains(name)) {
                return null;
            }
        }

        return super.transformPropertyName(name);
    }

    private static class Transformer extends BeanTransformer<FilteringBeanMap> {
        @Override
        protected FilteringBeanMap onTransform(Object obj) {
            return new FilteringBeanMap<>(obj);
        }
    }
}
