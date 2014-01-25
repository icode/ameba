package ameba.db.ebean;

import ameba.db.DataSourceFeature;
import ameba.db.TransactionFeature;
import ameba.db.ebean.transaction.EbeanTransactional;
import ameba.db.model.ModelManager;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
@ConstrainedTo(RuntimeType.SERVER)
public class EbeanFeature extends TransactionFeature {

    /**
     * Helper method that generates the required evolution to properly run Ebean.
     */
    public static String generateEvolutionScript(EbeanServer server, ServerConfig config) {
        DdlGenerator ddl = new DdlGenerator((SpiEbeanServer) server, config.getDatabasePlatform(), config);
        String ups = ddl.generateCreateDdl();
        String downs = ddl.generateDropDdl();

        if (ups == null || ups.trim().isEmpty()) {
            return null;
        }

        return (
                "# --- Created by Ebean DDL\n" +
                        "# To stop Ebean DDL generation, remove this comment and start using Evolutions\n" +
                        "\n" +
                        "# --- !Ups\n" +
                        "\n" +
                        ups +
                        "\n" +
                        "# --- !Downs\n" +
                        "\n" +
                        downs
        );
    }

    public static String generateEvolutionScript(String serverName, ServerConfig config) {
        return generateEvolutionScript(Ebean.getServer(serverName), config);
    }

    @Override
    public boolean configure(final FeatureContext context) {
        super.configure(context);
        context.register(EbeanTransactional.class);

        //configure EBean
        Configuration appConfig = context.getConfiguration();
        for (String key : appConfig.getPropertyNames()) {
            if (key.startsWith("ebean.")) {
                Object value = appConfig.getProperty(key);
                if (null != value)
                    GlobalProperties.put(key, String.valueOf(value));
            }
        }

        for (String name : DataSourceFeature.getDataSourceNames()) {
            ServerConfig config = new ServerConfig();
            config.loadFromProperties();//设置默认配置
            config.setName(name);
            if (!"product".equals(appConfig.getProperty("app.mode"))) {
                //转化部分公用属性
                /*"ddl.generate", "ddl.run", "debug.sql", "debug.lazyload"*/
                String value = (String) appConfig.getProperty("db." + name + ".ddl.generate");
                if (null != value)
                    config.setDdlGenerate(Boolean.valueOf(value));

                value = (String) appConfig.getProperty("db." + name + ".ddl.run");
                if (null != value)
                    config.setDdlRun(Boolean.valueOf(value));

                value = (String) appConfig.getProperty("db." + name + ".debug.sql");
                if (null != value)
                    config.setDebugSql(Boolean.valueOf(value));

                value = (String) appConfig.getProperty("db." + name + ".debug.lazyload");
                if (null != value)
                    config.setDebugLazyLoad(Boolean.valueOf(value));
            }

            config.setDataSource(DataSourceFeature.getDataSource(name));//设置为druid数据源
            if (name.equals("default")) {
                config.setDefaultServer(true);
            }

            ModelManager manager = ModelManager.getManager(name);

            if (manager != null)

                for (Class clazz : manager.getModelClasses()) {
                    config.addClass(clazz);
                }

            EbeanServerFactory.create(config);
        }

        return true;
    }

}