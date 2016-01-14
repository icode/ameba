package ameba.mvc.template.httl;

import ameba.mvc.template.httl.internal.HttlClasspathLoader;
import ameba.mvc.template.httl.internal.HttlViewProcessor;
import ameba.mvc.template.internal.NotFoundForward;
import ameba.mvc.template.internal.TemplateHelper;
import ameba.util.LinkedProperties;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import httl.Engine;
import httl.util.BeanFactory;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
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
    private static final Properties templateProperties = new Properties();

    public static Properties getTemplateProperties() {
        Properties properties = new LinkedProperties();
        properties.putAll(templateProperties);
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(final FeatureContext context) {
        if (!context.getConfiguration().isRegistered(NotFoundForward.class)) {
            context.register(NotFoundForward.class);
        }

        Configuration config = context.getConfiguration();
        Map<String, Object> map = config.getProperties();

        String encoding = TemplateHelper.getTemplateOutputEncoding(config, "." + CONFIG_SUFFIX).name().toLowerCase();

        templateProperties.put("input.encoding", encoding);
        templateProperties.put("output.encoding", encoding);

        String keyPrefix = HttlViewProcessor.TEMPLATE_CONF_PREFIX + CONFIG_SUFFIX + ".";

        for (String key : map.keySet()) {
            if (key.startsWith(keyPrefix)) {
                String name = key.substring(keyPrefix.length());
                if (name.equals("parser")) {
                    name = "template.parser";
                } else if (name.startsWith("template.")) {
                    continue;
                }
                // 累加ameba配置值，并累加到httl-default上，${name}++ 会变为${name}+
                if (name.endsWith("s")) {
                    if (!name.endsWith("+")) {
                        name = name.concat("+");
                    }
                }
                templateProperties.put(name, map.get(key));
            }
        }

        String basePath = TemplateHelper.getBasePath(map, CONFIG_SUFFIX);
        Collection<String> basePaths = TemplateHelper.getBasePaths(basePath);

        templateProperties.put("template.directory", StringUtils.join(basePaths, ","));

        String[] supportedExtensions = TemplateHelper.getExtends(config, CONFIG_SUFFIX,
                HttlViewProcessor.DEFAULT_TEMPLATE_EXTENSIONS);

        Set<String> exts = Sets.newHashSet(Collections2.transform(
                Arrays.asList(supportedExtensions), new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        input = input.toLowerCase();
                        return input.startsWith(".") ? input : "." + input;
                    }
                }));


        templateProperties.put("template.suffix", StringUtils.join(exts, ","));
        templateProperties.put("loaders", HttlClasspathLoader.class.getName());
//        templateProperties.put("engine", HttlEngine.class.getName());
        templateProperties.put("localized", "false");
        templateProperties.put("output.writer", "false");
        templateProperties.put("output.stream", "true");
        templateProperties.put("import.methods-", "httl.spi.methods.MessageMethod");

        final Engine engine = BeanFactory.createBean(Engine.class,
                HttlUtil.initProperties("default", templateProperties));

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
