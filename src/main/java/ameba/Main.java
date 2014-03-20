package ameba;

import org.glassfish.grizzly.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class.
 */
public class Main {

    public static final Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        logger.info("启动服务...");
        final HttpServer server = Application.createHttpServer();
        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("关闭服务器..");
                server.shutdownNow();
            }
        }, "shutdownHook"));

        // run
        try {
            server.start();
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("启动服务器出现错误.", e);
        }
    }
}

