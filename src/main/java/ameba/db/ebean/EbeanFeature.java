package ameba.db.ebean;

import ameba.db.DataSourceFeature;
import ameba.db.TransactionFeature;
import ameba.db.ebean.transaction.EbeanTransactional;
import ameba.db.model.Model;
import ameba.model.ModelDescription;
import ameba.model.ModelManager;
import ameba.util.IOUtils;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.enhance.agent.InputStreamTransform;
import com.avaje.ebean.enhance.agent.Transformer;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import javassist.CannotCompileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
@ConstrainedTo(RuntimeType.SERVER)
public class EbeanFeature extends TransactionFeature {
    private static final Logger logger = LoggerFactory.getLogger(EbeanFeature.class);
    private static final int EBEAN_TRANSFORM_LOG_LEVEL = LoggerFactory.getLogger(Ebean.class).isDebugEnabled() ? 9 : 0;

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
                "/*--- Created by Ameba DDL */\n" +
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
        if (desc.getClassBytecode() != null) {
            in = new ByteArrayInputStream(desc.getClassBytecode());
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
            result = desc.getClassBytecode();
        }
        return result;
    }

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(EbeanTransactional.class);

        //configure EBean
        final Configuration appConfig = context.getConfiguration();
        for (String key : appConfig.getPropertyNames()) {
            if (key.startsWith("model.")) {
                Object value = appConfig.getProperty(key);
                if (null != value)
                    GlobalProperties.put(key.replaceFirst("model\\.", "ebean."), String.valueOf(value));
            }
        }

        for (final String name : DataSourceFeature.getDataSourceNames()) {
            final ServerConfig config = new ServerConfig();
            config.setPackages(null);
            config.setJars(null);

            //config.loadFromProperties();//设置默认配置
            config.setName(name);
            final boolean isProd = "product".equals(appConfig.getProperty("app.mode"));
            if (!isProd) {
                //转化部分公用属性
                /*"debug.lazyLoadSize"*/
                String value = (String) appConfig.getProperty("db." + name + ".debug.lazyLoadSize");
                if (null != value)
                    config.setLazyLoadBatchSize(Integer.valueOf(value));
            }

            config.setDataSource(DataSourceFeature.getDataSource(name));//设置为druid数据源
            if (name.equals(Model.DB_DEFAULT_SERVER_NAME)) {
                config.setDefaultServer(true);
            }

            ModelManager manager = ModelManager.getManager(name);

            config.setDdlGenerate(false);
            config.setDdlRun(false);

            String value = (String) appConfig.getProperty("db." + name + ".ddl.generate");
            boolean genDdl = false;
            if (null != value)
                genDdl = Boolean.valueOf(value);
            value = (String) appConfig.getProperty("db." + name + ".ddl.run");
            boolean runDdl = false;
            if (null != value)
                runDdl = Boolean.valueOf(value);

            manager.addModelLoadedListener(new ModelEventListener(config, isProd, genDdl, runDdl));
        }

        return true;
    }

    public static class ModelEventListener extends ModelManager.ModelEventListener {

        ServerConfig config;
        boolean isProd;
        boolean runDdl;
        boolean genDdl;

        public ModelEventListener(ServerConfig config, boolean isProd, boolean genDdl, boolean runDdl) {
            this.config = config;
            this.isProd = isProd;
            this.runDdl = runDdl;
            this.genDdl = genDdl;
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
            if (index == size - 1) {//最后一个model进行初始化ebean
                EbeanServer server = EbeanServerFactory.create(config);
                // DDL
                if (!isProd) {
                    if (genDdl) {
                        final String basePath = IOUtils.getResource("").getFile() + "conf/evolutions/" + server.getName() + "/";
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
                            Files.createDirectories(Paths.get(basePath));
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