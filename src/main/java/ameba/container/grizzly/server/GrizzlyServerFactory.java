package ameba.container.grizzly.server;

import ameba.container.grizzly.server.websocket.WebSocketAddOn;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.ajp.AjpAddOn;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.spdy.SpdyAddOn;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.tyrus.core.TyrusWebSocketEngine;
import org.glassfish.tyrus.core.Utils;
import org.glassfish.tyrus.core.cluster.ClusterContext;
import org.glassfish.tyrus.core.monitoring.ApplicationEventListener;
import org.glassfish.tyrus.server.TyrusServerContainer;
import org.glassfish.tyrus.spi.ServerContainer;
import org.glassfish.tyrus.spi.WebSocketEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.DeploymentException;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author icode
 */
public class GrizzlyServerFactory {
    public static final Logger logger = LoggerFactory.getLogger(GrizzlyServerFactory.class);
    public static final String DEFAULT_NETWORK_LISTENER_NAME = "ameba";
    /**
     * Server-side property to set custom worker {@link org.glassfish.grizzly.threadpool.ThreadPoolConfig}.
     * <p/>
     * Value is expected to be instance of {@link org.glassfish.grizzly.threadpool.ThreadPoolConfig}, can be {@code null} (it won't be used).
     */
    public static final String WORKER_THREAD_POOL_CONFIG = "app.container.server.workerThreadPoolConfig";

    /**
     * Server-side property to set custom selector {@link org.glassfish.grizzly.threadpool.ThreadPoolConfig}.
     * <p/>
     * Value is expected to be instance of {@link org.glassfish.grizzly.threadpool.ThreadPoolConfig}, can be {@code null} (it won't be used).
     */
    public static final String SELECTOR_THREAD_POOL_CONFIG = "app.container.server.selectorThreadPoolConfig";
    /**
     * Maximum size of incoming buffer in bytes.
     * <p/>
     * The value must be {@link java.lang.Integer} or its primitive alternative.
     * <p/>
     * Default value is 4194315, which means that TyrusWebSocketEngine is by default
     * capable of processing messages up to 4 MB.
     */
    public static final String INCOMING_BUFFER_SIZE = "app.websocket.incomingBufferSize";

    /**
     * Maximum number of open sessions per server application.
     * <p/>
     * The value must be positive {@link java.lang.Integer} or its primitive alternative. Negative values
     * and zero are ignored.
     * <p/>
     * The number of open sessions per application is not limited by default.
     */
    public static final String MAX_SESSIONS_PER_APP = "app.websocket.maxSessionsPerApp";

    /**
     * Maximum number of open sessions per unique remote address.
     * <p/>
     * The value must be positive {@link java.lang.Integer} or its primitive alternative. Negative values
     * and zero are ignored.
     * <p/>
     * The number of open sessions per remote address is not limited by default.
     */
    public static final String MAX_SESSIONS_PER_REMOTE_ADDR = "app.websocket.maxSessionsPerRemoteAddr";

    /**
     * Creates HttpServer instance.
     *
     * @param uri                   URI on which the Jersey web application will be deployed. Only first path segment
     *                              will be used as context path, the rest will be ignored.
     * @param configuration         web application configuration.
     * @param compressionCfg        {@link org.glassfish.grizzly.http.CompressionConfig} instance.
     * @param secure                used for call {@link org.glassfish.grizzly.http.server.NetworkListener#setSecure(boolean)}.
     * @param ajpEnabled            used for call {@link org.glassfish.grizzly.http.server.NetworkListener#registerAddOn(org.glassfish.grizzly.http.server.AddOn)}
     *                              {@link org.glassfish.grizzly.spdy.SpdyAddOn}.
     * @param jmxEnabled            {@link org.glassfish.grizzly.http.server.ServerConfiguration#setJmxEnabled(boolean)}.
     * @param sslEngineConfigurator Ssl settings to be passed to {@link org.glassfish.grizzly.http.server.NetworkListener#setSSLEngineConfig(org.glassfish.grizzly.ssl.SSLEngineConfigurator)}.
     * @param start                 if set to false, server will not get started, which allows to configure the
     *                              underlying transport, see above for details.
     * @return newly created {@link HttpServer}.
     */
    public static HttpServer createHttpServer(final URI uri,
                                              final ResourceConfig configuration,
                                              final CompressionConfig compressionCfg,
                                              final boolean secure,
                                              final boolean ajpEnabled,
                                              final boolean jmxEnabled,
                                              final SSLEngineConfigurator sslEngineConfigurator,
                                              final boolean start) {
        return createHttpServer(uri, ContainerFactory.createContainer(GrizzlyHttpContainer.class, configuration),
                configuration.getProperties(), compressionCfg, secure, ajpEnabled, jmxEnabled, sslEngineConfigurator, start);
    }

