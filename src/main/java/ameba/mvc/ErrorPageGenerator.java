package ameba.mvc;

import ameba.Application;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.server.DefaultErrorPageGenerator;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.HashMap;

/**
 * 错误处理页面配置
 * Created by icode on 14-3-25.
 */
@Singleton
public abstract class ErrorPageGenerator extends DefaultErrorPageGenerator implements ExceptionMapper<Exception> {
    private static final HashMap<Integer, String> errorTemplateMap = Maps.newHashMap();
    private static String defaultErrorTemplate;

    private static ErrorPageGenerator instance;

    public static ErrorPageGenerator getInstance() {
        return instance;
    }

    protected abstract ErrorPageGenerator configure(ServiceLocator serviceLocator);

    protected ErrorPageGenerator(ServiceLocator serviceLocator) {
        instance = configure(serviceLocator);
    }

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

    @Inject
    private Application app;


    @Override
    public Response toResponse(Exception e) {
        int status = 500;
        Request request = Request.create();
        Response response = null;
        Response.ResponseBuilder builder;
        if (e instanceof WebApplicationException) {
            WebApplicationException ex = (WebApplicationException) e;
            response = ex.getResponse();
            status = response.getStatus();
        }

        String cont = generate(request,
                response,
                status,
                e.getMessage(),
                StringUtils.join(e.getStackTrace(), "\n"),
                e
        );

        if (response != null) {
            builder = Response.fromResponse(response);
        } else {
            builder = Response.status(status)
                    .type(MediaType.TEXT_HTML_TYPE)
                    .encoding(getResponseEncoding());
        }

        return builder.entity(cont).build();
    }

    private String getResponseEncoding() {
        return StringUtils.defaultIfBlank((String) app.getProperty("app.encoding"), "utf-8");
    }

    @Override
    public String generate(Request request, int status, String reasonPhrase, String description, Throwable exception) {
        if (request != null) {
            request.getResponse().setCharacterEncoding(getResponseEncoding());
        }
        return generate(request, null, status, reasonPhrase, description, exception);
    }

    public String generate(Request request, Response response, int status, String reasonPhrase, String description, Throwable exception) {
        String tplName = errorTemplateMap.get(status);
        if (StringUtils.isBlank(tplName)) {
            if (StringUtils.isBlank(defaultErrorTemplate)) {
                return super.generate(request, status, reasonPhrase, description, exception);
            } else {
                tplName = defaultErrorTemplate;
            }
        }

        return processTemplate(tplName,
                (response != null ? response.getHeaders() : new MultivaluedHashMap<String, Object>()),
                status, reasonPhrase, description, exception);
    }

    protected abstract String processTemplate(String tplName,
                                              MultivaluedMap<String, Object> httpHeaders,
                                              int status, String reasonPhrase,
                                              String description, Throwable exception);

    public static class Error {
        public int status;
        public String reasonPhrase;
        public String description;
        public Throwable exception;

        public Error(int status, String reasonPhrase, String description, Throwable exception) {
            this.status = status;
            this.reasonPhrase = reasonPhrase;
            this.description = description;
            this.exception = exception;
        }
    }
}
