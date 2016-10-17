package ameba.lib;

import ameba.websocket.WebSocketException;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.jersey.internal.util.Producer;
import org.glassfish.jersey.process.internal.RequestScope;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.security.PrivilegedAction;
import java.util.concurrent.Callable;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author icode
 */
public class JerseyScopeDelegate {
    private static final MethodHandles.Lookup lookup = new PrivilegedAction<MethodHandles.Lookup>() {
        @Override
        public MethodHandles.Lookup run() {
            try {
                Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                field.setAccessible(true);
                return (MethodHandles.Lookup) field.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new WebSocketException(e);
            }
        }
    }.run();
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

    public RequestScope.Instance createInstance() {
        return scope.createInstance();
    }

    public RequestScope.Instance retrieveCurrent() {
        try {
            return (RequestScope.Instance) RETRIEVE_CURRENT_HANDLE.invokeExact(this.scope);
        } catch (Throwable throwable) {
            throw new WebSocketException(throwable);
        }
    }

    public void setCurrent(RequestScope.Instance instance) {
        try {
            SET_CURRENT_HANDLE.invokeExact(this.scope, instance);
        } catch (Throwable throwable) {
            throw new WebSocketException(throwable);
        }
    }

    public void resumeCurrent(RequestScope.Instance oldInstance) {
        try {
            RESUME_CURRENT_HANDLE.invokeExact(this.scope, oldInstance);
        } catch (Throwable throwable) {
            throw new WebSocketException(throwable);
        }
    }

    public Class<? extends Annotation> getScope() {
        return scope.getScope();
    }

    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor, ServiceHandle<?> root) {
        return scope.findOrCreate(activeDescriptor, root);
    }

    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        return scope.containsKey(descriptor);
    }

    public boolean supportsNullCreation() {
        return scope.supportsNullCreation();
    }

    public boolean isActive() {
        return scope.isActive();
    }

    public void destroyOne(ActiveDescriptor<?> descriptor) {
        scope.destroyOne(descriptor);
    }

    public RequestScope.Instance referenceCurrent() throws IllegalStateException {
        return scope.referenceCurrent();
    }

    public RequestScope.Instance suspendCurrent() {
        return scope.suspendCurrent();
    }

    public void runInScope(RequestScope.Instance scopeInstance, Runnable task) {
        scope.runInScope(scopeInstance, task);
    }

    public void runInScope(Runnable task) {
        scope.runInScope(task);
    }

    public <T> T runInScope(RequestScope.Instance scopeInstance, Callable<T> task) throws Exception {
        return scope.runInScope(scopeInstance, task);
    }

    public <T> T runInScope(Callable<T> task) throws Exception {
        return scope.runInScope(task);
    }

    public <T> T runInScope(RequestScope.Instance scopeInstance, Producer<T> task) {
        return scope.runInScope(scopeInstance, task);
    }

    public <T> T runInScope(Producer<T> task) {
        return scope.runInScope(task);
    }

    public RequestScope.Instance enterScope() {
        final RequestScope.Instance oldInstance = retrieveCurrent();
        final RequestScope.Instance instance = createInstance();
        setCurrent(instance);
        return oldInstance;
    }

    public void leaveScope(RequestScope.Instance oldInstance) {
        retrieveCurrent().release();
        resumeCurrent(oldInstance);
    }

    public void shutdown() {
        scope.shutdown();
    }
}
