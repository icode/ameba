package ameba;

import ameba.container.Container;
import ameba.core.Application;
import ameba.exception.AmebaException;
import ameba.util.IOUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author icode
 */
public class Ameba {
    public static final Logger logger = LoggerFactory.getLogger(Ameba.class);
    public static final String LOGO = "\n\n" +
            "    _                   _           \n" +
            "   / \\   _ __ ___   ___| |__   __ _ \n" +
            "  / _ \\ | '_ ` _ \\ / _ \\ '_ \\ / _` |\n" +
            " / ___ \\| | | | | |  __/ |_) | (_| |\n" +
            "/_/   \\_\\_| |_| |_|\\___|_.__/ \\__,_|   {}\n\n";
    private static Application app;
    private static Container container;
    private static String version;

    private Ameba() {
    }

    public static ServiceLocator getServiceLocator() {
        return container.getServiceLocator();
    }

    public static Container getContainer() {
        return container;
    }

    public static Application getApp() {
        return app;
    }

    public static String getVersion() {

        if (version == null){
            version = IOUtils.getJarImplVersion(Ameba.class);
        }

        return version;
    }

    public static void printInfo() {
        logger.info(LOGO, version);
    }

    public static void main(String[] args) throws Exception {

        bootstrap();

        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shutdown();
            }
        }, "shutdownHook"));

        Thread.currentThread().join();
    }

    public static void bootstrap() throws Exception {
        bootstrap(new Application());
    }

    public static synchronized void bootstrap(Application application) throws Exception {
        if (Ameba.container != null) {
            throw new AmebaException("无法启动多个实例");
        }

        app = application;
        container = Container.create(app);

        // run
        logger.info("启动服务...");
        container.start();
    }

    public static synchronized void shutdown() {
        logger.info("关闭服务器...");
        try {
            container.shutdown();
        } catch (Exception e) {
            logger.error("服务器关闭出错", e);
        }
        logger.info("服务器已关闭");
    }
}
