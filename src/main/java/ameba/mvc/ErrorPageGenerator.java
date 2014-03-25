package ameba.mvc;

import ameba.mvc.template.internal.HttlViewProcessor;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.server.DefaultErrorPageGenerator;
import org.glassfish.grizzly.http.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;

/**
 * 错误处理页面配置
 * Created by icode on 14-3-25.
 */
public abstract class ErrorPageGenerator extends DefaultErrorPageGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ErrorPageGenerator.class);
    private HashMap<Integer, String> errorTemplateMap;
    private String defaultErrorTemplate;

    public ErrorPageGenerator(HashMap<Integer, String> errorTemplateMap) {
        this.errorTemplateMap = errorTemplateMap;
    }

    public ErrorPageGenerator(HashMap<Integer, String> errorTemplateMap, String defaultErrorTemplate) {
        this.errorTemplateMap = errorTemplateMap;
        this.defaultErrorTemplate = defaultErrorTemplate;
    }

    public HashMap<Integer, String> getErrorTemplateMap() {
        return errorTemplateMap;
    }

    public void setErrorTemplateMap(HashMap<Integer, String> errorTemplateMap) {
        this.errorTemplateMap = errorTemplateMap;
    }

    public String getDefaultErrorTemplate() {
        return defaultErrorTemplate;
    }

    public void setDefaultErrorTemplate(String defaultErrorTemplate) {
        this.defaultErrorTemplate = defaultErrorTemplate;
    }


    @Inject
    private HttlViewProcessor templateProcessor;

    @Override
    public String generate(Request request, int status, String reasonPhrase, String description, Throwable exception) {
        String tplName = errorTemplateMap.get(status);
        if (StringUtils.isBlank(tplName)) {
            if (StringUtils.isBlank(defaultErrorTemplate)) {
                return super.generate(request, status, reasonPhrase, description, exception);
            } else {
                tplName = defaultErrorTemplate;
            }
        }

        return processTemplate(tplName, request, status, reasonPhrase, description, exception);
    }

    protected abstract String processTemplate(String tplName, Request request, int status, String reasonPhrase,
                                              String description, Throwable exception);

    public static class Error {
        public Request request;
        public int status;
        public String reasonPhrase;
        public String description;
        public Throwable exception;

        public Error(Request request, int status, String reasonPhrase, String description, Throwable exception) {
            this.request = request;
            this.status = status;
            this.reasonPhrase = reasonPhrase;
            this.description = description;
            this.exception = exception;
        }
    }
}
