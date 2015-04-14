package ameba.db;

import ameba.core.AddOn;
import ameba.core.Application;
import ameba.db.model.ModelManager;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.ws.rs.core.Configuration;
import java.util.Map;
import java.util.Set;

/**
 * <p>DataSourceManager class.</p>
 *
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
public class DataSourceManager extends AddOn {

    private static final Map<String, javax.sql.DataSource> dataSourceMap = Maps.newHashMap();
    private static final Logger logger = LoggerFactory.getLogger(DataSourceManager.class);
    private static String DEFAULT_DS_NAME = "default";

    /**
     * <p>getDefaultDataSourceName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getDefaultDataSourceName() {
        return DEFAULT_DS_NAME;
    }

    /**
     * 根据数据源名称获取数据源
     *
     * @param name data source name
     * @return DataSource
     */
    public static DataSource getDataSource(String name) {
        return dataSourceMap.get(name);
    }

    /**
     * 获取所有数据源名称
     *
     * @return data source name set
     */
    public static Set<String> getDataSourceNames() {
        return dataSourceMap.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setup(final Application app) {
        Configuration config = app.getConfiguration();

        String dsName = (String) config.getProperty("db.default");

        if (StringUtils.isNotBlank(dsName)) {
            DEFAULT_DS_NAME = StringUtils.deleteWhitespace(dsName);
        }

        Map<String, Map<String, String>> map = Maps.newHashMap();
        for (String key : config.getPropertyNames()) {
            key = StringUtils.deleteWhitespace(key);
            key = key.replaceAll("\\.{2,}", ".");
            if (key.startsWith(ModelManager.MODULE_MODELS_KEY_PREFIX)) continue;
            //db.[DataSourceName].[ConfigKey]
            String[] keys = key.split("\\.");
            if (keys.length > 2 && "db".equals(keys[0])) {
                Map<String, String> sourceConfig = map.get(keys[1]);
                if (null == sourceConfig) {
                    sourceConfig = Maps.newHashMap();
                    map.put(keys[1], sourceConfig);
                }
                if (StringUtils.isNotBlank(keys[2])) {
                    sourceConfig.put(keys[2], String.valueOf(config.getProperty(key)));
                }
            }
        }

        for (String name : map.keySet()) {
            try {
                Map<String, String> conf = map.get(name);
                String value = conf.get("init");
                if (StringUtils.isBlank(value)) {
                    conf.put("init", "true");
                }
                DruidDataSource ds = (DruidDataSource) DruidDataSourceFactory.createDataSource(conf);
                ds.setName(name);
                ds.setDefaultAutoCommit(false);
                dataSourceMap.put(name, ds);
            } catch (Exception e) {
                logger.error("配置数据源出错", e);
            }
        }
    }
}
