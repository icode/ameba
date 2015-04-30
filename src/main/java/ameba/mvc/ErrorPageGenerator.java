package ameba.mvc;

import ameba.core.Frameworks;
import ameba.message.ErrorMessage;
import ameba.mvc.template.internal.Viewables;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 错误处理页面配置
 *
 * @author icode
 */
@Singleton
public class ErrorPageGenerator implements MessageBodyWriter<ErrorMessage> {
    // 模板引擎会去掉第一个斜线
    /**
     * Constant <code>DEFAULT_ERROR_PAGE_DIR="/error/"</code>
     */
    public static final String DEFAULT_ERROR_PAGE_DIR = "/error/";
    /**
     * Constant <code>DEFAULT_404_ERROR_PAGE="DEFAULT_ERROR_PAGE_DIR + 404.httl"</code>
     */
    public static final String DEFAULT_404_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "404.httl";
    /**
     * Constant <code>DEFAULT_5XX_PRODUCT_ERROR_PAGE="DEFAULT_ERROR_PAGE_DIR + 500.httl"</code>
     */
    public static final String DEFAULT_5XX_PRODUCT_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "500.httl";
    /**
     * Constant <code>DEFAULT_501_ERROR_PAGE="DEFAULT_ERROR_PAGE_DIR + 501.httl"</code>
     */
    public static final String DEFAULT_501_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "501.httl";
    /**
     * Constant <code>DEFAULT_403_ERROR_PAGE="DEFAULT_ERROR_PAGE_DIR + 403.httl"</code>
     */
    public static final String DEFAULT_403_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "403.httl";
    /**
     * Constant <code>DEFAULT_400_ERROR_PAGE="DEFAULT_ERROR_PAGE_DIR + 400.httl"</code>
     */
    public static final String DEFAULT_400_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "400.httl";
    /**
     * Constant <code>DEFAULT_405_ERROR_PAGE="DEFAULT_ERROR_PAGE_DIR + 405.httl"</code>
     */
    public static final String DEFAULT_405_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "405.httl";
    /**
     * Constant <code>DEFAULT_406_ERROR_PAGE="DEFAULT_ERROR_PAGE_DIR + 406.httl"</code>
     */
    public static final String DEFAULT_406_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "406.httl";
    /**
     * Constant <code>DEFAULT_415_ERROR_PAGE="DEFAULT_ERROR_PAGE_DIR + 415.httl"</code>
     */
    public static final String DEFAULT_415_ERROR_PAGE = DEFAULT_ERROR_PAGE_DIR + "415.httl";
    /**
     * Constant <code>errorTemplateMap</code>
     */
    protected static final Map<Integer, String> errorTemplateMap = Maps.newHashMap();
    private static String defaultErrorTemplate;

    @Inject
    protected Provider<ContainerRequestContext> requestProvider;
    @Inject
    private Provider<MessageBodyWorkers> workers;

    static void pushErrorMap(int status, String tpl) {
        errorTemplateMap.put(status, tpl);
    }

    static void pushAllErrorMap(HashMap<Integer, String> map) {
        errorTemplateMap.putAll(map);
    }

    /**
     * <p>Getter for the field <code>errorTemplateMap</code>.</p>
     *
     * @return a {@link java.util.HashMap} object.
     */
    public static Map<Integer, String> getErrorTemplateMap() {
        return Collections.unmodifiableMap(errorTemplateMap);
    }

    /**
     * <p>Getter for the field <code>defaultErrorTemplate</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getDefaultErrorTemplate() {
        return defaultErrorTemplate;
    }

    static void setDefaultErrorTemplate(String template) {
        defaultErrorTemplate = template;
    }

    protected Viewable createViewable(String tplName, ContainerRequestContext request,
                                      int status, Throwable exception) {
        Error error = new Error(
                request,
                status,
                exception);

        return Viewables.newDefaultViewable(tplName, error);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ErrorMessage.class.isAssignableFrom(type)
                && (mediaType.getSubtype().equals("html")
                || mediaType.getSubtype().equals("xhtml+xml"));
    }

    @Override
    public long getSize(ErrorMessage errorMessage,
                        Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    protected String getErrorTemplate(int status) {
        return errorTemplateMap.get(status);
    }

    @Override
    public void writeTo(ErrorMessage errorMessage,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        ContainerRequestContext request = requestProvider.get();
        int status = errorMessage.getStatus();

        String tplName = getErrorTemplate(status);
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
        Viewable viewable = createViewable(tplName, request, status, errorMessage.getThrowable());
        writeViewable(viewable, mediaType, httpHeaders, entityStream);
    }

    protected void writeViewable(Viewable viewable,
                                 MediaType mediaType,
                                 MultivaluedMap<String, Object> httpHeaders,
                                 OutputStream entityStream) throws IOException {
        MessageBodyWriter<Viewable> writer = Frameworks.getViewableMessageBodyWriter(workers.get());
        if (writer != null) {
            writer.writeTo(viewable,
                    Viewable.class,
                    Viewable.class,
                    new Annotation[0],
                    mediaType,
                    httpHeaders,
                    entityStream);
        }
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
