package ameba.mvc.template.internal;

import ameba.mvc.template.HttlMvcFeature;
import ameba.mvc.template.TemplateException;
import ameba.mvc.template.TemplateNotFoundException;
import ameba.util.IOUtils;
import com.google.common.collect.Lists;
import httl.Engine;
import httl.Template;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.*;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 淘宝HTTL模板处理器
 *
 * @author ICode
 * @since 13-8-6 下午7:57
 */
@Singleton
public class HttlViewProcessor extends AmebaTemplateProcessor<Template> {

    /**
     * Constant <code>CONFIG_SUFFIX="httl"</code>
     */
    public static final String[] DEFAULT_TEMPLATE_EXTENSIONS = new String[]{".httl.html", ".httl"};

    private static String REQ_TPL_PATH_KEY = HttlViewProcessor.class.getName() + ".template.path";
    private static Logger logger = LoggerFactory.getLogger(HttlViewProcessor.class);
    @Inject
    private Engine engine;
    @Inject
    private Provider<ContainerRequest> request;

    /**
     * <p>Constructor for HttlViewProcessor.</p>
     *
     * @param config a {@link javax.ws.rs.core.Configuration} object.
     */
    @Inject
    public HttlViewProcessor(Configuration config) {
        super(config, HttlMvcFeature.CONFIG_SUFFIX,
                TemplateUtils.getExtends(config, HttlMvcFeature.CONFIG_SUFFIX, DEFAULT_TEMPLATE_EXTENSIONS));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TemplateException createException(Exception e, Template template) {
        TemplateException ecx;
        if (e instanceof ParseException) {
            List<String> msgSource = Lists.newArrayList(e.getMessage().split("\n"));
            File file = getTemplateFile(template);
            List<String> source = Lists.newArrayList();
            String sourceString = getTemplateSource(template);

            if (StringUtils.isNotBlank(sourceString)) {
                Collections.addAll(source, sourceString.split("\n"));
            }
            int line = -1;
            int lineIndex = -1;
            try {
                String[] positionInfo = msgSource.get(1).split(",");
                lineIndex = Integer.valueOf(positionInfo[0].split(":")[1].trim()) - 1;
                line = Integer.valueOf(positionInfo[1].split(":")[1].trim()) - 1;
            } catch (Exception ex) {
                // no op
            }
            ecx = new TemplateException(msgSource.get(0)
                    + "\n" + msgSource.get(1).replace(", in:", ""), e, line, file, source, lineIndex);
        } else if (template != null) {
            List<String> sources;

            String source = getTemplateSource(template);

            if (StringUtils.isNotBlank(source)) {
                sources = Lists.newArrayList(source.split("\n"));
            } else {
                sources = Lists.newArrayList();
            }

            File tFile = getTemplateFile(template);

            if (e instanceof FileNotFoundException || e.getCause() instanceof FileNotFoundException) {

                int line = -1;
                int lineIndex = -1;
                String fileName = e.getMessage().replace("Not found template ", "");
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf(" in"));
                for (String s : sources) {
                    line++;
                    lineIndex = s.indexOf(fileName);
                    if (lineIndex > -1 && s.contains("${") && s.contains("}")) {
                        lineIndex++;
                        break;
                    }
                }
                if (lineIndex == -1) {
                    line = -1;
                }

                ecx = new TemplateNotFoundException(e.getMessage(),
                        e, line, tFile, sources, lineIndex);
            } else {
                ecx = new TemplateException("Write template error in  " + tFile.getPath() + ". " + e.getMessage(),
                        e, -1, tFile, sources, -1);
            }
        } else {
            ecx = new TemplateException("template error", e, e.getStackTrace()[0].getLineNumber());
        }
        return ecx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Template resolve(String templatePath, Reader reader) throws Exception {
        Template template = null;
        if (templatePath != null) {
            request.get().setProperty(REQ_TPL_PATH_KEY, templatePath);
            try {
                template = resolve(templatePath);
            } catch (Exception e) {
                if (reader != null) {
                    template = resolve(reader);
                } else {
                    throw e;
                }
            }
        } else if (reader != null) {
            template = resolve(reader);
        }

        return template;
    }

    private String getTemplateSource(Template template) {
        if (template != null) {
            try {
                return template.getSource();
            } catch (IOException e) {
                logger.error("get template source code error", e);
            }
        } else {
            try {
                return IOUtils.readFromResource((String) request.get().getProperty(REQ_TPL_PATH_KEY));
            } catch (IOException e) {
                logger.error("read template file error", e);
            }
        }
        return "";
    }

    private File getTemplateFile(Template template) {
        return new File((String) request.get().getProperty(REQ_TPL_PATH_KEY));
    }

    private Template resolve(String templatePath) throws Exception {
        return engine.getTemplate(templatePath, getEncoding().name().toLowerCase());
    }

    private Template resolve(Reader reader) throws Exception {
        String content = IOUtils.read(reader);
        return engine.parseTemplate(content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeTemplate(Template template, final Viewable viewable, MediaType mediaType,
                              MultivaluedMap<String, Object> httpHeaders, OutputStream outputStream) throws Exception {
        Object model = viewable.getModel();
        if (!(model instanceof Map)) {
            model = new HashMap<String, Object>() {{
                put("model", viewable.getModel());
            }};
        }
        if (httpHeaders != null)
            setContentType(mediaType.equals(MediaType.WILDCARD_TYPE)
                    ? MediaType.TEXT_HTML_TYPE : mediaType, httpHeaders);
        template.render(model, outputStream);
    }
}
