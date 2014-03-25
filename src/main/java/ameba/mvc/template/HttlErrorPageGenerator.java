package ameba.mvc.template;

import ameba.mvc.ErrorPageGenerator;
import ameba.mvc.template.internal.HttlViewProcessor;
import httl.Template;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * 错误处理页面配置
 * Created by icode on 14-3-25.
 */
public class HttlErrorPageGenerator extends ErrorPageGenerator {
    private static final Logger logger = LoggerFactory.getLogger(HttlErrorPageGenerator.class);

    public HttlErrorPageGenerator(HashMap<Integer, String> errorTemplateMap) {
        super(errorTemplateMap);
    }

    public HttlErrorPageGenerator(HashMap<Integer, String> errorTemplateMap, String defaultErrorTemplate) {
        super(errorTemplateMap, defaultErrorTemplate);
    }

    @Inject
    private HttlViewProcessor templateProcessor;

    @Override
    protected String processTemplate(String tplName, Request request, int status, String reasonPhrase,
                                     String description, Throwable exception) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Template template = templateProcessor.resolve(tplName, MediaType.TEXT_HTML_TYPE);
        Error error = new Error(request, status, reasonPhrase, description, exception);

        try {
            templateProcessor.writeTo(template, new Viewable(tplName, error), MediaType.TEXT_HTML_TYPE, null, outputStream);
        } catch (IOException e) {
            logger.error("template process error", e);
        }
        return outputStream.toString();
    }
}
