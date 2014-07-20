package ameba.mvc;

import ameba.Application;
import ameba.exceptions.AmebaException;
import ameba.exceptions.SourceAttachment;
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
import java.util.List;
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
    private static final String SER_ERR_MSG = "服务器发生错误！";
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
            //真正未被包装的程序抛出的错误
            if (status == 500)
                exception = exception.getCause();
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
        Object viewable = createViewble(tplName, request, status, exception);
        if (isDefaultTpl) {
            try {
                viewable = new ResolvedViewable<Object>(
                        getTemplateProcessor()
                        , getTemplate(tplName)
                        , (Viewable) viewable
                        , this.getClass()
                        , getMediaType());
            } catch (Exception e) {
                tplName = app.getMode().isDev() ?
                        DEFAULT_5XX_DEV_ERROR_PAGE
                        : DEFAULT_5XX_PRODUCT_ERROR_PAGE;
                viewable = new ResolvedViewable<Object>(
                        getTemplateProcessor()
                        , getTemplate(tplName)
                        , createViewble(tplName, request, 500, e)
                        , this.getClass()
                        , getMediaType());
            }
        }

        return Response.status(status).entity(viewable).build();
    }

    private Viewable createViewble(String tplName, ContainerRequestContext request,
                                   int status, Throwable exception) {
        Error error = new Error(
                request,
                status,
                exception);

        return Viewables.newDefaultViewable(tplName, error);
    }

    protected abstract TemplateProcessor<Object> getTemplateProcessor();

    protected abstract Object getTemplate(String name);

    protected abstract MediaType getMediaType();


    public static class Error implements SourceAttachment {
        private int status;
        private ContainerRequestContext request;
        private Throwable exception;
        private String sourceFile;
        private List<String> source;
        private int line;

        public Error() {
        }

        public Error(ContainerRequestContext request, int status, Throwable exception) {
            this.status = status;
            this.exception = exception;
            this.request = request;

            if (exception instanceof SourceAttachment) {
                SourceAttachment e = (SourceAttachment) exception;
                sourceFile = e.getSourceFile();
                source = e.getSource();
                line = e.getLineNumber();
            } else {
                AmebaException.getInterestingStackTraceElement(exception);
            }
        }

        public int getStatus() {
            return status;
        }

        public ContainerRequestContext getRequest() {
            return request;
        }

        public Throwable getException() {
            return exception;
        }

        public boolean isSourceAvailable() {
            return getSourceFile()!=null;
        }

        @Override
        public String getSourceFile() {
            return sourceFile;
        }

        @Override
        public List<String> getSource() {
            return source;
        }

        @Override
        public Integer getLineNumber() {
            return line;
        }
    }
}
