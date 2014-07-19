package ameba.mvc.template;

import ameba.mvc.ErrorPageGenerator;
import ameba.mvc.template.internal.HttlViewProcessor;
import ameba.util.IOUtils;
import org.glassfish.jersey.server.mvc.spi.TemplateProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.text.ParseException;

/**
 * @author icode
 */
public class HttlErrorPageGenerator extends ErrorPageGenerator {

    private static final Logger logger = LoggerFactory.getLogger(HttlErrorPageGenerator.class);
    private HttlViewProcessor t;

    @Override
    @SuppressWarnings("unchecked")
    protected TemplateProcessor<Object> getTemplateProcessor() {
        for (TemplateProcessor t : getTemplateProcessors()) {
            if (t.getClass().equals(HttlViewProcessor.class)) {
                this.t = (HttlViewProcessor) t;
                return t;
            }
        }
        return null;
    }

    @Override
    protected Object getTemplate(String name) {
        try {
            String con = IOUtils.readFromResource(name);
            if (con == null) {
                logger.error("error page template not found {}", name);
                return null;
            }
            return t.parseTemplate(con);
        } catch (IOException e) {
            logger.error("get error page template error", e);
        } catch (ParseException e) {
            logger.error("parse error page template error", e);
        }
        return null;
    }

    @Override
    protected MediaType getMediaType() {
        return MediaType.TEXT_HTML_TYPE;
    }
}
