package ameba.mvc;

import ameba.mvc.template.internal.Viewables;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.message.internal.MessageBodyProviderNotFoundException;
import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;

/**
 * 错误处理页面配置
 *
 * @author icode
 */
@Provider
@Singleton
public class ErrorPageGenerator implements ExceptionMapper<Throwable> {
    protected static final HashMap<Integer, String> errorTemplateMap = Maps.newHashMap();
    // 模板引擎会去掉第一个斜线
    public static final String DEFAULT_ERROR_PAGE_DIR = "/error/";
    public static final String DEFAULT_404_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "404.httl";
    public static final String DEFAULT_5XX_PRODUCT_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "500.httl";
    public static final String DEFAULT_501_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "501.httl";
    public static final String DEFAULT_403_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "403.httl";
    public static final String DEFAULT_400_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "400.httl";
    public static final String DEFAULT_405_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "405.httl";
    public static final String DEFAULT_406_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "406.httl";
    public static final String DEFAULT_415_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "415.httl";
    private static final Logger logger = LoggerFactory.getLogger(ErrorPageGenerator.class);
    private static String defaultErrorTemplate;

    @Context
    protected javax.inject.Provider<ContainerRequestContext> requestProvider;

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

    protected int getStatus(Throwable exception) {
        int status = 500;
        if (exception instanceof InternalServerErrorException) {
            if (exception.getCause() instanceof MessageBodyProviderNotFoundException) {
                MessageBodyProviderNotFoundException e = (MessageBodyProviderNotFoundException) exception.getCause();
                if (e.getMessage().startsWith("MessageBodyReader")) {
                    status = 415;
                } else if (e.getMessage().startsWith("MessageBodyWriter")) {
                    status = 406;
                }
            }
        } else if (exception instanceof WebApplicationException) {
            status = ((WebApplicationException) exception).getResponse().getStatus();
        }
        return status;
    }

    @Override
    public Response toResponse(Throwable exception) {
        ContainerRequestContext request = requestProvider.get();
        int status = getStatus(exception);

        String tplName = errorTemplateMap.get(status);
        if (StringUtils.isBlank(tplName)) {
            if (StringUtils.isBlank(defaultErrorTemplate)) {
                if (status < 500) {
                    switch (status) {
                        case 401:
                        case 403:
                            tplName = DEFAULT_403_ERROR_PAGE;
                            break;
                        case 404:
                            tplName = DEFAULT_404_ERROR_PAGE;
                            break;
                        case 405:
                            tplName = DEFAULT_405_ERROR_PAGE;
                            break;
                        case 406:
                            tplName = DEFAULT_406_ERROR_PAGE;
                            break;
                        case 415:
                            tplName = DEFAULT_415_ERROR_PAGE;
                            break;
                        default:
                            tplName = DEFAULT_400_ERROR_PAGE;
                    }
                } else {
                    switch (status) {
                        case 501:
                            tplName = DEFAULT_501_ERROR_PAGE;
                            break;
                        default:
                            tplName = DEFAULT_5XX_PRODUCT_ERROR_PAGE;
                    }
                }
            } else {
                tplName = defaultErrorTemplate;
            }
        }
        Object viewable = createViewable(tplName, request, status, exception);
        if (status == 500)
            logger.error("服务器错误", exception);

        return Response.status(status).entity(viewable).build();
    }

    private Viewable createViewable(String tplName, ContainerRequestContext request,
                                    int status, Throwable exception) {
        Error error = new Error(
                request,
                status,
                exception);

        return Viewables.newDefaultViewable(tplName, error);
    }


    public static class Error {
        private int status;
        private ContainerRequestContext request;
        private Throwable exception;

        public Error() {
        }

        public Error(ContainerRequestContext request, int status, Throwable exception) {
            this.status = status;
            this.exception = exception;
            this.request = request;
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
    }
}