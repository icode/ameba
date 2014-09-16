package ameba.mvc.template.internal;

import ameba.Ameba;
import ameba.mvc.template.TemplateException;
import ameba.util.IOUtils;
import com.google.common.collect.Lists;
import httl.Engine;
import httl.Template;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.mvc.Viewable;
import org.jvnet.hk2.annotations.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.File;
import java.io.OutputStream;
import java.io.Reader;
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
@Provider
@Singleton
public class HttlViewProcessor extends AmebaTemplateProcessor<Template> {

    public static final String CONFIG_SUFFIX = "httl";
    private static final String TEMPLATE_CONF_PREFIX = "template.";

    private static final Engine engine;

    static {
        Properties properties = new Properties();
        Map<String, Object> map = Ameba.getApp().getProperties();

        properties.put("template.suffix", StringUtils.join(getExtends(Ameba.getApp())));

        String encoding = (String) map.get("app.encoding");

        if (StringUtils.isNotBlank(encoding)) {
            properties.put("input.encoding", encoding);
            properties.put("output.encoding", encoding);
            properties.put("message.encoding", encoding);
        }

        for (String key : map.keySet()) {
            if (key.startsWith(TEMPLATE_CONF_PREFIX)) {
                String name;
                if (key.equals("template.suffix") || key.equals("template.directory") || key.equals("template.parser")) {
                    name = key;
                } else {
                    name = key.substring(TEMPLATE_CONF_PREFIX.length());
                }
                properties.put(name, map.get(key));
            }
        }
        engine = Engine.getEngine(properties);
    }


    @Inject
    public HttlViewProcessor(Configuration config, @Optional ServletContext servletContext) {
        super(config, servletContext, CONFIG_SUFFIX, getExtends(config));
    }

    static String[] getExtends(Configuration config) {
        Map<String, Object> map = config.getProperties();
        String extension = (String) map.get("template.suffix");

        if (StringUtils.isBlank(extension)) {
            return new String[]{".html"};
        }
        return extension.split(",");
    }

    @Override
    protected TemplateException createException(ParseException e) {
        List<String> msgSource = Lists.newArrayList(e.getMessage().split("\n"));
        File file = new File(getBasePath() + msgSource.get(2));
        List<String> source = Lists.newArrayList();
        source.add(msgSource.get(4));
        source.add(msgSource.get(5));
        Integer line;
        try {
            line = Integer.valueOf(msgSource.get(1).split(",")[1].split(":")[1].trim());
        } catch (Exception ex) {
            line = 0;
        }
        return new TemplateException(msgSource.get(0) + "\n" + msgSource.get(1).replace(", in:", ""), e, line, file, source, 0);
    }

    @Override
    protected Template resolve(String templatePath) throws Exception {
        String dir = (String) engine.getProperty("template.directory");
        if (templatePath.startsWith(dir)) {
            templatePath = templatePath.substring(dir.length());
        }
        return engine.getTemplate(templatePath);
    }

    @Override
    protected Template resolve(Reader reader) throws Exception {
        String content = IOUtils.read(reader);
        return engine.parseTemplate(content);
    }

    @Override
    public String getTemplateFile(Template templateReference) {
        return templateReference.getName();
    }

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
            setContentType(mediaType.equals(MediaType.WILDCARD_TYPE) ? MediaType.TEXT_HTML_TYPE : mediaType, httpHeaders);
        template.render(model, outputStream);
    }
}