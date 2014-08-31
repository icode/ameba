package ameba;

import ameba.container.Container;
import ameba.exceptions.AmebaException;
import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author icode
 */
public class Ameba {
    public static final Logger logger = LoggerFactory.getLogger(Ameba.class);

    private static Application app;
    private static Container container;

    private Ameba() {
    }

    public static ServiceLocator getServiceLocator() {
        return container.getServiceLocator();
    }

    public static Application getApp() {
        return app;
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
        logger.info("启动容器...");
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
