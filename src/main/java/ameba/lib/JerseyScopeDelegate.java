package ameba.lib;

import ameba.websocket.WebSocketException;
import org.glassfish.jersey.internal.util.Producer;
import org.glassfish.jersey.process.internal.RequestScope;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.security.PrivilegedAction;
import java.util.concurrent.Callable;

import static java.lang.invoke.MethodType.methodType;

/**
 * <p>JerseyScopeDelegate class.</p>
 *
 * @author icode
 *
 */
public class JerseyScopeDelegate {
    private static final MethodHandles.Lookup lookup = ((PrivilegedAction<MethodHandles.Lookup>) () -> {
        try {
            Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            field.setAccessible(true);
            return (MethodHandles.Lookup) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new WebSocketException(e);
        }
    }).run();
    private static final MethodHandle RETRIEVE_CURRENT_HANDLE = getMethodHandle(
            "retrieveCurrent",
            RequestScope.Instance.class
    );
    private static final MethodHandle SET_CURRENT_HANDLE = getMethodHandle(
            "setCurrent",
            void.class,
            RequestScope.Instance.class
    );
    private static final MethodHandle RESUME_CURRENT_HANDLE = getMethodHandle(
            "resumeCurrent",
            void.class,
            RequestScope.Instance.class
    );
    private RequestScope scope;


    /**
     * <p>Constructor for JerseyScopeDelegate.</p>
     *
     * @param scope a {@link org.glassfish.jersey.process.internal.RequestScope} object.
     */
    public JerseyScopeDelegate(RequestScope scope) {
        this.scope = scope;
    }

    private static MethodHandle getMethodHandle(String name, Class rtype, Class... args) {
        try {
            return lookup.findSpecial(
                    RequestScope.class,
                    name,
                    methodType(rtype, args),
                    RequestScope.class
            );
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new WebSocketException(e);
        }
    }

    /**
     * <p>createInstance.</p>
     *
     * @return a {@link org.glassfish.jersey.process.internal.RequestScope.Instance} object.
     */
    public RequestScope.Instance createInstance() {
        return scope.createInstance();
    }

    /**
     * <p>retrieveCurrent.</p>
     *
     * @return a {@link org.glassfish.jersey.process.internal.RequestScope.Instance} object.
     */
    public RequestScope.Instance retrieveCurrent() {
        try {
            return (RequestScope.Instance) RETRIEVE_CURRENT_HANDLE.invokeExact(this.scope);
        } catch (Throwable throwable) {
            throw new WebSocketException(throwable);
        }
    }

    /**
     * <p>setCurrent.</p>
     *
     * @param instance a {@link org.glassfish.jersey.process.internal.RequestScope.Instance} object.
     */
    public void setCurrent(RequestScope.Instance instance) {
        try {
            SET_CURRENT_HANDLE.invokeExact(this.scope, instance);
        } catch (Throwable throwable) {
            throw new WebSocketException(throwable);
        }
    }

    /**
     * <p>resumeCurrent.</p>
     *
     * @param oldInstance a {@link org.glassfish.jersey.process.internal.RequestScope.Instance} object.
     */
    public void resumeCurrent(RequestScope.Instance oldInstance) {
        try {
            RESUME_CURRENT_HANDLE.invokeExact(this.scope, oldInstance);
        } catch (Throwable throwable) {
            throw new WebSocketException(throwable);
        }
    }

    /**
     * <p>isActive.</p>
     *
     * @return a boolean.
     */
    public boolean isActive() {
        return scope.isActive();
    }

    /**
     * <p>referenceCurrent.</p>
     *
     * @return a {@link org.glassfish.jersey.process.internal.RequestScope.Instance} object.
     * @throws java.lang.IllegalStateException if any.
     */
    public RequestScope.Instance referenceCurrent() throws IllegalStateException {
        return scope.referenceCurrent();
    }

    public RequestScope.Instance current() {
        return scope.current();
    }

    /**
     * <p>suspendCurrent.</p>
     *
     * @return a {@link org.glassfish.jersey.process.internal.RequestScope.Instance} object.
     */
    public RequestScope.Instance suspendCurrent() {
        return scope.suspendCurrent();
    }

    /**
     * <p>runInScope.</p>
     *
     * @param scopeInstance a {@link org.glassfish.jersey.process.internal.RequestScope.Instance} object.
     * @param task a {@link java.lang.Runnable} object.
     */
    public void runInScope(RequestScope.Instance scopeInstance, Runnable task) {
        scope.runInScope(scopeInstance, task);
    }

    /**
     * <p>runInScope.</p>
     *
     * @param task a {@link java.lang.Runnable} object.
     */
    public void runInScope(Runnable task) {
        scope.runInScope(task);
    }

    /**
     * <p>runInScope.</p>
     *
     * @param scopeInstance a {@link org.glassfish.jersey.process.internal.RequestScope.Instance} object.
     * @param task a {@link java.util.concurrent.Callable} object.
     * @param <T> a T object.
     * @return a T object.
     * @throws java.lang.Exception if any.
     */
    public <T> T runInScope(RequestScope.Instance scopeInstance, Callable<T> task) throws Exception {
        return scope.runInScope(scopeInstance, task);
    }

    /**
     * <p>runInScope.</p>
     *
     * @param task a {@link java.util.concurrent.Callable} object.
     * @param <T> a T object.
     * @return a T object.
     * @throws java.lang.Exception if any.
     */
    public <T> T runInScope(Callable<T> task) throws Exception {
        return scope.runInScope(task);
    }

    /**
     * <p>runInScope.</p>
     *
     * @param scopeInstance a {@link org.glassfish.jersey.process.internal.RequestScope.Instance} object.
     * @param task a {@link org.glassfish.jersey.internal.util.Producer} object.
     * @param <T> a T object.
     * @return a T object.
     */
    public <T> T runInScope(RequestScope.Instance scopeInstance, Producer<T> task) {
        return scope.runInScope(scopeInstance, task);
    }

    /**
     * <p>runInScope.</p>
     *
     * @param task a {@link org.glassfish.jersey.internal.util.Producer} object.
     * @param <T> a T object.
     * @return a T object.
     */
    public <T> T runInScope(Producer<T> task) {
        return scope.runInScope(task);
    }

    /**
     * <p>enterScope.</p>
     *
     * @return a {@link org.glassfish.jersey.process.internal.RequestScope.Instance} object.
     */
    public RequestScope.Instance enterScope() {
        final RequestScope.Instance oldInstance = retrieveCurrent();
        final RequestScope.Instance instance = createInstance();
        setCurrent(instance);
        return oldInstance;
    }

    /**
     * <p>leaveScope.</p>
     *
     * @param oldInstance a {@link org.glassfish.jersey.process.internal.RequestScope.Instance} object.
     */
    public void leaveScope(RequestScope.Instance oldInstance) {
        retrieveCurrent().release();
        resumeCurrent(oldInstance);
    }

    /**
     * <p>shutdown.</p>
     */
    public void shutdown() {
        scope.shutdown();
    }
}
