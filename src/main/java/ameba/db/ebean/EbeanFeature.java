package ameba.db.ebean;

import ameba.db.DataSource;
import ameba.db.TransactionFeature;
import ameba.db.ebean.internal.EbeanModelWriter;
import ameba.db.ebean.transaction.EbeanTransactional;
import ameba.db.model.ModelManager;
import ameba.exception.ConfigErrorException;
import ameba.message.internal.JacksonUtils;
import ameba.util.IOUtils;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.*;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.fasterxml.jackson.core.JsonFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.avaje.ebeanorm.jackson.JacksonEbeanModule;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * @author icode
 * @since 2013-08-07
 */
public class EbeanFeature extends TransactionFeature {

    private static final Logger logger = LoggerFactory.getLogger(EbeanFeature.class);

    public static final String SELECTABLE_PARAM_NAME = "model.query.selectable";
    public static final String ORDER_BY_PARAM_NAME = "model.query.orderBy";
    public static final String MAX_ROWS_PARAM_NAME = "model.query.maxRows";
    public static final String FIRST_ROW_PARAM_NAME = "model.query.firstRow";
    public static final String DEFAULT_MAX_ROWS_PARAM_NAME = "model.query.maxRows.default";
    public static final String WHERE_PARAM_NAME = "model.query.where";

    static {
        setFinderClass(EbeanFinder.class);
        setPersisterClass(EbeanPersister.class);
        setUpdaterClass(EbeanUpdater.class);
    }

    /**
     * Helper method that generates the required evolution to properly run Ebean.
     *
     * @param server ebean server
     * @param config server config
     * @param ddl    ddl generator
     * @return ddl
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

    /**
     * 生成sql语句
     *
     * @param server ebean server
     * @param config server config
     * @return ddl
     */
    public static String generateEvolutionScript(EbeanServer server, ServerConfig config) {
        return generateEvolutionScript(server, config, new DdlGenerator());
    }

    /**
     * 生成sql语句
     *
     * @param serverName ebean server name
     * @param config     server config
     * @return ddl
     */
    public static String generateEvolutionScript(String serverName, ServerConfig config) {
        return generateEvolutionScript(Ebean.getServer(serverName), config);
    }


    @Override
    public boolean configure(final FeatureContext context) {
        context.register(EbeanTransactional.class);
        context.register(EbeanModelWriter.class);
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

        for (final String name : DataSource.getDataSourceNames()) {
            final ServerConfig config = new ServerConfig() {
                @Override
                public void loadFromProperties(Properties properties) {
                    loadSettings(new PropertiesWrapper("db", name, properties));
                }
            };
            config.setNamingConvention(new UnderscoreNamingConvention() {

                String tableNamePrefix = null;

                @Override
                public void loadFromProperties(PropertiesWrapper properties) {
                    super.loadFromProperties(properties);
                    tableNamePrefix = properties.get("namingConvention.table.name.prefix", tableNamePrefix);
                }

                public TableName getTableNameByConvention(Class<?> beanClass) {

                    String tableName = beanClass.getSimpleName();

                    if (StringUtils.isNotBlank(tableNamePrefix)) {
                        tableName = tableNamePrefix + tableName;
                    }

                    return new TableName(
                            getCatalog(),
                            getSchema(),
                            toUnderscoreFromCamel(tableName));
                }
            });
            config.setPackages(null);
            config.setJars(null);
            config.setRegisterJmxMBeans(Boolean.parseBoolean((String) appConfig.getProperty("app.jmx.enabled")));
            config.setName(name);
            config.loadFromProperties(eBeanConfig);
            config.setDataSource(DataSource.getDataSource(name));//设置为druid数据源
            config.setDdlGenerate(false);
            config.setDdlRun(false);
            config.setJsonFactory(jsonFactory);
            config.setContainerConfig(containerConfig);

            if (name.equals(DataSource.getDefaultDataSourceName())) {
                config.setDefaultServer(true);
            }


            Set<Class> classes = ModelManager.getModels(name);
            if (classes == null) {
                throw new ConfigErrorException("please config db.{name}.models property");
            }
            for (Class clazz : classes) {
                config.addClass(clazz);
            }

            final boolean genDdl = PropertiesHelper.getValue(appConfig.getProperties(),
                    "db." + name + ".ddl.generate", false, Boolean.class, null);

            final boolean runDdl = PropertiesHelper.getValue(appConfig.getProperties(),
                    "db." + name + ".ddl.run", false, Boolean.class, null);

            final boolean isProd = "product".equals(appConfig.getProperty("app.mode"));

            EbeanServer server = EbeanServerFactory.create(config);

            JacksonUtils.addDefaultModule(new JacksonEbeanModule(server.json()) {
                @Override
                public String getModuleName() {
                    return super.getModuleName() + "-" + name + "-server";
                }
            });

            // DDL
            if (!isProd) {
                if (genDdl) {
                    final String basePath = IOUtils.getResource("").getPath() + "conf/evolutions/" + server.getName() + "/";
                    DdlGenerator ddl = new DdlGenerator() {
                        @Override
                        protected String getDropFileName() {
                            return basePath + "drop.sql";
                        }

                        @Override
                        protected String getCreateFileName() {
                            return basePath + "create.sql";
                        }

                        @Override
                        public String generateDropDdl() {
                            return "/* Generated Drop Table DDL By Ameba */\n\n" +
                                    super.generateDropDdl();
                        }

                        @Override
                        public String generateCreateDdl() {
                            return "/* Generated Create Table DDL By Ameba */\n\n" +
                                    super.generateCreateDdl();
                        }

                        @Override
                        public void generateDdl() {
                            writeDrop(getDropFileName());
                            writeCreate(getCreateFileName());
                        }

                        @Override
                        public void runDdl() {
                            if (runDdl) {
                                try {
                                    runScript(true, readFile(getDropFileName()));
                                    runScript(false, readFile(getCreateFileName()));
                                } catch (IOException e) {
                                    String msg = "Error reading drop/create script from file system";
                                    throw new RuntimeException(msg, e);
                                }
                            }
                        }
                    };
                    ddl.setup((SpiEbeanServer) server, config.getDatabasePlatform(), config);
                    try {
                        FileUtils.forceMkdir(new File(basePath));
                        ddl.generateDdl();
                        ddl.runDdl();
                    } catch (IOException e) {
                        logger.error("Create ddl error", e);
                    }
                }
            }
        }

        return true;
    }
}