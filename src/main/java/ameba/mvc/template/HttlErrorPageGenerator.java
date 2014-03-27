package ameba.mvc.template;

import ameba.mvc.ErrorPageGenerator;
import httl.Template;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 错误处理页面配置
 * Created by icode on 14-3-25.
 */
public class HttlErrorPageGenerator extends ErrorPageGenerator {
    private static final Logger logger = LoggerFactory.getLogger(HttlErrorPageGenerator.class);

    @Override
    protected String processTemplate(String tplName, Request request,
                                     MultivaluedMap<String, Object> httpHeaders,
                                     int status, String reasonPhrase,
                                     String description, Throwable exception) {

        Template template = (Template) getTemplateProcessor().resolve(tplName, MediaType.TEXT_HTML_TYPE);
        Error error = new Error(request, status, reasonPhrase, description, exception);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            getTemplateProcessor().writeTo(template, new Viewable(tplName, error), MediaType.TEXT_HTML_TYPE, httpHeaders, outputStream);
            return outputStream.toString();
        } catch (IOException e) {
            logger.error("template process error", e);
        }
        return null;
    }
}
