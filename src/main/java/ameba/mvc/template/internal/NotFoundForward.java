package ameba.mvc.template.internal;

import groovy.lang.Singleton;
import jersey.repackaged.com.google.common.collect.Sets;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.server.mvc.spi.TemplateProcessor;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.util.Set;

/**
 * 404 跳转到模板
 *
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-27
 */
@Provider
@Singleton
public class NotFoundForward implements ExtendedExceptionMapper<NotFoundException> {

    Set<TemplateProcessor> templateProcessors;
    @Inject
    private javax.inject.Provider<UriInfo> uriInfo;
    @Inject
    private ServiceLocator serviceLocator;
    private ThreadLocal<String> templatePath = new ThreadLocal<String>();

    private Set<TemplateProcessor> getTemplateProcessors() {
        if (templateProcessors == null) {
            synchronized (this) {
                if (templateProcessors == null) {
                    templateProcessors = Sets.newLinkedHashSet();

                    templateProcessors.addAll(Providers.getCustomProviders(serviceLocator, TemplateProcessor.class));
                    templateProcessors.addAll(Providers.getProviders(serviceLocator, TemplateProcessor.class));
                }
            }
        }
        return templateProcessors;
    }

    @Override
    public Response toResponse(NotFoundException exception) {
        return Response.ok(Viewables.newDefaultViewable(templatePath.get())).build();
    }

    private String getCurrentPath() {
        return "/" + uriInfo.get().getPath();
    }

    @Override
    public boolean isMappable(NotFoundException exception) {
        String path = getCurrentPath();
        //受保护目录,不允许直接访问
        if (path.startsWith(Viewables.PROTECTED_DIR)) return false;
        try {
            for (TemplateProcessor templateProcessor : getTemplateProcessors()) {
                Object has = templateProcessor.resolve(path, null);
                if (has == null) {
                    path = path + "/index";
                    has = templateProcessor.resolve(path, null);
                }
                if (has != null) {
                    templatePath.set(path);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}