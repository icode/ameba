package ameba.mvc;

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
 * Created by icode on 14-3-25.
 */
@ConstrainedTo(RuntimeType.SERVER)
public class ErrorPageFeature implements Feature {
    private static final Logger logger = LoggerFactory.getLogger(ErrorPageFeature.class);

    @Override
    public final boolean configure(FeatureContext featureContext) {
        HashMap<Integer, String> errorMap = Maps.newHashMap();
        Map<String, Object> config = featureContext.getConfiguration().getProperties();
        String defaultTemplate = null;
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
        featureContext.register(ErrorPageGenerator.class);
        return true;
    }
}
