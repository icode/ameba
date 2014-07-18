package ameba.db.ebean;

import ameba.db.DataSourceFeature;
import ameba.db.TransactionFeature;
import ameba.db.ebean.transaction.EbeanTransactional;
import ameba.db.model.DefaultProperties;
import ameba.db.model.ModelDescription;
import ameba.db.model.ModelManager;
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
import javassist.ClassPool;
import javassist.CtClass;
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
    private static boolean baseModelEnhanced = false;
    private static final ModelDescription BASE_MODEL_DESCRIPTION = new ModelDescription() {
        byte[] bytecode;

        {
            try {
                bytecode = ehModel(this);
            } catch (URISyntaxException | IOException | IllegalClassFormatException
                    | ClassNotFoundException | NoSuchMethodException
                    | IllegalAccessException | InvocationTargetException
                    | CannotCompileException e) {
                throw new RuntimeException("get " + getClassFile() + " bytecode error", e);
            }
        }

        @Override
        public String getClassName() {
            return ModelManager.BASE_MODEL_NAME;
        }

        @Override
        public String getClassFile() {
            return EbeanFeature.class.getResource("/" + getClassName().replaceAll("\\.", "/") + ".class").toExternalForm();
        }

        @Override
        public String getClassSimpleName() {
            return ModelManager.BASE_MODEL_SIMPLE_NAME;
        }

        @Override
        public byte[] getClassBytecode() {
            return bytecode;
        }
    };

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
            if (name.equals(DefaultProperties.DB_DEFAULT_SERVER_NAME)) {
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

            try {
                manager.addModelLoadedListener(ModelEventListener.class, new Class[]{
                        ServerConfig.class, boolean.class, boolean.class, boolean.class
                }, config, isProd, genDdl, runDdl);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException("add model manager listener error", e);
            }

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

            if (!baseModelEnhanced) {
                try (InputStream in = new ByteArrayInputStream(BASE_MODEL_DESCRIPTION.getClassBytecode())) {
                    CtClass ctClass = ClassPool.getDefault().makeClass(in);
                    ctClass.toClass();
                    ctClass.detach();
                } catch (IOException | CannotCompileException e) {
                    throw new RuntimeException("make " + BASE_MODEL_DESCRIPTION.getClassFile() + " class input stream error", e);
                } finally {
                    baseModelEnhanced = true;
                }
            }

            try {
                return ehModel(desc);
            } catch (IOException | CannotCompileException | URISyntaxException | IllegalClassFormatException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
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

    private static final int EBEAN_TRANSFORM_LOG_LEVEL = LoggerFactory.getLogger(Ebean.class).isDebugEnabled() ? 9 : 0;

    private static byte[] ehModel(ModelDescription desc) throws URISyntaxException, IOException, IllegalClassFormatException,
            ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, CannotCompileException {
        Transformer transformer = new Transformer("", "debug=" + EBEAN_TRANSFORM_LOG_LEVEL);
        InputStreamTransform streamTransform = new InputStreamTransform(transformer, ClassLoader.getSystemClassLoader());
        InputStream in = null;
        if (desc.getClassBytecode() != null) {
            in = new ByteArrayInputStream(desc.getClassBytecode());
        } else {
            in = new URL(desc.getClassFile()).openStream();
        }
        byte[] result = null;
        try {
            result = streamTransform.transform(desc.getClassSimpleName(), in);
        } finally {
            in.close();
        }
        if (result == null) {
            throw new CannotCompileException("ebean enhance model fail!");
        }
        return result;
    }
}