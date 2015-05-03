package ameba.mvc.template.httl;

import ameba.mvc.template.httl.internal.HttlClasspathLoader;
import ameba.mvc.template.httl.internal.HttlEngine;
import ameba.mvc.template.httl.internal.HttlViewProcessor;
import ameba.mvc.template.internal.NotFoundForward;
import ameba.mvc.template.internal.TemplateUtils;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import httl.Engine;
import httl.spi.methods.MessageMethod;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.nio.charset.Charset;
import java.util.*;

/**
 * <p>HttlMvcFeature class.</p>
 *
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
@ConstrainedTo(RuntimeType.SERVER)
public class HttlMvcFeature implements Feature {

    public static final String CONFIG_SUFFIX = "httl";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(final FeatureContext context) {
        if (!context.getConfiguration().isRegistered(NotFoundForward.class)) {
            context.register(NotFoundForward.class);
        }

        Properties properties = new Properties();
        Configuration config = context.getConfiguration();
        Map<String, Object> map = config.getProperties();

        String encoding = (String) map.get("app.encoding");

        if (StringUtils.isNotBlank(encoding)) {
            encoding = Charset.forName(encoding).name().toLowerCase();
            properties.put("input.encoding", encoding);
            properties.put("output.encoding", encoding);
            properties.put("message.encoding", encoding);
        }

        String keyPrefix = HttlViewProcessor.TEMPLATE_CONF_PREFIX + CONFIG_SUFFIX + ".";

        for (String key : map.keySet()) {
            if (key.startsWith(keyPrefix)) {
                String name = key.substring(keyPrefix.length());
                if (name.equals("parser")) {
                    name = "template.parser";
                } else if (name.startsWith("template.")) {
                    continue;
                }

                properties.put(name, map.get(key));
            }
        }

        String basePath = TemplateUtils.getBasePath(map, CONFIG_SUFFIX);
        Collection<String> basePaths = TemplateUtils.getBasePaths(basePath);

        properties.put("template.directory", StringUtils.join(basePaths, ","));

        String[] supportedExtensions = TemplateUtils.getExtends(config, CONFIG_SUFFIX,
                HttlViewProcessor.DEFAULT_TEMPLATE_EXTENSIONS);

        Set<String> exts = Sets.newHashSet(Collections2.transform(
                Arrays.asList(supportedExtensions), new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        input = input.toLowerCase();
                        return input.startsWith(".") ? input : "." + input;
                    }
                }));


        properties.put("template.suffix", StringUtils.join(exts, ","));
        properties.put("loaders", HttlClasspathLoader.class.getName());
        properties.put("engine", HttlEngine.class.getName());
        properties.put("import.methods-", MessageMethod.class.getName());
        properties.put("import.methods+", "ameba.mvc.template.httl.internal.MessageMethod");
        properties.put("localized", "false");

        final Engine engine = Engine.getEngine(properties);

        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(engine).to(Engine.class).proxy(false);
            }
        });

        context.register(HttlViewProcessor.class);
        return true;
    }
}
