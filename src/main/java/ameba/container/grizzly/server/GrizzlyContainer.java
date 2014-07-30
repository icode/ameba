package ameba.container.grizzly.server;

import ameba.Application;
import ameba.container.Container;
import ameba.exceptions.AmebaException;
import org.glassfish.grizzly.http.server.HttpServer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author icode
 */
public class GrizzlyContainer extends Container {

    private HttpServer httpServer;

    public GrizzlyContainer(Application application) {
        super(application);
        httpServer = GrizzlyServerFactory.createHttpServer(application);
    }

    @Override
    public void start() {
        try {
            httpServer.start();
        } catch (IOException e) {
            throw new AmebaException("端口无法使用", e);
        }
    }

    @Override
    public void shutdown() throws ExecutionException, InterruptedException {
        httpServer.shutdown().get();
    }
}
