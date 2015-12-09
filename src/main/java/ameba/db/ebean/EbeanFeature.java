package ameba.db.ebean;

import ameba.core.Application;
import ameba.db.DataSourceManager;
import ameba.db.PersistenceExceptionMapper;
import ameba.db.ebean.internal.EbeanModelInterceptor;
import ameba.db.ebean.jackson.JacksonEbeanModule;
import ameba.db.ebean.jackson.JsonIOExceptionMapper;
import ameba.db.model.ModelManager;
import ameba.i18n.Messages;
import ameba.util.IOUtils;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.*;
import com.avaje.ebean.dbmigration.DdlGenerator;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.hk2.utilities.binding.ScopedBindingBuilder;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * <p>EbeanFeature class.</p>
 *
 * @author icode
 * @since 2013-08-07
 */
public class EbeanFeature implements Feature {

    /**
     * Constant <code>SORT_PARAM_NAME="model.query.param.sort"</code>
     */
    public static final String SORT_PARAM_NAME = "model.query.param.sort";
    /**
     * Constant <code>PAGE_PARAM_NAME="model.query.param.page"</code>
     */
    public static final String PAGE_PARAM_NAME = "model.query.param.page";
    /**
     * Constant <code>PER_PAGE_PARAM_NAME="model.query.param.prePage"</code>
     */
    public static final String PER_PAGE_PARAM_NAME = "model.query.param.prePage";
    /**
     * Constant <code>REQ_TOTAL_COUNT_PARAM_NAME="model.query.param.requireTotalCount"</code>
     */
    public static final String REQ_TOTAL_COUNT_PARAM_NAME = "model.query.param.requireTotalCount";
    /**
     * Constant <code>REQ_TOTAL_COUNT_HEADER_NAME="model.query.requireTotalCount.header"</code>
     */
    public static final String REQ_TOTAL_COUNT_HEADER_NAME = "model.query.param.requireTotalCount.header";
    /**
     * Constant <code>DEFAULT_PER_PAGE_PARAM_NAME="model.query.param.perPage.default"</code>
     */
    public static final String DEFAULT_PER_PAGE_PARAM_NAME = "model.query.param.perPage.default";
    /**
     * Constant <code>EXCLUDE_DDL_PKG_KEY_SUFFIX=".ddl.generate.excludes"</code>
     */
    public static final String EXCLUDE_DDL_PKG_KEY_SUFFIX = ".ddl.generate.excludes";
    /**
     * Constant <code>FILTER_PARAM_NAME="model.query.param.where"</code>
     */
    public static final String FILTER_PARAM_NAME = "model.query.param.where";
    private static final Logger logger = LoggerFactory.getLogger(EbeanFeature.class);
    private static final List<EbeanServer> SERVERS = Lists.newArrayList();
    @Inject
    private ServiceLocator locator;
    @Inject
    private ObjectMapper objectMapper;
    @Inject
    private XmlMapper xmlMapper;
    @Inject
    private Application application;

