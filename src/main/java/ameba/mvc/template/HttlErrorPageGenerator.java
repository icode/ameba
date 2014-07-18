package ameba.mvc.template;

import ameba.mvc.ErrorPageGenerator;
import ameba.mvc.template.internal.HttlViewProcessor;
import httl.Template;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.TemplateProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * 错误处理页面配置
 * Created by icode on 14-3-25.
 */
public class HttlErrorPageGenerator extends ErrorPageGenerator {
    private static final Logger logger = LoggerFactory.getLogger(HttlErrorPageGenerator.class);

    private HttlViewProcessor httlViewProcessor;

    @Inject
    public HttlErrorPageGenerator(ServiceLocator serviceLocator) {
        super(serviceLocator);
    }

    @Override
    protected ErrorPageGenerator configure(ServiceLocator serviceLocator) {
        Set<TemplateProcessor> templateProcessors = Providers.getCustomProviders(serviceLocator, TemplateProcessor.class);
        for (TemplateProcessor tpl : templateProcessors) {
            if (tpl instanceof HttlViewProcessor) {
                httlViewProcessor = (HttlViewProcessor) tpl;
                return this;
            }
        }
        return this;
    }

    @Override
    protected String processTemplate(String tplName,
                                     MultivaluedMap<String, Object> httpHeaders,
                                     int status, String reasonPhrase,
                                     String description, Throwable exception) {

        Template template = httlViewProcessor.resolve(tplName, MediaType.TEXT_HTML_TYPE);
        Error error = new Error(status, reasonPhrase, description, exception);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            httlViewProcessor.writeTo(template, new Viewable(tplName, error), MediaType.TEXT_HTML_TYPE, httpHeaders, outputStream);
            return outputStream.toString();
        } catch (IOException e) {
            logger.error("template process error", e);
        }
        return null;
    }
}
