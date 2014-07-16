package ameba.db.ebean;

import ameba.db.DataSourceFeature;
import ameba.db.TransactionFeature;
import ameba.db.ebean.transaction.EbeanTransactional;
import ameba.db.model.Model;
import ameba.db.model.ModelDescription;
import ameba.db.model.ModelManager;
import ameba.util.ByteArrayClassLoader;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.enhance.agent.InputStreamTransform;
import com.avaje.ebean.enhance.agent.Transformer;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;
import java.io.IOException;
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
    private static final ByteArrayClassLoader modelClassLoader = new ByteArrayClassLoader();
    public EbeanFeature() throws IllegalClassFormatException, IOException, URISyntaxException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        super(EbeanFinder.class, EbeanPersister.class);
        ehModel(new ModelDescription() {
            @Override
            public String getClassName() {
                return "ameba.db.model.Model";
            }

            @Override
            public String getClassFile() {
                return EbeanFeature.class.getResource("/" + getClassName().replaceAll("\\.", "/") + ".class").toExternalForm();
            }

            @Override
            public String getClassSimpleName() {
                return "Model";
            }
        });
    }

    /**
     * Helper method that generates the required evolution to properly run Ebean.
     */
    public static String generateEvolutionScript(EbeanServer server, ServerConfig config, DdlGenerator ddl) {
        ddl.setup((SpiEbeanServer) server, config.getDatabasePlatform(), config);
        String ups = ddl.generateCreateDdl();
        String downs = ddl.generateDropDdl();

        if (ups == null || ups.trim().isEmpty()) {
            return null;
        }

        return (
                "# --- Created by Ameba DDL\n" +
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

    public static String generateEvolutionScript(EbeanServer server, ServerConfig config) {
        return generateEvolutionScript(server, config, new DdlGenerator());
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
            if (key.startsWith("model.")) {
                Object value = appConfig.getProperty(key);
                if (null != value)
                    GlobalProperties.put(key.replaceFirst("model\\.", "ebean."), String.valueOf(value));
            }
        }

        for (String name : DataSourceFeature.getDataSourceNames()) {
            ServerConfig config = new ServerConfig();
            //config.loadFromProperties();//设置默认配置
            config.setName(name);
            boolean isProd = "product".equals(appConfig.getProperty("app.mode"));
            if (!isProd) {
                //转化部分公用属性
                /*"debug.lazyLoadSize"*/
                String value = (String) appConfig.getProperty("db." + name + ".debug.lazyLoadSize");
                if (null != value)
                    config.setLazyLoadBatchSize(Integer.valueOf(value));
            }

            config.setDataSource(DataSourceFeature.getDataSource(name));//设置为druid数据源
            if (name.equals(Model.DEFAULT_SERVER_NAME)) {
                config.setDefaultServer(true);
            }

            ModelManager manager = ModelManager.getManager(name);

            for (ModelDescription desc : manager.getModelClassesDesc()) {
                try {
                    ehModel(desc);
                    config.addClass(modelClassLoader.loadClass(desc.getClassName()));
                } catch (IOException | URISyntaxException | IllegalClassFormatException | ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            EbeanServer server = EbeanServerFactory.create(config);
            // DDL
            if (!isProd) {
                String value = (String) appConfig.getProperty("db." + name + ".ddl.generate");
                if (null != value)
                    config.setDdlGenerate(Boolean.valueOf(value));
                value = (String) appConfig.getProperty("db." + name + ".ddl.run");
                if (null != value)
                    config.setDdlRun(Boolean.valueOf(value));

                if (config.isDdlGenerate()) {
                    final String basePath = "conf/evolutions/" + server.getName() + "/";
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
                            return "# --- Generated Drop DDL by Ameba\n" +
                                    super.generateDropDdl();
                        }

                        @Override
                        public String generateCreateDdl() {
                            return "# --- Generated Create DDL by Ameba\n" +
                                    super.generateCreateDdl();
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

        return true;
    }

    private void ehModel(ModelDescription desc) throws URISyntaxException, IOException, IllegalClassFormatException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Transformer transformer = new Transformer("", "debug=5");
        InputStreamTransform streamTransform = new InputStreamTransform(transformer, ClassLoader.getSystemClassLoader());
        byte[] result = streamTransform.transform(desc.getClassSimpleName(), new URL(desc.getClassFile()).openStream());
        modelClassLoader.defineClass(desc.getClassName(), result);
    }
}