package ameba.mvc;

import ameba.exception.ConfigErrorException;
import ameba.util.ClassUtils;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.HashMap;
import java.util.Map;

/**
 * 错误处理页面配置
 * @author icode
 */
@ConstrainedTo(RuntimeType.SERVER)
public class ErrorPageFeature implements Feature {
    private static final Logger logger = LoggerFactory.getLogger(ErrorPageFeature.class);

    private static final String GEN_CONF_KEY = "http.error.page.generator";

    @Override
    public final boolean configure(FeatureContext featureContext) {
        HashMap<Integer, String> errorMap = Maps.newHashMap();
        Map<String, Object> config = featureContext.getConfiguration().getProperties();
        String defaultTemplate = null;
        String clazz = (String) config.get(GEN_CONF_KEY);
        Class clz;
        if (StringUtils.isBlank(clazz)) {
            clz = ErrorPageGenerator.class;
        } else {
            try {
                clz = ClassUtils.getClass(clazz);
            } catch (ClassNotFoundException e) {
                throw new ConfigErrorException(GEN_CONF_KEY + "config error,not found class " + clazz,
                        GEN_CONF_KEY, e);
            }
        }

        for (String key : config.keySet()) {
            if (StringUtils.isNotBlank(key) && key.startsWith("http.error.page.")) {
                int startIndex = key.lastIndexOf(".");
                String statusCodeStr = key.substring(startIndex + 1);
                if (StringUtils.isNotBlank(statusCodeStr)) {
                    if (statusCodeStr.toLowerCase().equals("default")) {
                        defaultTemplate = (String) config.get(key);
                        defaultTemplate = defaultTemplate.startsWith("/") ? defaultTemplate :
                                "/" + defaultTemplate;
                    } else if (!statusCodeStr.toLowerCase().equals("generator")) {
                        try {
                            String va = (String) config.get(key);
                            int statusCode = Integer.parseInt(statusCodeStr);
                            if (StringUtils.isNotBlank(va))
                                errorMap.put(statusCode, va.startsWith("/") ? va : "/" + va);
                        } catch (Exception e) {
                            logger.error("parse http.compression.minSize error", e);
                        }
                    }
                }
            }
        }

        ErrorPageGenerator.setDefaultErrorTemplate(defaultTemplate);
        ErrorPageGenerator.pushAllErrorMap(errorMap);

        featureContext.register(clz);
        return true;
    }
}
