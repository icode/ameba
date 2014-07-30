package ameba;

import ameba.container.Container;
import ameba.event.Listener;
import ameba.event.SystemEventBus;
import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author icode
 */
public class Ameba {
    public static final Logger logger = LoggerFactory.getLogger(Ameba.class);

    private static Application app;
    private static ServiceLocator serviceLocator;

    public static ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public static Application getApp() {
        return app;
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {

        SystemEventBus.subscribe(Application.ConfiguredEvent.class, new Listener<Application.ConfiguredEvent>() {
            @Override
            public void onReceive(Application.ConfiguredEvent event) {
                app = event.getApp();
            }
        });

        bootstrap();
    }

    static Application bootstrap() throws IllegalAccessException, InstantiationException {
        return bootstrap(new Application());
    }

    static Application bootstrap(Application app) throws InstantiationException, IllegalAccessException {
        final Container container = Container.create(app);
        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("关闭服务器...");
                try {
                    container.shutdown();
                } catch (Exception e) {
                    logger.error("服务器关闭出错", e);
                }
                logger.info("服务器已关闭");
            }
        }, "shutdownHook"));

        // run
        try {
            logger.info("启动容器...");
            container.start();
            logger.info("服务已启动");
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("启动服务器出现错误", e);
        }
        return app;
    }
}
