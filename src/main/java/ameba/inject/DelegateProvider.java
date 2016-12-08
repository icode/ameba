package ameba.inject;

import javax.inject.Provider;

/**
 * <p>DelegateProvider class.</p>
 *
 * @author icode
 *
 */
public class DelegateProvider<T> implements Provider<T> {
    private T target;

    private DelegateProvider(T target) {
        this.target = target;
    }

    /**
     * <p>create.</p>
     *
     * @param target a T object.
     * @param <T>    a T object.
     * @return a {@link ameba.inject.DelegateProvider} object.
     */
    public static <T> DelegateProvider<T> create(T target) {
        return new DelegateProvider<>(target);
    }

    /** {@inheritDoc} */
    @Override
    public T get() {
        return target;
    }
}
