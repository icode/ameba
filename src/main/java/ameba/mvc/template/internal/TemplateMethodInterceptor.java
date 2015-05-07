package ameba.mvc.template.internal;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Predicate;
import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.util.List;

/**
 * Intercepts resource methods that are annotated by {@link Template template annotation} and does not return {@link Viewable}
 * instances.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 * @author icode
 * @see org.glassfish.jersey.server.mvc.spi.ResolvedViewable
 * @see org.glassfish.jersey.server.mvc.internal.ViewableMessageBodyWriter
 */
@Priority(Priorities.ENTITY_CODER)
class TemplateMethodInterceptor implements WriterInterceptor {

    private static final String DEFAULT_DATA_VIEW_PAGE_DIR = "/_protected/default/";
    private static final String DEFAULT_DATA_VIEW = DEFAULT_DATA_VIEW_PAGE_DIR + "viewer";
    private static final List<MediaType> TEMPLATE_PRODUCES = Lists.newArrayList(
            MediaType.TEXT_HTML_TYPE,
            MediaType.APPLICATION_XHTML_XML_TYPE
    );

//    @Inject
//    private ContainerRequestContext request;

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

//    private boolean isNeedViewable() {
//        return -1 != ListUtils.indexOf(request.getAcceptableMediaTypes(), new Predicate<MediaType>() {
//            @Override
//            public boolean evaluate(MediaType mediaType) {
//                for (MediaType type : TEMPLATE_PRODUCES) {
//                    if (mediaType.getType().equalsIgnoreCase(type.getType())
//                            && mediaType.getSubtype().equalsIgnoreCase(type.getSubtype())) {
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });
//    }
}