    /**
     * Creates HttpServer instance.
     *
     * @param uri                   uri on which the {@link org.glassfish.jersey.server.ApplicationHandler} will be deployed. Only first path
     *                              segment will be used as context path, the rest will be ignored.
     * @param compressionCfg        {@link org.glassfish.grizzly.http.CompressionConfig} instance.
     * @param handler               {@link org.glassfish.grizzly.http.server.HttpHandler} instance.
     * @param secure                used for call {@link org.glassfish.grizzly.http.server.NetworkListener#setSecure(boolean)}.
     * @param ajpEnabled            used for call {@link org.glassfish.grizzly.http.server.NetworkListener#registerAddOn(org.glassfish.grizzly.http.server.AddOn)}
     *                              {@link org.glassfish.grizzly.spdy.SpdyAddOn}.
     * @param jmxEnabled            {@link org.glassfish.grizzly.http.server.ServerConfiguration#setJmxEnabled(boolean)}.
     * @param sslEngineConfigurator Ssl settings to be passed to {@link org.glassfish.grizzly.http.server.NetworkListener#setSSLEngineConfig(org.glassfish.grizzly.ssl.SSLEngineConfigurator)}.
     * @param start                 if set to false, server will not get started, this allows end users to set
     *                              additional properties on the underlying listener.
     * @return newly created {@link HttpServer}.
     * @throws javax.ws.rs.ProcessingException
     * @see GrizzlyHttpContainer
     */
    @SuppressWarnings("unchecked")
    public static HttpServer createHttpServer(final URI uri,
                                              final HttpHandler handler,
                                              final Map<String, Object> webSocketConfig,
                                              final CompressionConfig compressionCfg,
                                              final boolean secure,
                                              final boolean ajpEnabled,
                                              final boolean jmxEnabled,
                                              final SSLEngineConfigurator sslEngineConfigurator,
                                              final boolean start)
            throws ProcessingException {
        final String host = (uri.getHost() == null) ? NetworkListener.DEFAULT_NETWORK_HOST
                : uri.getHost();
        final int port = (uri.getPort() == -1) ? 80 : uri.getPort();
        final NetworkListener listener = new NetworkListener(DEFAULT_NETWORK_LISTENER_NAME, host, port);

        final ServerContainer webSocketContainer = bindWebSocket(webSocketConfig, listener);
        final HttpServer server = new HttpServer() {
            @Override
            public synchronized void start() throws IOException {
                if (webSocketContainer != null)
                    try {
                        webSocketContainer.start("/", port);
                    } catch (DeploymentException e) {
                        logger.error("启动websocket容器失败", e);
                    }
                super.start();
            }

            @Override
            public synchronized GrizzlyFuture<HttpServer> shutdown(long gracePeriod, TimeUnit timeUnit) {
                if (webSocketContainer != null)
                    webSocketContainer.stop();
                return super.shutdown(gracePeriod, timeUnit);
            }

            @Override
            public synchronized void shutdownNow() {
                if (webSocketContainer != null)
                    webSocketContainer.stop();
                super.shutdownNow();
            }
        };


        listener.setSecure(secure);
        if (sslEngineConfigurator != null) {
            listener.setSSLEngineConfig(sslEngineConfigurator);

            if (secure && !ajpEnabled) {
                SpdyAddOn spdyAddon = new SpdyAddOn();
                listener.registerAddOn(spdyAddon);
            } else if (secure) {
                logger.warn("AJP模式开启，不启动SPDY支持");
            }
        }

        if (ajpEnabled) {
            AjpAddOn ajpAddon = new AjpAddOn();
            listener.registerAddOn(ajpAddon);
        }

        server.getServerConfiguration().setJmxEnabled(jmxEnabled);

        server.addListener(listener);
        CompressionConfig compressionConfig = listener.getCompressionConfig();
        if (compressionCfg != null) {
            compressionConfig.set(compressionCfg);
        }

        // Map the path to the processor.
        final ServerConfiguration config = server.getServerConfiguration();
        if (handler != null) {
            config.addHttpHandler(handler, uri.getPath());
        }

        config.setPassTraceRequest(true);

        if (start) {
            try {
                // Start the server.
                server.start();
            } catch (IOException ex) {
                String msg = "无法启动HTTP服务";
                logger.error(msg, ex);
                throw new ProcessingException(msg, ex);
            }
        }

        return server;
    }

