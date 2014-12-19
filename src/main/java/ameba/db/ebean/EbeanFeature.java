package ameba.db.ebean;

import ameba.db.DataSourceFeature;
import ameba.db.TransactionFeature;
import ameba.db.ebean.transaction.EbeanTransactional;
import ameba.db.model.Model;
import ameba.enhancer.model.EnhanceModelFeature;
import ameba.enhancer.model.ModelDescription;
import ameba.enhancer.model.ModelManager;
import ameba.util.IOUtils;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ContainerConfig;
import com.avaje.ebean.config.PropertiesWrapper;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.enhance.agent.InputStreamTransform;
import com.avaje.ebean.enhance.agent.Transformer;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.fasterxml.jackson.core.JsonFactory;
import javassist.CannotCompileException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
@ConstrainedTo(RuntimeType.SERVER)
public class EbeanFeature extends TransactionFeature {
    private static final Logger logger = LoggerFactory.getLogger(EbeanFeature.class);
    private static final int EBEAN_TRANSFORM_LOG_LEVEL = LoggerFactory.getLogger(Ebean.class).isDebugEnabled() ? 9 : 0;
    private static String DEFAULT_DB_NAME = null;

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

    private static byte[] ehModel(ModelDescription desc) throws URISyntaxException, IOException, IllegalClassFormatException,
            ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, CannotCompileException {
        Transformer transformer = new Transformer("", "debug=" + EBEAN_TRANSFORM_LOG_LEVEL);
        InputStreamTransform streamTransform = new InputStreamTransform(transformer, Ebean.class.getClassLoader());
        InputStream in;
        if (desc.getClassByteCode() != null) {
            in = new ByteArrayInputStream(desc.getClassByteCode());
        } else {
            in = new URL(desc.getClassFile()).openStream();
        }
        byte[] result = null;
        try {
            result = streamTransform.transform(desc.getClassSimpleName(), in);
        } finally {
            IOUtils.closeQuietly(in);
        }
        if (result == null) {
            logger.debug("{} class not entity.", desc.getClassName());
            result = desc.getClassByteCode();
        }
        return result;
    }

    public static String getDefaultDBName() {
        return DEFAULT_DB_NAME;
    }

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(EbeanTransactional.class);
        final Configuration appConfig = context.getConfiguration();

        DEFAULT_DB_NAME = (String) appConfig.getProperty("db.default");

        if (StringUtils.isBlank(DEFAULT_DB_NAME)) {
            DEFAULT_DB_NAME = Model.DB_DEFAULT_SERVER_NAME;
        }

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
            final boolean isProd = "product".equals(appConfig.getProperty("app.mode"));

            config.setDataSource(DataSourceFeature.getDataSource(name));//设置为druid数据源
            if (name.equals(EbeanFeature.getDefaultDBName())) {
                config.setDefaultServer(true);
            }

            ModelManager manager = ModelManager.getManager(name);

            config.setDdlGenerate(false);
            config.setDdlRun(false);
            config.setJsonFactory(jsonFactory);
            config.setContainerConfig(containerConfig);

            String value = (String) appConfig.getProperty("db." + name + ".ddl.generate");
            boolean genDdl = false;
            if (null != value)
                genDdl = Boolean.valueOf(value);
            value = (String) appConfig.getProperty("db." + name + ".ddl.run");
            boolean runDdl = false;
            if (null != value)
                runDdl = Boolean.valueOf(value);

            ModelEventListener listener = new ModelEventListener(config, isProd, genDdl, runDdl);

            listener.bindManager(manager);

            if (config.isDefaultServer()) {
                listener.bindManager(EnhanceModelFeature.getModulesModelManager());
            }
        }

        return true;
    }

    private static class ModelEventListener extends ModelManager.ModelEventListener {

        ServerConfig config;
        boolean isProd;
        boolean runDdl;
        boolean genDdl;
        int managerCount = 0;

        public ModelEventListener(ServerConfig config, boolean isProd, boolean genDdl, boolean runDdl) {
            this.config = config;
            this.isProd = isProd;
            this.runDdl = runDdl;
            this.genDdl = genDdl;
        }

        public void addManagerCount() {
            managerCount++;
        }

        public void bindManager(ModelManager manager) {
            if (manager == null) return;
            manager.addModelLoadedListener(this);
            addManagerCount();
        }

        @Override
        protected byte[] enhancing(ModelDescription desc) {
            try {
                return ehModel(desc);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void loaded(Class clazz, ModelDescription desc, int index, int size) {
            config.addClass(clazz);
            if (index == size - 1 && (--managerCount) == 0) {//最后一个manager+model进行初始化ebean
                EbeanServer server = EbeanServerFactory.create(config);
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
                                if (genDdl) {
                                    writeDrop(getDropFileName());
                                    writeCreate(getCreateFileName());
                                }
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
        }
    }
}