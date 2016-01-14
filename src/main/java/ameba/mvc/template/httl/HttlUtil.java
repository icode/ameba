package ameba.mvc.template.httl;

import httl.util.CollectionUtils;
import httl.util.ConfigUtils;

import java.util.Map;
import java.util.Properties;

/**
 * @author icode
 */
public final class HttlUtil {
    // Built-in configuration name
    private static final String HTTL_DEFAULT_PROPERTIES = "httl-default.properties";
    // User configuration name
    private static final String HTTL_PROPERTIES = "httl.properties";
    // HTTL configuration prefix
    private static final String HTTL_PREFIX = "httl-";
    // HTTL configuration key prefix
    private static final String HTTL_KEY_PREFIX = "httl.";
    // HTTL configuration suffix
    private static final String PROPERTIES_SUFFIX = ".properties";
    // The modes configuration key
    private static final String MODES_KEY = "modes";
    // The engine name configuration key
    private static final String ENGINE_NAME = "engine.name";

    private HttlUtil() {
    }

    @SuppressWarnings("unchecked")
    public static Properties initProperties(String configPath, Properties configProperties) {
        Map<String, String> systemProperties = ConfigUtils.filterWithPrefix(HTTL_KEY_PREFIX, (Map) System.getProperties(), false);
        Map<String, String> systemEnv = ConfigUtils.filterWithPrefix(HTTL_KEY_PREFIX, System.getenv(), true);
        Properties properties = ConfigUtils.mergeProperties(HTTL_DEFAULT_PROPERTIES, configPath, configProperties, systemProperties, systemEnv);
        String[] modes = httl.util.StringUtils.splitByComma(properties.getProperty(MODES_KEY));
        if (CollectionUtils.isNotEmpty(modes)) {
            Object[] configs = new Object[modes.length + 5];
            configs[0] = HTTL_DEFAULT_PROPERTIES;
            for (int i = 0; i < modes.length; i++) {
                configs[i + 1] = HTTL_PREFIX + modes[i] + PROPERTIES_SUFFIX;
            }
            configs[modes.length + 1] = configPath;
            configs[modes.length + 2] = configProperties;
            configs[modes.length + 3] = systemProperties;
            configs[modes.length + 4] = systemEnv;
            properties = ConfigUtils.mergeProperties(configs);
        }
        properties.setProperty(ENGINE_NAME, configPath);
        return properties;
    }
}