    public static List<EbeanServer> getServers() {
        return Collections.unmodifiableList(SERVERS);
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
        ddl.setup((SpiEbeanServer) server, config);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(final FeatureContext context) {
        if (!context.getConfiguration().isRegistered(PersistenceExceptionMapper.class)) {
            context.register(PersistenceExceptionMapper.class);
        }

        if (context.getConfiguration().isRegistered(EbeanModelInterceptor.class)) {
            return false;
        }

        context.register(EbeanModelInterceptor.class)
                .register(JsonIOExceptionMapper.class);

        for (EbeanServer server : SERVERS) {
            try {
                server.shutdown(false, false);
            } catch (Exception e) {
                logger.warn("shut old ebean server has a error", e);
            }
        }
        SERVERS.clear();

        final Configuration appConfig = context.getConfiguration();

        final Properties eBeanConfig = new Properties();

        final JsonFactory jsonFactory = objectMapper.getFactory();

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

        for (final String name : DataSourceManager.getDataSourceNames()) {
            final ServerConfig config = new ServerConfig() {
                @Override
                public void loadFromProperties(Properties properties) {
                    loadSettings(new PropertiesWrapper("db", name, properties));
                }
            };
            config.setJsonInclude(JsonConfig.Include.NON_EMPTY);
            config.setPersistBatch(PersistBatch.ALL);
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
            config.loadFromProperties(eBeanConfig);
            config.setUpdateAllPropertiesInBatch(false);
            config.setPackages(null);
            config.setJars(null);
            config.setRegisterJmxMBeans("true".equals(appConfig.getProperty("jmx.enabled")));
            config.setName(name);
            config.setDataSourceJndiName(null);
            config.setDataSource(DataSourceManager.getDataSource(name));//设置为druid数据源
            config.setDdlGenerate(false);
            config.setDdlRun(false);
            config.setJsonFactory(jsonFactory);
            config.setContainerConfig(containerConfig);
            config.setResourceDirectory(null);
            config.setDisableClasspathSearch(true);

            if (name.equals(DataSourceManager.getDefaultDataSourceName())) {
                config.setDefaultServer(true);
            }

            Set<Class> classes = ModelManager.getModels(name);
            if (classes != null) {
                for (Class clazz : classes) {
                    config.addClass(clazz);
                }
            }

            final boolean isDev = application.getMode().isDev();

            final boolean genDdl = PropertiesHelper.getValue(appConfig.getProperties(),
                    "db." + name + ".ddl.generate", isDev, Boolean.class, null);

            final boolean runDdl = PropertiesHelper.getValue(appConfig.getProperties(),
                    "db." + name + ".ddl.run", false, Boolean.class, null);

            String[] ddlExcludes = new String[0];

            String ddlExcludeStr = (String) appConfig.getProperty("db." + name + EXCLUDE_DDL_PKG_KEY_SUFFIX);

            if (StringUtils.isNotBlank(ddlExcludeStr)) {
                ddlExcludes = ddlExcludeStr.split(",");
            }

            logger.debug(Messages.get("info.db.connect", name));

            final EbeanServer server = EbeanServerFactory.create(config);

            logger.info(Messages.get("info.db.connected", name, appConfig.getProperty("db." + name + ".url")));

            JacksonEbeanModule module = new JacksonEbeanModule(server, locator);

            objectMapper.registerModules(module);
            xmlMapper.registerModules(module);

            SERVERS.add(server);

            // DDL
            if (genDdl) {// todo: db migration 可能总是需要输出的ddl
                final String basePath = IOUtils.getResource("/").getPath()
                        + (isDev ? "../generated-sources/ameba/" : "temp/")
                        + "conf/evolutions/" + server.getName() + "/";

                DdlGenerator ddl = new AmebaGenerator(ddlExcludes, basePath);

                ddl.setup((SpiEbeanServer) server, config);
                try {
                    FileUtils.forceMkdir(new File(basePath));
                    ddl.generateDdl();
                } catch (Exception e) {
                    logger.error("Create ddl error", e);
                }
                if (runDdl) {
                    try {
                        ddl.runDdl();
                    } catch (Exception e) {
                        logger.error("Run ddl error", e);
                    }
                }
            }
        }

        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
                for (EbeanServer server : SERVERS) {
                    String name = server.getName();
                    createBuilder(server).named(name);

                    if (name.equals(DataSourceManager.getDefaultDataSourceName())) {
                        createBuilder(server);
                    }
                }
            }

            private ScopedBindingBuilder<SpiEbeanServer> createBuilder(EbeanServer server) {
                return bind((SpiEbeanServer) server)
                        .to(SpiEbeanServer.class)
                        .to(EbeanServer.class)
                        .proxy(false);
            }
        });
        return true;
    }

    private static class AmebaGenerator extends DdlGenerator {
        private String[] excludes;
        private String dropContent;
        private String createContent;
        private String basePath;

        public AmebaGenerator(String[] excludes, String basePath) {
            this.excludes = excludes;
            this.basePath = basePath;
        }

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
            return "/* Generated Drop Table DDL By Ameba */\n\n" + super.generateDropDdl();
        }

        @Override
        public String generateCreateDdl() {
            return "/* Generated Create Table DDL By Ameba */\n\n" + super.generateCreateDdl();
        }

        @Override
        public void generateDdl() {
            this.writeDrop(this.getDropFileName());
            this.writeCreate(this.getCreateFileName());
        }

        @Override
        public void runDdl() {
            try {
                if (dropContent == null) {
                    dropContent = readFile(getDropFileName());
                }
                if (createContent == null) {
                    createContent = readFile(getCreateFileName());
                }
                runScript(true, dropContent);
                runScript(false, createContent);

            } catch (IOException e) {
                String msg = "Error reading drop/create script from file system";
                throw new RuntimeException(msg, e);
            }
        }
    }
}
