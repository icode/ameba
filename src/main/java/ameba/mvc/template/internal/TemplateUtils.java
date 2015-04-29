package ameba.mvc.template.internal;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.server.mvc.MvcFeature;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;
import java.util.Collection;
import java.util.Map;

/**
 * @author icode
 */
public class TemplateUtils {
    public static final String TPL_ENGINE_DIR_PR = "template.directory.engine.";
    public static final String TPL_DIR = "template.directory";

    private TemplateUtils() {
    }

    public static Collection<String> getBasePaths(String basePath) {
        return Collections2.transform(Lists.newArrayList(basePath.split(",")),
                new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return s.startsWith("/") ? s.substring(1) : s;
                    }
                });
    }

    public static String[] getExtends(Configuration config, String module, String[] defaultExtensions) {
        String extension = getStringExtends(config, module);

        if (StringUtils.isBlank(extension)) {
            return defaultExtensions;
        } else {
            extension = extension.toLowerCase();
        }
        String[] extensions = extension.split(",");
        for (String ext : defaultExtensions) {
            if (!ArrayUtils.contains(extensions, ext.substring(1))
                    && !ArrayUtils.contains(extensions, ext)) {
                extensions = ArrayUtils.add(extensions, ext);
            }
        }
        return extensions;
    }

    public static String getStringExtends(Configuration config, String module) {
        Map<String, Object> map = config.getProperties();
        String extension = (String) map.get(AmebaTemplateProcessor.TEMPLATE_CONF_PREFIX + module + ".suffix");
        if (StringUtils.isNotBlank(extension)) {
            extension = StringUtils.deleteWhitespace(extension);
        }

        if (StringUtils.isBlank(extension)) {
            extension = (String) map.get(AmebaTemplateProcessor.TEMPLATE_CONF_PREFIX + "suffix");
            if (StringUtils.isNotBlank(extension)) {
                extension = StringUtils.deleteWhitespace(extension);
            }
        }

        return StringUtils.isBlank(extension) ? null : extension.toLowerCase();
    }


    public static String getTemplateEngineDirConfig(String value,
                                                    String engine,
                                                    FeatureContext context,
                                                    Map<String, String> tempConf) {
        if (StringUtils.isBlank(value)) {
            value = tempConf.get(TPL_ENGINE_DIR_PR + engine);
        }

        if (StringUtils.isBlank(value)) {
            value = (String) context.getConfiguration().getProperty(TPL_ENGINE_DIR_PR + engine);
        }

        if (StringUtils.isBlank(value)) {
            value = (String) context.getConfiguration().getProperty(TPL_DIR);
        }
        return value;
    }

    public static String getBasePath(Map<String, Object> properties, String cfgSuffix) {
        String basePath = PropertiesHelper.getValue(properties,
                MvcFeature.TEMPLATE_BASE_PATH + "." + cfgSuffix, String.class, null);
        if (basePath == null) {
            basePath = PropertiesHelper.getValue(properties, MvcFeature.TEMPLATE_BASE_PATH, "", null);
        }
        return basePath;
    }
}
