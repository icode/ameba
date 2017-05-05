package ameba.db;

import ameba.container.event.ShutdownEvent;
import ameba.core.Addon;
import ameba.core.Application;
import ameba.db.model.ModelManager;
import ameba.event.SystemEventBus;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InstanceBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;

/**
 * <p>DataSourceManager class.</p>
 *
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
public class DataSourceManager extends Addon {

    private static final Map<String, DruidDataSource> dataSourceMap = Maps.newLinkedHashMap();
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
        Map<String, Object> config = app.getSrcProperties();

        String dsName = (String) config.get("db.default");

        if (StringUtils.isNotBlank(dsName)) {
            DEFAULT_DS_NAME = StringUtils.deleteWhitespace(dsName);
        }

        Map<String, Map<String, String>> map = Maps.newHashMap();
        for (String key : config.keySet()) {
            key = StringUtils.deleteWhitespace(key);
            key = key.replaceAll("\\.{2,}", ".");
            if (key.startsWith(ModelManager.MODULE_MODELS_KEY_PREFIX)) continue;
            //db.[DataSourceName].[ConfigKey]
            String[] keys = key.split("\\.");
            if (keys.length > 2 && "db".equals(keys[0])) {
                Map<String, String> sourceConfig = map.computeIfAbsent(keys[1], k -> Maps.newHashMap());
                if (StringUtils.isNotBlank(keys[2])) {
                    sourceConfig.put(keys[2], String.valueOf(config.get(key)));
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

        SystemEventBus.subscribe(ShutdownEvent.class,
                (ShutdownEvent event) -> {
                    dataSourceMap.forEach((name, dataSource) -> {
                        if (!dataSource.isClosed()) dataSource.close();
                    });
                    dataSourceMap.clear();
                });

        app.register(new AbstractBinder() {
            @Override
            protected void configure() {
                for (Map.Entry<String, DruidDataSource> entry : dataSourceMap.entrySet()) {
                    DruidDataSource ds = entry.getValue();
                    String name = entry.getKey();
                    createBuilder(ds).named(name);

                    if (getDefaultDataSourceName().equals(name)) {
                        createBuilder(ds);
                    }
                }
            }

            private InstanceBinding<DruidDataSource> createBuilder(DruidDataSource dataSource) {
                return bind(dataSource)
                        .to(DruidDataSource.class)
                        .to(DataSource.class)
                        .proxy(false);
            }
        });
    }
}
