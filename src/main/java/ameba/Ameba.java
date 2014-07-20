package ameba;

import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * @author icode
 */
public class Ameba {
    public static final Logger logger = LoggerFactory.getLogger(Ameba.class);

    private static Application app;

    public static Application getApp() {
        return app;
    }

    public static void main(String[] args) {
        bootstrap();
    }

    static Application bootstrap() {
        app = new Application();
        final HttpServer server = Application.createHttpServer(app);
        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("关闭服务器...");
                GrizzlyFuture<HttpServer> future = server.shutdown();
                try {
                    future.get();
                } catch (InterruptedException e) {
                    logger.error("服务器关闭出错", e);
                } catch (ExecutionException e) {
                    logger.error("服务器关闭出错", e);
                }
                logger.info("服务器已关闭");
            }
        }, "shutdownHook"));

        // run
        try {
            logger.info("启动容器...");
            server.start();
            logger.info("服务已启动");
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("启动服务器出现错误", e);
        }
        return app;
    }
}
