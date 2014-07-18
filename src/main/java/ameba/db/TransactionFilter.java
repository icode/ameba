package ameba.db;

import ameba.db.annotation.Transactional;
import org.glassfish.jersey.server.ContainerResponse;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * 事务拦截器
 *
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-08
 */
@Transactional
public abstract class TransactionFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        begin();
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (((ContainerResponse)responseContext).isMappedFromException()) {
            //if (Throwable.class.isInstance(responseContext.getEntity())) {
            rollback();
            // }
        } else {
            commit();
        }
        end();
    }

    protected abstract void begin();

    protected abstract void commit();

    protected abstract void rollback();

    protected abstract void end();

}