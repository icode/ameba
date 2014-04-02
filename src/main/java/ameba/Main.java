package ameba;

import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * Main class.
 */
public class Main {

    public static final Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        final HttpServer server = Application.createHttpServer();
        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("关闭服务器..");
                GrizzlyFuture<HttpServer> future = server.shutdown();
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("服务器关闭出错.", e);
                }
                logger.info("服务器已关闭.");
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

