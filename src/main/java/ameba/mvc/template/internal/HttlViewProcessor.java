package ameba.mvc.template.internal;

import ameba.core.Application;
import ameba.mvc.template.TemplateException;
import ameba.mvc.template.TemplateNotFoundException;
import ameba.util.IOUtils;
import com.google.common.collect.Lists;
import httl.Engine;
import httl.Template;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.inject.Provider;
import java.io.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
    public static final String CONFIG_SUFFIX = "httl";
    private static final String TEMPLATE_CONF_PREFIX = "template.";
    private static Engine engine;
    private static String REQ_TPL_PATH_KEY = HttlViewProcessor.class.getName() + ".template.path";
    private static Logger logger = LoggerFactory.getLogger(HttlViewProcessor.class);
    @Inject
    private Provider<ContainerRequest> request;

    /**
     * <p>Constructor for HttlViewProcessor.</p>
     *
     * @param config a {@link javax.ws.rs.core.Configuration} object.
     */
    @Inject
    public HttlViewProcessor(Configuration config) {
        super(config, CONFIG_SUFFIX, getExtends(config));
    }

    static String[] getExtends(Configuration config) {
        Map<String, Object> map = config.getProperties();
        String extension = (String) map.get("template.suffix");
        extension = StringUtils.deleteWhitespace(extension);

        if (StringUtils.isEmpty(extension)) {
            return new String[]{".httl.html", ".httl"};
        } else {
            extension = extension.toLowerCase();
        }
        String[] extensions = extension.split(",");
        if (!ArrayUtils.contains(extensions, "httl") && !ArrayUtils.contains(extensions, ".httl")) {
            extensions = ArrayUtils.add(extensions, "httl");
        }
        if (!ArrayUtils.contains(extensions, "httl.html")
                && !ArrayUtils.contains(extensions, ".httl.html")) {
            extensions = ArrayUtils.add(extensions, ".httl.html");
        }
        return extensions;
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
            source.add(msgSource.get(4));
            source.add(msgSource.get(5));
            Integer line;
            try {
                line = Integer.valueOf(msgSource.get(1).split(",")[1].split(":")[1].trim());
            } catch (Exception ex) {
                line = 0;
            }
            ecx = new TemplateException(msgSource.get(0) + "\n" + msgSource.get(1).replace(", in:", ""), e, line, file, source, 0);
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
                ecx = new TemplateNotFoundException(e.getMessage(),
                        e, -1, tFile, sources, -1);
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

        if (template != null) {
            request.get().setProperty(REQ_TPL_PATH_KEY, templatePath);
        }

        return template;
    }

    private String getTemplateSource(Template template) {
        try {
            return template.getSource();
        } catch (IOException e) {
            logger.error("get template source code error", e);
        }
        return "";
    }

    private File getTemplateFile(Template template) {
        return new File((String) request.get().getProperty(REQ_TPL_PATH_KEY));
    }

    private Template resolve(String templatePath) throws Exception {
        String dir = (String) engine.getProperty("template.directory");
        if (templatePath.startsWith(dir)) {
            templatePath = templatePath.substring(dir.length());
        }
        return engine.getTemplate(templatePath);
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

    public static class AddOn extends ameba.core.AddOn {
        @Override
        public void done(Application application) {
            Properties properties = new Properties();
            Map<String, Object> map = application.getProperties();

            properties.put("template.suffix", StringUtils.join(getExtends(application.getConfig())));

            String encoding = (String) map.get("app.encoding");

            if (StringUtils.isNotBlank(encoding)) {
                properties.put("input.encoding", encoding);
                properties.put("output.encoding", encoding);
                properties.put("message.encoding", encoding);
            }

            for (String key : map.keySet()) {
                if (key.startsWith(TEMPLATE_CONF_PREFIX)) {
                    String name;
                    if (key.equals("template.suffix")
                            || key.equals("template.directory")
                            || key.equals("template.parser")) {
                        name = key;
                    } else {
                        name = key.substring(TEMPLATE_CONF_PREFIX.length());
                    }
                    properties.put(name, map.get(key));
                }
            }
            engine = Engine.getEngine(properties);
        }
    }
}
