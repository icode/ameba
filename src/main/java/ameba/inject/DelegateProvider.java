package ameba.inject;

import javax.inject.Provider;

/**
 * @author icode
 */
public class DelegateProvider<T> implements Provider<T> {
    private T target;

    private DelegateProvider(T target) {
        this.target = target;
    }

    public static <T> DelegateProvider<T> create(T target) {
        return new DelegateProvider<>(target);
    }

    @Override
    public T get() {
        return target;
    }
}
