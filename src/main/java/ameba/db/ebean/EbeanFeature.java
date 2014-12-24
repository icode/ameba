package ameba.db.ebean;

import ameba.db.DataSourceFeature;
import ameba.db.TransactionFeature;
import ameba.db.ebean.transaction.EbeanTransactional;
import ameba.db.model.ModelManager;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ContainerConfig;
import com.avaje.ebean.config.PropertiesWrapper;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.fasterxml.jackson.core.JsonFactory;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;
import java.util.Properties;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
@ConstrainedTo(RuntimeType.SERVER)
public class EbeanFeature extends TransactionFeature {

    public EbeanFeature() {
        super(EbeanFinder.class, EbeanPersister.class);
    }

    /**
     * Helper method that generates the required evolution to properly run Ebean.
     */
    public static String generateEvolutionScript(EbeanServer server, ServerConfig config, DdlGenerator ddl) {
        ddl.setup((SpiEbeanServer) server, config.getDatabasePlatform(), config);
        String create = ddl.generateCreateDdl();
        String drop = ddl.generateDropDdl();

        if (create == null || create.trim().isEmpty()) {
            return null;
        }

        return (
                "/* Created by Ameba DDL */\n" +
                        "\n" +
                        "/*--- !Drop */\n" +
                        "\n" +
                        drop +
                        "\n" +
                        "/*--- !Create */\n" +
                        "\n" +
                        create
        );
    }

    public static String generateEvolutionScript(EbeanServer server, ServerConfig config) {
        return generateEvolutionScript(server, config, new DdlGenerator());
    }

    public static String generateEvolutionScript(String serverName, ServerConfig config) {
        return generateEvolutionScript(Ebean.getServer(serverName), config);
    }


    @Override
    public boolean configure(final FeatureContext context) {
        context.register(EbeanTransactional.class);
        final Configuration appConfig = context.getConfiguration();

        final Properties eBeanConfig = new Properties();

        final JsonFactory jsonFactory = new JsonFactory();

        //读取过滤ebean配置
        for (String key : appConfig.getPropertyNames()) {
            if (key.startsWith("db.")) {
                Object value = appConfig.getProperty(key);
                if (null != value) {
                    eBeanConfig.put(key, String.valueOf(value));
                }
            }
        }

        //TODO ebean容器读取配置还未实现，实现后设置配置读取的转换，这个容器是全局的
        ContainerConfig containerConfig = new ContainerConfig();
        containerConfig.loadFromProperties(eBeanConfig);

        for (final String name : DataSourceFeature.getDataSourceNames()) {
            final ServerConfig config = new ServerConfig() {
                @Override
                public void loadFromProperties() {
                    loadSettings(new PropertiesWrapper("db", name, eBeanConfig));
                }
            };
            config.setPackages(null);
            config.setJars(null);
            config.setRegisterJmxMBeans(Boolean.parseBoolean((String) appConfig.getProperty("app.jmx.enabled")));
            config.setName(name);
            config.loadFromProperties(eBeanConfig);
            config.setDataSource(DataSourceFeature.getDataSource(name));//设置为druid数据源
            config.setDdlGenerate(false);
            config.setDdlRun(false);
            config.setJsonFactory(jsonFactory);
            config.setContainerConfig(containerConfig);

            if (name.equals(ModelManager.getDefaultDBName())) {
                config.setDefaultServer(true);
            }

            for (Class clazz : ModelManager.getModels(name)) {
                config.addClass(clazz);
            }

            EbeanServerFactory.create(config);
        }

        return true;
    }
}