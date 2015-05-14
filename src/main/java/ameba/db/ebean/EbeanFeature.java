package ameba.db.ebean;

import ameba.db.DataSourceManager;
import ameba.db.OrmFeature;
import ameba.db.ebean.internal.EbeanModelInterceptor;
import ameba.db.ebean.jackson.JacksonEbeanModule;
import ameba.db.ebean.jackson.JsonIOExceptionMapper;
import ameba.db.migration.DatabaseMigrationFeature;
import ameba.db.model.ModelManager;
import ameba.i18n.Messages;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.*;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.*;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
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
import javax.ws.rs.core.FeatureContext;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * <p>EbeanFeature class.</p>
 *
 * @author icode
 * @since 2013-08-07
 */
public class EbeanFeature extends OrmFeature {

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
     * Constant <code>WHERE_PARAM_NAME="model.query.param.where"</code>
     */
    public static final String WHERE_PARAM_NAME = "model.query.param.where";
    private static final Logger logger = LoggerFactory.getLogger(EbeanFeature.class);
    private static final List<EbeanServer> SERVERS = Lists.newArrayList();

    static {
        setFinderClass(EbeanFinder.class);
        setPersisterClass(EbeanPersister.class);
        setUpdaterClass(EbeanUpdater.class);
    }

    @Inject
    private ServiceLocator locator;
    @Inject
    private ObjectMapper objectMapper;
    @Inject
    private XmlMapper xmlMapper;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(final FeatureContext context) {
        super.configure(context);

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
            config.setPackages(null);
            config.setJars(null);
            config.setRegisterJmxMBeans("true".equals(appConfig.getProperty("app.jmx.enabled")));
            config.setName(name);
            config.setDataSourceJndiName(null);
            config.setDataSource(DataSourceManager.getDataSource(name));//设置为druid数据源
            config.setDdlGenerate(false);
            config.setDdlRun(false);
            config.setJsonFactory(jsonFactory);
            config.setContainerConfig(containerConfig);
            config.setPackages(null);
            config.setJars(null);
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

            final boolean genDdl = PropertiesHelper.getValue(appConfig.getProperties(),
                    "db." + name + ".ddl.generate", false, Boolean.class, null);

//            final boolean runDdl = PropertiesHelper.getValue(appConfig.getProperties(),
//                    "db." + name + ".ddl.run", false, Boolean.class, null);

            String[] excludes = null;

            String excludeStr = (String) appConfig.getProperty("db." + name + EXCLUDE_DDL_PKG_KEY_SUFFIX);

            if (StringUtils.isNotBlank(excludeStr)) {
                excludes = excludeStr.split(",");
            }

            logger.debug(Messages.get("info.db.connect", name));

            final EbeanServer server = EbeanServerFactory.create(config);

            logger.info(Messages.get("info.db.connected", name, appConfig.getProperty("db." + name + ".url")));

            JacksonEbeanModule module = new JacksonEbeanModule(server, locator);

            objectMapper.registerModules(module);
            xmlMapper.registerModules(module);

            SERVERS.add(server);

            // DDL
            if (genDdl) {
                final String basePath = DatabaseMigrationFeature.getEvolutionsBasePath() + server.getName() + "/";

                DdlGenerator ddl = new AmebaGenerator(excludes, basePath, (SpiEbeanServer) server);

                ddl.setup((SpiEbeanServer) server, config.getDatabasePlatform(), config);
                try {
                    FileUtils.forceMkdir(new File(basePath));
                    ddl.generateDdl();
                } catch (IOException e) {
                    logger.error("Create ddl error", e);
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
        private SpiEbeanServer server;

        public AmebaGenerator(String[] excludes, String basePath, SpiEbeanServer server) {
            this.excludes = excludes;
            this.basePath = basePath;
            this.server = server;
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

            DdlGenContext ctx = createContext();

            if (ctx.getDdlSyntax().isDropKeyConstraints()) {
                // generate drop foreign key constraint statements (sql server joy)
                AddForeignKeysVisitor fkeys = new AddForeignKeysVisitor(false, ctx);
                visit(server, fkeys);
                ctx.writeNewLine();
            }

            DropTableVisitor drop = new DropTableVisitor(ctx);
            visit(server, drop);

            DropSequenceVisitor dropSequence = new DropSequenceVisitor(ctx);
            visit(server, dropSequence);

            ctx.flush();
            dropContent = ctx.getContent();
            return "/* Generated Drop Table DDL By Ameba */\n\n" + dropContent;
        }

        public void visit(SpiEbeanServer server, BeanVisitor visitor) {
            visit(server.getBeanDescriptors(), visitor);
        }


        /**
         * Visit all the descriptors in the list.
         */
        public void visit(List<BeanDescriptor<?>> descriptors, BeanVisitor visitor) {

            visitor.visitBegin();

            for (BeanDescriptor<?> desc : descriptors) {

                if (desc.getBaseTable() != null && !isExclude(desc)) {
                    VisitorUtil.visitBean(desc, visitor);
                }
            }

            visitor.visitEnd();
        }

        private boolean isExclude(BeanDescriptor<?> desc) {
            for (String exclude : excludes) {
                if (desc.getFullName().equals(exclude)
                        || desc.getFullName().startsWith(exclude + ".")) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String generateCreateDdl() {

            DdlGenContext ctx = createContext();
            CreateTableVisitor create = new CreateTableVisitor(ctx);
            visit(server, create);

            CreateSequenceVisitor createSequence = new CreateSequenceVisitor(ctx);
            visit(server, createSequence);

            AddForeignKeysVisitor fkeys = new AddForeignKeysVisitor(true, ctx);
            visit(server, fkeys);

            CreateIndexVisitor indexes = new CreateIndexVisitor(ctx);
            visit(server, indexes);

            ctx.flush();
            createContent = ctx.getContent();
            return "/* Generated Create Table DDL By Ameba */\n\n" + createContent;
        }

        @Override
        public void generateDdl() {
            writeDrop(getDropFileName());
            writeCreate(getCreateFileName());
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
