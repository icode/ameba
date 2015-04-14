package ameba.db;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InterceptionService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 事务拦截器
 *
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-08
 */
public abstract class TransactionInterceptor implements MethodInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
        begin();
        try {
            // Invoke JAX-RS resource method.
            final Object result = methodInvocation.proceed();

            // Commit the transaction.
            commit();

            return result;
        } catch (final Throwable re) {
            // Something bad happened, rollback;
            rollback();

            // Rethrow the Exception.
            throw re;
        } finally {
            end();
        }
    }

    /**
     * <p>begin.</p>
     */
    protected abstract void begin();

    /**
     * <p>commit.</p>
     */
    protected abstract void commit();

    /**
     * <p>rollback.</p>
     */
    protected abstract void rollback();

    /**
     * <p>end.</p>
     */
    protected abstract void end();

    static class TransactionBinder extends AbstractBinder {
        @Override
        protected void configure() {
//            bind(MyInterceptionService.class)
//                    .to(org.glassfish.hk2.api.InterceptionService.class)
//                    .in(Singleton.class);
        }
    }

    static class c implements InterceptionService {

        @Override
        public Filter getDescriptorFilter() {
            return null;
        }

        @Override
        public List<MethodInterceptor> getMethodInterceptors(Method method) {
            return null;
        }

        @Override
        public List<ConstructorInterceptor> getConstructorInterceptors(Constructor<?> constructor) {
            return null;
        }
    }

}
