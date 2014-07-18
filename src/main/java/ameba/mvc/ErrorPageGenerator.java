package ameba.mvc;

import ameba.Application;
import ameba.mvc.template.internal.Viewables;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.HashMap;

/**
 * 错误处理页面配置
 * Created by icode on 14-3-25.
 */
@Singleton
public class ErrorPageGenerator implements ExtendedExceptionMapper<Throwable> {
    private static final HashMap<Integer, String> errorTemplateMap = Maps.newHashMap();
    private static String defaultErrorTemplate;

    private static ErrorPageGenerator instance;

    public static ErrorPageGenerator getInstance() {
        return instance;
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
    public boolean isMappable(Throwable exception) {
        return true;
    }

    @Inject
    private Provider<Request> requestProvider;

    @Inject
    private Provider<Response> responseProvider;

    @Override
    public Response toResponse(Throwable e) {

        Request request = requestProvider.get();
        Response response = responseProvider.get();
        int status = response.getStatus();
        String tplName = errorTemplateMap.get(status);
        if (StringUtils.isBlank(tplName)) {
            if (StringUtils.isBlank(defaultErrorTemplate)) {
                throw new RuntimeException(e);
            } else {
                tplName = defaultErrorTemplate;
            }
        }

        Error error = new Error(
                request,
                status,
                e.getMessage(),
                StringUtils.join(e.getStackTrace(), "\n"),
                e);

        return Response.fromResponse(response)
                .entity(Viewables.newDefaultViewable(tplName, error))
                .build();
    }

    private String getResponseEncoding() {
        return StringUtils.defaultIfBlank((String) app.getProperty("app.encoding"), "utf-8");
    }


    public static class Error {
        public int status;
        public Request request;
        public String reasonPhrase;
        public String description;
        public Throwable exception;

        public Error() {
        }

        public Error(Request request, int status, String reasonPhrase, String description, Throwable exception) {
            this.status = status;
            this.reasonPhrase = reasonPhrase;
            this.description = description;
            this.exception = exception;
            this.request = request;
        }
    }
}
