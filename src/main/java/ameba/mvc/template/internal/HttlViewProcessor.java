package ameba.mvc.template.internal;

import ameba.util.IOUtils;
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
import java.io.OutputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 淘宝HTTL模板处理器
 *
 * @author: ICode
 * @since: 13-8-6 下午7:57
 */
@Provider
@Singleton
public class HttlViewProcessor extends AmebaTemplateProcessor<Template> {

    public static final String CONFIG_SUFFIX = "httl";

    private Engine engine;

    @Inject
    public HttlViewProcessor(Configuration config, @Optional ServletContext servletContext) {
        super(config, servletContext, CONFIG_SUFFIX, getExtends(config));

        Properties properties = new Properties();
        Map<String, Object> map = config.getProperties();

        properties.put("template.suffix", StringUtils.join(getExtends(config)));

        String encoding = (String) map.get("app.encoding");

        if (StringUtils.isNotBlank(encoding)) {
            properties.put("input.encoding", encoding);
            properties.put("output.encoding", encoding);
            properties.put("message.encoding", encoding);
        }

        for (String key : map.keySet()) {
            if (key.startsWith("template.")) {
                String name;
                if (key.equals("template.suffix") || key.equals("template.directory") || key.equals("template.parser")) {
                    name = key;
                } else {
                    name = key.replaceFirst("^template\\.", "");
                }
                properties.put(name, map.get(key));
            }
        }
        this.engine = Engine.getEngine(properties);
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