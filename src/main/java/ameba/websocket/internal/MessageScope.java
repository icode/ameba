package ameba.websocket.internal;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>MessageScope class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@Singleton
public class MessageScope implements Context<MessageScoped> {

    private static Logger logger = LoggerFactory.getLogger(MessageScope.class);

    /**
     * A thread local copy of the current scope instance.
     */
    private final ThreadLocal<Instance> currentScopeInstance = new ThreadLocal<>();
    private volatile boolean isActive = true;

    @Override
    public Class<? extends Annotation> getScope() {
        return MessageScoped.class;
    }

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

    @Override
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        final Instance instance = current();
        return instance.contains(descriptor);
    }

    @Override
    public boolean supportsNullCreation() {
        return true;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        final Instance instance = current();
        instance.remove(descriptor);
    }

    @Override
    public void shutdown() {
        isActive = false;
    }

    /**
     * Get a new reference for to currently running Message scope instance. This call
     * prevents automatic {@link Instance#release() release} of the scope
     * instance once the task that runs in the scope has finished.
     * <p>
     * The returned scope instance may be used to run additional task(s) in the
     * same Message scope using one of the {@code #runInScope(Instance, ...)} methods.
     * </p>
     * <p>
     * Note that the returned instance must be {@link Instance#release()
     * released} manually once not needed anymore to prevent memory leaks.
     * </p>
     *
     * @return currently active {@link Instance Message scope instance}.
     * @throws IllegalStateException in case there is no active Message scope associated
     *                               with the current thread or if the Message scope has
     *                               been already shut down.
     * @see #suspendCurrent()
     */
    public Instance referenceCurrent() throws IllegalStateException {
        return current().getReference();
    }

    private Instance current() {
        Preconditions.checkState(isActive, "Message scope has been already shut down.");

        final Instance scopeInstance = currentScopeInstance.get();
        Preconditions.checkState(scopeInstance != null, "Not inside a Message scope.");

        return scopeInstance;
    }

    private Instance retrieveCurrent() {
        Preconditions.checkState(isActive, "Message scope has been already shut down.");
        return currentScopeInstance.get();
    }

    private void setCurrent(Instance instance) {
        Preconditions.checkState(isActive, "Message scope has been already shut down.");
        currentScopeInstance.set(instance);
    }

    private void resumeCurrent(Instance instance) {
        currentScopeInstance.set(instance);
    }

    /**
     * Get the current {@link Instance Message scope instance}
     * and mark it as suspended. This call prevents automatic
     * {@link Instance#release() release} of the scope instance
     * once the task that runs in the scope has finished.
     * <p>
     * The returned scope instance may be used to run additional task(s) in the
     * same Message scope using one of the {@code #runInScope(Instance, ...)} methods.
     * </p>
     * <p>
     * Note that the returned instance must be {@link Instance#release()
     * released} manually once not needed anymore to prevent memory leaks.
     * </p>
     *
     * @return currently active {@link Instance Message scope instance}
     * that was suspended or {@code null} if the thread is not currently running
     * in an active Message scope.
     * @see #referenceCurrent()
     */
    public Instance suspendCurrent() {
        final Instance scopeInstance = retrieveCurrent();
        if (scopeInstance == null) {
            return null;
        }
        try {
            return scopeInstance.getReference();
        } finally {
            logger.debug("Returned a new reference of the Message scope instance {0}", scopeInstance);
        }
    }

    /**
     * Creates a new instance of the {@link Instance Message scope instance}.
     * This instance can be then used to run task in the Message scope. Returned instance
     * is suspended by default and must therefore be closed explicitly as it is shown in
     * the following example:
     * <pre>
     * Instance instance = MessageScope.createInstance();
     * MessageScope.runInScope(instance, someRunnableTask);
     * instance.release();
     * </pre>
     *
     * @return New suspended Message scope instance.
     */
    public Instance createInstance() {
        return new Instance();
    }

    private <T> T _runInScope(Instance current, Instance oldInstance, Callable<T> task) throws Exception {
        try {
            setCurrent(current);
            return Errors.process(task);
        } finally {
            current.release();
            resumeCurrent(oldInstance);
        }
    }

    private void _runInScope(Instance current, Instance oldInstance, final Runnable task) {
        try {
            _runInScope(current, oldInstance, new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    task.run();
                    return null;
                }
            });
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Runs the {@link Runnable task} in the Message scope initialized from the
     * {@link Instance scope instance}. The {@link Instance
     * scope instance} is NOT released by the method (this must be done explicitly). The
     * current thread might be already in any Message scope and in that case the scope
     * will be changed to the scope defined by the {@link Instance scope
     * instance}. At the end of the method the Message scope is returned to its original
     * state.
     *
     * @param scopeInstance The Message scope instance from which the Message scope will
     *                      be initialized.
     * @param task          Task to be executed.
     */
    public void runInScope(Instance scopeInstance, Runnable task) {
        final Instance oldInstance = retrieveCurrent();
        _runInScope(scopeInstance.getReference(), oldInstance, task);
    }

    /**
     * Runs the {@link Runnable task} in the new Message scope. The current thread might
     * be already in any Message scope and in that case the scope will be changed to the
     * scope defined by the {@link Instance scope instance}. At the end of
     * the method the Message scope is returned to its original state. The newly created
     * {@link Instance scope instance} will be implicitly released at the end
     * of the method call except the task will call
     * {@link MessageScope#suspendCurrent}.
     *
     * @param task Task to be executed.
     */
    public void runInScope(Runnable task) {
        final Instance oldInstance = retrieveCurrent();
        final Instance instance = createInstance();
        _runInScope(instance, oldInstance, task);
    }

    /**
     * Runs the {@link Callable task} in the Message scope initialized from the
     * {@link Instance scope instance}. The {@link Instance
     * scope instance} is NOT released by the method (this must be done explicitly). The
     * current thread might be already in any Message scope and in that case the scope
     * will be changed to the scope defined by the {@link Instance scope
     * instance}. At the end of the method the Message scope is returned to its original
     * state.
     *
     * @param scopeInstance The Message scope instance from which the Message scope will
     *                      be initialized.
     * @param task          Task to be executed.
     * @param <T>           {@code task} result type.
     * @return result returned by the {@code task}.
     * @throws Exception Exception thrown by the {@code task}.
     */
    public <T> T runInScope(Instance scopeInstance, Callable<T> task) throws Exception {
        final Instance oldInstance = retrieveCurrent();
        try {
            return _runInScope(scopeInstance.getReference(), oldInstance, task);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Runs the {@link Callable task} in the new Message scope. The current thread might
     * be already in any Message scope and in that case the scope will be changed to the
     * scope defined by the {@link Instance scope instance}. At the end of
     * the method the Message scope is returned to its original state. The newly created
     * {@link Instance scope instance} will be implicitly released at the end
     * of the method call except the task will call
     * {@link MessageScope#suspendCurrent}.
     *
     * @param task Task to be executed.
     * @param <T>  {@code task} result type.
     * @return result returned by the {@code task}.
     * @throws Exception Exception thrown by the {@code task}.
     */
    public <T> T runInScope(Callable<T> task) throws Exception {
        final Instance oldInstance = retrieveCurrent();
        final Instance instance = createInstance();
        return _runInScope(instance, oldInstance, task);
    }

    /**
     * Runs the {@link org.glassfish.jersey.internal.util.Producer task} in the Message scope initialized
     * from the {@link Instance scope instance}.
     * The {@link Instance scope instance} is NOT released by the method (this
     * must be done explicitly). The current thread might be already in any Message scope
     * and in that case the scope will be changed to the scope defined by the
     * {@link Instance scope instance}. At the end of the method the message
     * scope is returned to its original state.
     *
     * @param scopeInstance The Message scope instance from which the Message scope will
     *                      be initialized.
     * @param task          Task to be executed.
     * @param <T>           {@code task} result type.
     * @return result returned by the {@code task}
     */
    public <T> T runInScope(Instance scopeInstance, Producer<T> task) {
        final Instance oldInstance = retrieveCurrent();
        try {
            return _runInScope(scopeInstance.getReference(), oldInstance, task);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Runs the {@link org.glassfish.jersey.internal.util.Producer task} in the new Message scope. The
     * current thread might be already in any Message scope and in that case the scope
     * will be changed to the scope defined by the {@link Instance scope
     * instance}. At the end of the method the Message scope is returned to its original
     * state. The newly created {@link Instance scope instance} will be
     * implicitly released at the end of the method call except the task will call
     * {@link MessageScope#suspendCurrent}.
     *
     * @param task Task to be executed.
     * @param <T>  {@code task} result type.
     * @return result returned by the {@code task}.
     */
    public <T> T runInScope(Producer<T> task) {
        final Instance oldInstance = retrieveCurrent();
        final Instance instance = createInstance();
        try {
            return _runInScope(instance, oldInstance, task);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Implementation of the Message scope instance.
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
            this.store = new HashMap<>();
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
         * Release a single reference to the current Message scope instance.
         * <p>
         * Once all instance references are released, the instance will be recycled.
         */
        public void release() {
            if (referenceCounter.decrementAndGet() < 1) {
                try {
                    for (final ActiveDescriptor<?> descriptor : Sets.newHashSet(store.keySet())) {
                        remove(descriptor);
                    }
                } finally {
                    logger.debug("Released scope instance {0}", this);
                }
            }
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("id", id.value()).add("referenceCounter", referenceCounter.get())
                    .add("store size", store.size()).toString();
        }
    }
}
