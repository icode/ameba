package ameba.websocket.internal;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.jersey.internal.Errors;
import org.glassfish.jersey.internal.util.LazyUid;
import org.glassfish.jersey.internal.util.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkState;

/**
 * <p>MessageScope class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@Singleton
public class MessageScope implements Context<MessageScoped> {

    private static Logger logger = LoggerFactory.getLogger(MessageScope.class);

    private ThreadLocal<Instance> currentScopeInstance = new ThreadLocal<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Annotation> getScope() {
        return MessageScoped.class;
    }

    /**
     * <p>referenceCurrent.</p>
     *
     * @return a {@link ameba.websocket.internal.MessageScope.Instance} object.
     * @throws java.lang.IllegalStateException if any.
     */
    public Instance referenceCurrent() throws IllegalStateException {
        return current().getReference();
    }

    /**
     * <p>suspendCurrent.</p>
     *
     * @return a {@link ameba.websocket.internal.MessageScope.Instance} object.
     */
    public Instance suspendCurrent() {
        final Instance scopeInstance = currentScopeInstance.get();
        if (scopeInstance == null) {
            return null;
        }
        try {
            return scopeInstance.getReference();
        } finally {
            logger.debug("Returned a new reference of the message scope instance {}", scopeInstance);
        }
    }

    private Instance current() {
        Instance scopeInstance = currentScopeInstance.get();
        checkState(scopeInstance != null, "Not inside a request scope.");
        return scopeInstance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor, ServiceHandle<?> root) {
        final Instance instance = current();

        U retVal = instance.get(activeDescriptor);
        if (retVal == null) {
            retVal = activeDescriptor.create(root);
            instance.put(activeDescriptor, retVal);
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        Instance instance = current();
        return instance.contains(descriptor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        final Instance instance = current();
        instance.remove(descriptor);
    }

    /**
     * <p>createInstance.</p>
     *
     * @return a {@link ameba.websocket.internal.MessageScope.Instance} object.
     */
    public Instance createInstance() {
        return new Instance();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsNullCreation() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        currentScopeInstance = null;
    }


    /**
     * Runs the {@link Runnable task} in the request scope initialized from the
     * {@link MessageScope.Instance scope instance}. The {@link MessageScope.Instance
     * scope instance} is NOT released by the method (this must be done explicitly). The
     * current thread might be already in any request scope and in that case the scope
     * will be changed to the scope defined by the {@link MessageScope.Instance scope
     * instance}. At the end of the method the request scope is returned to its original
     * state.
     *
     * @param scopeInstance The request scope instance from which the request scope will
     *                      be initialized.
     * @param task          Task to be executed.
     */
    public void runInScope(Instance scopeInstance, Runnable task) {
        Instance oldInstance = currentScopeInstance.get();
        try {
            currentScopeInstance.set(scopeInstance.getReference());
            Errors.process(task);
        } finally {
            scopeInstance.release();
            currentScopeInstance.set(oldInstance);
        }
    }

    /**
     * Runs the {@link Runnable task} in the new request scope. The current thread might
     * be already in any request scope and in that case the scope will be changed to the
     * scope defined by the {@link MessageScope.Instance scope instance}. At the end of
     * the method the request scope is returned to its original state. The newly created
     * {@link MessageScope.Instance scope instance} will be implicitly released at the end
     * of the method call except the task will call
     * {@link ameba.websocket.internal.MessageScope#suspendCurrent}.
     *
     * @param task Task to be executed.
     */
    public void runInScope(Runnable task) {
        Instance oldInstance = currentScopeInstance.get();
        Instance instance = createInstance();
        try {
            currentScopeInstance.set(instance);
            Errors.process(task);
        } finally {
            instance.release();
            currentScopeInstance.set(oldInstance);
        }
    }

    /**
     * Runs the {@link java.util.concurrent.Callable task} in the request scope initialized from the
     * {@link MessageScope.Instance scope instance}. The {@link MessageScope.Instance
     * scope instance} is NOT released by the method (this must be done explicitly). The
     * current thread might be already in any request scope and in that case the scope
     * will be changed to the scope defined by the {@link MessageScope.Instance scope
     * instance}. At the end of the method the request scope is returned to its original
     * state.
     *
     * @param scopeInstance The request scope instance from which the request scope will
     *                      be initialized.
     * @param task          Task to be executed.
     * @param <T>           {@code task} result type.
     * @return result returned by the {@code task}.
     * @throws java.lang.Exception java.lang.Exception thrown by the {@code task}.
     */
    public <T> T runInScope(Instance scopeInstance, Callable<T> task) throws Exception {
        Instance oldInstance = currentScopeInstance.get();
        try {
            currentScopeInstance.set(scopeInstance.getReference());
            return Errors.process(task);
        } finally {
            scopeInstance.release();
            currentScopeInstance.set(oldInstance);
        }
    }

    /**
     * Runs the {@link Callable task} in the new request scope. The current thread might
     * be already in any request scope and in that case the scope will be changed to the
     * scope defined by the {@link MessageScope.Instance scope instance}. At the end of
     * the method the request scope is returned to its original state. The newly created
     * {@link MessageScope.Instance scope instance} will be implicitly released at the end
     * of the method call except the task will call
     * {@link ameba.websocket.internal.MessageScope#suspendCurrent}.
     *
     * @param task Task to be executed.
     * @param <T>  {@code task} result type.
     * @return result returned by the {@code task}.
     * @throws java.lang.Exception java.lang.Exception thrown by the {@code task}.
     */
    public <T> T runInScope(Callable<T> task) throws Exception {
        Instance oldInstance = currentScopeInstance.get();
        Instance instance = createInstance();
        try {
            currentScopeInstance.set(instance);
            return Errors.process(task);
        } finally {
            instance.release();
            currentScopeInstance.set(oldInstance);
        }
    }

    /**
     * Runs the {@link org.glassfish.jersey.internal.util.Producer task} in the request scope initialized
     * from the {@link MessageScope.Instance scope instance}.
     * The {@link MessageScope.Instance scope instance} is NOT released by the method (this
     * must be done explicitly). The current thread might be already in any request scope
     * and in that case the scope will be changed to the scope defined by the
     * {@link MessageScope.Instance scope instance}. At the end of the method the request
     * scope is returned to its original state.
     *
     * @param scopeInstance The request scope instance from which the request scope will
     *                      be initialized.
     * @param task          Task to be executed.
     * @param <T>           {@code task} result type.
     * @return result returned by the {@code task}
     */
    public <T> T runInScope(Instance scopeInstance, Producer<T> task) {
        Instance oldInstance = currentScopeInstance.get();
        try {
            currentScopeInstance.set(scopeInstance.getReference());
            return Errors.process(task);
        } finally {
            scopeInstance.release();
            currentScopeInstance.set(oldInstance);
        }
    }

    /**
     * Runs the {@link org.glassfish.jersey.internal.util.Producer task} in the new request scope. The
     * current thread might be already in any request scope and in that case the scope
     * will be changed to the scope defined by the {@link MessageScope.Instance scope
     * instance}. At the end of the method the request scope is returned to its original
     * state. The newly created {@link MessageScope.Instance scope instance} will be
     * implicitly released at the end of the method call except the task will call
     * {@link ameba.websocket.internal.MessageScope#suspendCurrent}.
     *
     * @param task Task to be executed.
     * @param <T>  {@code task} result type.
     * @return result returned by the {@code task}.
     */
    public <T> T runInScope(Producer<T> task) {
        Instance oldInstance = currentScopeInstance.get();
        Instance instance = createInstance();
        try {
            currentScopeInstance.set(instance);
            return Errors.process(task);
        } finally {
            instance.release();
            currentScopeInstance.set(oldInstance);
        }
    }

    /**
     * Implementation of the request scope instance.
     */
    public static final class Instance {
        /*
         * Scope instance UUID.
         *
         * For performance reasons, it's only generated if toString() method is invoked,
         * e.g. as part of some low-level logging.
         */

        private final LazyUid id = new LazyUid();
        /**
         * A map of injectable instances in this scope.
         */
        private final Map<ActiveDescriptor<?>, Object> store;
        /**
         * Holds the number of snapshots of this scope.
         */
        private final AtomicInteger referenceCounter;

        private Instance() {
            this.store = Maps.newHashMap();
            this.referenceCounter = new AtomicInteger(1);
        }

        /**
         * Get a "new" reference of the scope instance. This will increase
         * the internal reference counter which prevents the scope instance
         * to be destroyed until a {@link #release()} method is explicitly
         * called (once per each {@code getReference()} method call).
         *
         * @return referenced scope instance.
         */
        private Instance getReference() {
            referenceCounter.incrementAndGet();
            return this;
        }

        /**
         * Get an inhabitant stored in the scope instance that matches the active descriptor .
         *
         * @param <T>        inhabitant type.
         * @param descriptor inhabitant descriptor.
         * @return matched inhabitant stored in the scope instance or {@code null} if not matched.
         */
        @SuppressWarnings("unchecked")
        <T> T get(ActiveDescriptor<T> descriptor) {
            return (T) store.get(descriptor);
        }

        /**
         * Store a new inhabitant for the given descriptor.
         *
         * @param <T>        inhabitant type.
         * @param descriptor inhabitant descriptor.
         * @param value      inhabitant value.
         * @return old inhabitant previously stored for the given descriptor or
         * {@code null} if none stored.
         */
        @SuppressWarnings("unchecked")
        <T> T put(ActiveDescriptor<T> descriptor, T value) {
            Preconditions.checkState(!store.containsKey(descriptor),
                    "An instance for the descriptor %s was already seeded in this scope. Old instance: %s New instance: %s",
                    descriptor,
                    store.get(descriptor),
                    value);

            return (T) store.put(descriptor, value);
        }

        /**
         * Remove a value for the descriptor if present in the scope instance store.
         *
         * @param descriptor key for the value to be removed.
         */
        @SuppressWarnings("unchecked")
        <T> void remove(ActiveDescriptor<T> descriptor) {
            final T removed = (T) store.remove(descriptor);
            if (removed != null) {
                descriptor.dispose(removed);
            }
        }

        private <T> boolean contains(ActiveDescriptor<T> provider) {
            return store.containsKey(provider);
        }

        /**
         * Release a single reference to the current request scope instance.
         * <br>
         * Once all instance references are released, the instance will be recycled.
         */
        public void release() {
            if (referenceCounter.decrementAndGet() < 1) {
                try {
                    for (final ActiveDescriptor<?> descriptor : Sets.newHashSet(store.keySet())) {
                        remove(descriptor);
                    }
                } finally {
                    logger.debug("Released scope instance {}", this);
                }
            }
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).add("id", id.value()).add("referenceCounter", referenceCounter.get())
                    .add("store size", store.size()).toString();
        }
    }
}
