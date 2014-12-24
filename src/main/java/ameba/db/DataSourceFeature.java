package ameba.db;

import ameba.db.model.ModelManager;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.Map;
import java.util.Set;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
@Singleton
@ConstrainedTo(RuntimeType.SERVER)
public class DataSourceFeature implements Feature {

    private static final Map<String, DataSource> dataSourceMap = Maps.newHashMap();
    private static final Logger logger = LoggerFactory.getLogger(DataSourceFeature.class);

    /**
     * 根据数据源名称获取数据源
     *
     * @param name
     * @return
     */
    public static DataSource getDataSource(String name) {
        return dataSourceMap.get(name);
    }

    /**
     * 获取所有数据源名称
     *
     * @return
     */
    public static Set<String> getDataSourceNames() {
        return dataSourceMap.keySet();
    }

    @Override
    public boolean configure(final FeatureContext context) {
        Configuration config = context.getConfiguration();
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

        //用于重新加载
        dataSourceMap.clear();

        for (String name : map.keySet()) {
            try {
                Map<String, String> conf = map.get(name);
                String value = conf.get("init");
                if (StringUtils.isBlank(value)) {
                    conf.put("init", "true");
                }
                DataSource ds = DruidDataSourceFactory.createDataSource(conf);
                if (DruidDataSource.class.isInstance(ds))
                    ((DruidDataSource) ds).setName(name);
                dataSourceMap.put(name, ds);
            } catch (Exception e) {
                logger.error("配置数据源出错", e);
            }
        }
        return true;
    }
}