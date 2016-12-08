package ameba.mvc.template.internal;

import ameba.exception.UnprocessableEntityException;
import groovy.lang.Singleton;
import jersey.repackaged.com.google.common.collect.Sets;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.server.mvc.spi.TemplateProcessor;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * 404 跳转到模板
 *
 * @author icode
 * @since 2013-08-27
 *
 */
@Singleton
@Priority(Priorities.ENTITY_CODER)
public class NotFoundForward implements ContainerResponseFilter {

    @Context
    private Provider<UriInfo> uriInfo;
    @Inject
    private ServiceLocator serviceLocator;

    private Set<TemplateProcessor> getTemplateProcessors() {
        Set<TemplateProcessor> templateProcessors = Sets.newLinkedHashSet();

        templateProcessors.addAll(Providers.getCustomProviders(serviceLocator, TemplateProcessor.class));
        templateProcessors.addAll(Providers.getProviders(serviceLocator, TemplateProcessor.class));
        return templateProcessors;
    }

    private String getCurrentPath() {
        return "/" + uriInfo.get().getPath();
    }

    /**
     * <p>mappedViewPath.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String mappedViewPath() {
        String path = getCurrentPath();
        //受保护目录,不允许直接访问
        String pDir = Viewables.PROTECTED_DIR + "/";
        if (path.startsWith(pDir)
                || path.startsWith("/" + pDir)) return null;
        for (TemplateProcessor templateProcessor : getTemplateProcessors()) {
            Object has = templateProcessor.resolve(path, MediaType.TEXT_HTML_TYPE);
            if (has == null) {
                path = path + (path.endsWith("/") ? "" : "/") + "index";
                has = templateProcessor.resolve(path, MediaType.TEXT_HTML_TYPE);
            }
            if (has != null) {
                return path;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        int status = responseContext.getStatus();
        if (status == 404 || status == UnprocessableEntityException.STATUS) {
            String path = mappedViewPath();
            if (path != null) {
                responseContext.setEntity(Viewables.newDefaultViewable(path),
                        new Annotation[0], MediaType.TEXT_HTML_TYPE);
                responseContext.setStatus(Response.Status.OK.getStatusCode());
            }
        }
    }
}
