package ameba.mvc;

import ameba.Application;
import ameba.mvc.template.internal.Viewables;
import com.google.common.collect.Maps;
import jersey.repackaged.com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.ResolvedViewable;
import org.glassfish.jersey.server.mvc.spi.TemplateProcessor;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Set;

/**
 * 错误处理页面配置
 * Created by icode on 14-3-25.
 */
@Provider
@Singleton
public abstract class ErrorPageGenerator implements ExceptionMapper<Throwable> {
    private static final HashMap<Integer, String> errorTemplateMap = Maps.newHashMap();
    private static final String DEFAULT_ERROR_PAGE_DIR = "/views/ameba/error/";
    private static final String DEFAULT_404_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "404.html";
    private static final String DEFAULT_5XX_DEV_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "dev_500.html";
    private static final String DEFAULT_5XX_PRODUCT_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "product_500.html";
    private static final String DEFAULT_501_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "product_501.html";
    private static final String DEFAULT_401_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "403.html";
    private static final String DEFAULT_400_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "400.html";
    private static String defaultErrorTemplate;
    @Inject
    private Application app;
    @Inject
    private ServiceLocator serviceLocator;
    @Context
    private javax.inject.Provider<ContainerRequestContext> requestProvider;

    static void pushErrorMap(int status, String tpl) {
        errorTemplateMap.put(status, tpl);
    }

    static void pushAllErrorMap(HashMap<Integer, String> map) {
        errorTemplateMap.putAll(map);
    }

    public static HashMap<Integer, String> getErrorTemplateMap() {
        return errorTemplateMap;
    }

    public static String getDefaultErrorTemplate() {
        return defaultErrorTemplate;
    }

    static void setDefaultErrorTemplate(String template) {
        defaultErrorTemplate = template;
    }

    /**
     * Get a {@link java.util.LinkedHashSet collection} of available template processors.
     *
     * @return set of template processors.
     */
    protected Set<TemplateProcessor> getTemplateProcessors() {
        final Set<TemplateProcessor> templateProcessors = Sets.newLinkedHashSet();

        templateProcessors.addAll(Providers.getCustomProviders(serviceLocator, TemplateProcessor.class));
        templateProcessors.addAll(Providers.getProviders(serviceLocator, TemplateProcessor.class));

        return templateProcessors;
    }

    @Override
    public Response toResponse(Throwable exception) {
        ContainerRequestContext request = requestProvider.get();
        int status = 500;
        if (exception instanceof WebApplicationException) {
            status = ((WebApplicationException) exception).getResponse().getStatus();
        }
        String tplName;
        boolean isDefaultTpl = false;
        if (status >= 500 && app.getMode().isDev()) {
            //开发模式，显示详细错误信息
            tplName = DEFAULT_5XX_DEV_ERROR_PAGE;
            isDefaultTpl = true;
        } else {
            tplName = errorTemplateMap.get(status);
            if (StringUtils.isBlank(tplName)) {
                isDefaultTpl = true;
                if (StringUtils.isBlank(defaultErrorTemplate)) {
                    switch (status) {
                        case 400:
                            tplName = DEFAULT_400_ERROR_PAGE;
                            break;
                        case 401:
                        case 403:
                            tplName = DEFAULT_401_ERROR_PAGE;
                            break;
                        case 404:
                            tplName = DEFAULT_404_ERROR_PAGE;
                            break;
                        case 501:
                            tplName = DEFAULT_501_ERROR_PAGE;
                            break;
                        default:
                            tplName = DEFAULT_5XX_PRODUCT_ERROR_PAGE;
                    }
                } else {
                    tplName = defaultErrorTemplate;
                }
            }
        }
        Error error = new Error(
                request,
                status,
                exception.getMessage(),
                StringUtils.join(exception.getStackTrace(), "\n"),
                exception);

        Object viewable = Viewables.newDefaultViewable(tplName, error);
        if (isDefaultTpl) {
            try {
                viewable = new ResolvedViewable<Object>(
                        getTemplateProcessor()
                        , getTemplate(tplName)
                        , (Viewable) viewable
                        , this.getClass()
                        , getMediaType());
            } catch (Exception e) {
                viewable = new ResolvedViewable<Object>(
                        getTemplateProcessor()
                        , getTemplate(app.getMode().isDev() ?
                        DEFAULT_5XX_DEV_ERROR_PAGE
                        : DEFAULT_5XX_PRODUCT_ERROR_PAGE)
                        , (Viewable) viewable
                        , this.getClass()
                        , getMediaType());
            }
        }

        return Response.status(status).entity(viewable).build();
    }

    protected abstract TemplateProcessor<Object> getTemplateProcessor();

    protected abstract Object getTemplate(String name);

    protected abstract MediaType getMediaType();


    public static class Error {
        public int status;
        public ContainerRequestContext request;
        public String reasonPhrase;
        public String description;
        public Throwable exception;

        public Error() {
        }

        public Error(ContainerRequestContext request, int status, String reasonPhrase, String description, Throwable exception) {
            this.status = status;
            this.reasonPhrase = reasonPhrase;
            this.description = description;
            this.exception = exception;
            this.request = request;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public ContainerRequestContext getRequest() {
            return request;
        }

        public void setRequest(ContainerRequestContext request) {
            this.request = request;
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }

        public void setReasonPhrase(String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Throwable getException() {
            return exception;
        }

        public void setException(Throwable exception) {
            this.exception = exception;
        }
    }
}
