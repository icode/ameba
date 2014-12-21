package ameba.mvc.template.internal;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.mvc.MvcFeature;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.Map;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
public class AmebaMvcFeature implements Feature {

    private static final String TPL_ENGINE_DIR_PR = "template.directory.engine.";
    private static final String TPL_DIR = "template.directory";
    private static final String TPL_MODULE_DIR_PR = "template.directory.module.";


    @Override
    public boolean configure(final FeatureContext context) {
        if (!context.getConfiguration().isRegistered(MvcFeature.class)) {
            context.register(MvcFeature.class);
        }

        Map<String, String> tempConf = Maps.newHashMap();

        for (String key : context.getConfiguration().getPropertyNames()) {
            if (key.startsWith(TPL_ENGINE_DIR_PR)) {//模板引擎默认路径
                String engine = key.replaceFirst("template\\.directory\\.engine", "");
                String confKey = MvcFeature.TEMPLATE_BASE_PATH + engine;
                String value = (String) context.getConfiguration().getProperty(confKey);
                String append = (String) context.getConfiguration().getProperty(key);
                value = getTplDirConf(value, engine.substring(1), context, tempConf);
                if (StringUtils.isBlank(value)) {
                    value = append;
                } else {
                    value += "," + append;
                }
                tempConf.put(confKey, value);
            } else if (key.startsWith(TPL_MODULE_DIR_PR)) {//模块自定义模板路径
                String confKey = key.replaceFirst("template\\.directory\\.module\\.", "");
                int i = confKey.indexOf(".");
                if (i != -1) {
                    String engine = confKey.substring(0, i);
                    confKey = MvcFeature.TEMPLATE_BASE_PATH + "." + engine;
                    String value = (String) context.getConfiguration().getProperty(confKey);
                    String append = (String) context.getConfiguration().getProperty(key);
                    value = getTplDirConf(value, engine, context, tempConf);
                    if (StringUtils.isBlank(value)) {
                        value = append;
                    } else {
                        value += "," + append;
                    }
                    tempConf.put(confKey, value);
                }
            } else if (key.startsWith("template.cache.")) {
                tempConf.put(MvcFeature.CACHE_TEMPLATES + key.replaceFirst("template\\.cache", ""),
                        (String) context.getConfiguration().getProperty(key));
            }
        }

        for (String key : tempConf.keySet()) {
            context.property(key, tempConf.get(key));
        }

        context.property(MvcFeature.TEMPLATE_BASE_PATH,
                context.getConfiguration().getProperty(TPL_DIR));

        context.property(MvcFeature.CACHE_TEMPLATES,
                context.getConfiguration().getProperty("template.cache"));

        return true;
    }

    private String getTplDirConf(String value, String engine, FeatureContext context, Map<String, String> tempConf) {
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
}