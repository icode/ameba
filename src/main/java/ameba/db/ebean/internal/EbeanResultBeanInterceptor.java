package ameba.db.ebean.internal;

import ameba.db.ebean.EbeanUtils;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;

/**
 * <p>EbeanResultBeanInterceptor class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@Priority(Integer.MAX_VALUE)
public class EbeanResultBeanInterceptor implements WriterInterceptor {
    /**
     * {@inheritDoc}
     */
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        Object entity = context.getEntity();
        EbeanUtils.disableLazyLoad(entity);
        context.proceed();
    }
}