    private static ServerContainer bindWebSocket(Map<String, Object> properties, final NetworkListener listener) {

        final Map<String, Object> localProperties;
        // defensive copy
        if (properties == null) {
            localProperties = Collections.emptyMap();
        } else {
            localProperties = new HashMap<String, Object>(properties);
        }

        final Integer incomingBufferSize = Utils.getProperty(localProperties, INCOMING_BUFFER_SIZE, Integer.class);
        final ClusterContext clusterContext = Utils.getProperty(localProperties, ClusterContext.CLUSTER_CONTEXT, ClusterContext.class);
        final ApplicationEventListener applicationEventListener = Utils.getProperty(localProperties, ApplicationEventListener.APPLICATION_EVENT_LISTENER, ApplicationEventListener.class);
        final Integer maxSessionsPerApp = Utils.getProperty(localProperties, MAX_SESSIONS_PER_APP, Integer.class);
        final Integer maxSessionsPerRemoteAddr = Utils.getProperty(localProperties, MAX_SESSIONS_PER_REMOTE_ADDR, Integer.class);

        return new TyrusServerContainer((Set<Class<?>>)null) {

            private final WebSocketEngine engine = TyrusWebSocketEngine.builder(this)
                    .incomingBufferSize(incomingBufferSize)
                    .clusterContext(clusterContext)
                    .applicationEventListener(applicationEventListener)
                    .maxSessionsPerApp(maxSessionsPerApp)
                    .maxSessionsPerRemoteAddr(maxSessionsPerRemoteAddr)
                    .build();

            private String contextPath;

            @Override
            public void register(Class<?> endpointClass) throws DeploymentException {
                engine.register(endpointClass, contextPath);
            }

            @Override
            public void register(ServerEndpointConfig serverEndpointConfig) throws DeploymentException {
                engine.register(serverEndpointConfig, contextPath);
            }

            @Override
            public WebSocketEngine getWebSocketEngine() {
                return engine;
            }

            @Override
            public void start(final String rootPath, int port) throws IOException, DeploymentException {
                contextPath = rootPath;
                // server = HttpServer.createSimpleServer(rootPath, port);
                ThreadPoolConfig workerThreadPoolConfig = Utils.getProperty(localProperties, WORKER_THREAD_POOL_CONFIG, ThreadPoolConfig.class);
                ThreadPoolConfig selectorThreadPoolConfig = Utils.getProperty(localProperties, SELECTOR_THREAD_POOL_CONFIG, ThreadPoolConfig.class);

                // TYRUS-287: configurable server thread pools
                if (workerThreadPoolConfig != null || selectorThreadPoolConfig != null) {
                    TCPNIOTransportBuilder transportBuilder = TCPNIOTransportBuilder.newInstance();
                    if (workerThreadPoolConfig != null) {
                        transportBuilder.setWorkerThreadPoolConfig(workerThreadPoolConfig);
                    }
                    if (selectorThreadPoolConfig != null) {
                        transportBuilder.setSelectorThreadPoolConfig(selectorThreadPoolConfig);
                    }
                    transportBuilder.setIOStrategy(WorkerThreadIOStrategy.getInstance());
                    listener.setTransport(transportBuilder.build());
                } else {
                    // if no configuration is set, just update IO Strategy to worker thread strat.
                    listener.getTransport().setIOStrategy(WorkerThreadIOStrategy.getInstance());
                }

                // idle timeout set to indefinite.
                listener.getKeepAlive().setIdleTimeoutInSeconds(-1);
                listener.registerAddOn(new WebSocketAddOn(this));

                if (applicationEventListener != null) {
                    applicationEventListener.onApplicationInitialized(rootPath);
                }

                super.start(rootPath, port);
            }

            @Override
            public void stop() {
                super.stop();
                if (applicationEventListener != null) {
                    applicationEventListener.onApplicationDestroyed();
                }
            }
        };
    }
}
