package ameba.mvc.template.internal;

import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;

/**
 * Intercepts resource methods that are annotated by {@link Template template annotation} and does not return {@link Viewable}
 * instances.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 * @author icode
 * @see org.glassfish.jersey.server.mvc.spi.ResolvedViewable
 * @see org.glassfish.jersey.server.mvc.internal.ViewableMessageBodyWriter
 */
@Singleton
@Priority(Priorities.ENTITY_CODER)
class TemplateMethodInterceptor implements WriterInterceptor {

    @Override
    public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
        final Object entity = context.getEntity();

        if (!(entity instanceof Viewable)) {
            final Template template = TemplateHelper.getTemplateAnnotation(context.getAnnotations());
            if (template != null) {
                context.setType(Viewable.class);
                context.setEntity(new Viewable(template.name(), entity));
            }
        }

        context.proceed();
    }

}