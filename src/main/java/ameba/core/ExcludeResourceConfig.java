package ameba.core;

import org.glassfish.jersey.server.ResourceConfig;

import java.util.Map;
import java.util.Set;

/**
 * @author icode
 */
class ExcludeResourceConfig extends ResourceConfig {

    private Set<String> excludes;

    /**
     * <p>Constructor for ExcludeResourceConfig.</p>
     *
     * @param excludes a {@link java.util.Set} object.
     */
    public ExcludeResourceConfig(Set<String> excludes) {
        this.excludes = excludes;
    }

    private boolean isExclude(Class cls) {
        if (excludes == null) return false;
        if (cls == null) return true;
        String className = cls.getName();
        for (String e : excludes) {
            if (e.endsWith(".**")) {
                if (className.startsWith(e.substring(0, e.length() - 3))) {
                    return true;
                }
            } else if (e.endsWith(".*")) {
                int index = e.length() - 2;
                if (className.startsWith(e.substring(0, index)) && className.indexOf(".", index + 1) == -1) {
                    return true;
                }
            } else if (className.equals(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceConfig register(Object component, Map<Class<?>, Integer> contracts) {
        if (component == null || isExclude(component.getClass())) return this;
        return super.register(component, contracts);
    }

    /** {@inheritDoc} */
    @Override
    public ResourceConfig register(Class<?> componentClass) {
        if (componentClass == null || isExclude(componentClass)) return this;
        return super.register(componentClass);
    }

    /** {@inheritDoc} */
    @Override
    public ResourceConfig register(Class<?> componentClass, int bindingPriority) {
        if (componentClass == null || isExclude(componentClass)) return this;
        return super.register(componentClass, bindingPriority);
    }

    /** {@inheritDoc} */
    @Override
    public ResourceConfig register(Class<?> componentClass, Class<?>... contracts) {
        if (componentClass == null || isExclude(componentClass)) return this;
        return super.register(componentClass, contracts);
    }

    /** {@inheritDoc} */
    @Override
    public ResourceConfig register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        if (componentClass == null || isExclude(componentClass)) return this;
        return super.register(componentClass, contracts);
    }

    /** {@inheritDoc} */
    @Override
    public ResourceConfig register(Object component) {
        if (component == null || isExclude(component.getClass())) return this;
        return super.register(component);
    }

    /** {@inheritDoc} */
    @Override
    public ResourceConfig register(Object component, int bindingPriority) {
        if (component == null || isExclude(component.getClass())) return this;
        return super.register(component, bindingPriority);
    }

    /** {@inheritDoc} */
    @Override
    public ResourceConfig register(Object component, Class<?>... contracts) {
        if (component == null || isExclude(component.getClass())) return this;
        return super.register(component, contracts);
    }
}
