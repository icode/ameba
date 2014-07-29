package ameba;

import ameba.event.Listener;
import ameba.event.SystemEventBus;
import ameba.mvc.assets.AssetsFeature;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.ajp.AjpAddOn;
import org.glassfish.grizzly.http.server.*;
import org.glassfish.grizzly.spdy.SpdyAddOn;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author icode
 */
public class Ameba {
    public static final Logger logger = LoggerFactory.getLogger(Ameba.class);
    public static final String DEFAULT_NETWORK_LISTENER_NAME = "ameba";

    private static Application app;

    public static Application getApp() {
        return app;
    }

    public static void main(String[] args) {

        SystemEventBus.subscribe(Application.ConfiguredEvent.class, new Listener<Application.ConfiguredEvent>() {
            @Override
            public void onReceive(Application.ConfiguredEvent event) {
                app = event.getApp();
            }
        });

        bootstrap();
    }

    static Application bootstrap() {
        return bootstrap(new Application());
    }


    public static HttpServer createHttpServer() {

        Application app = new Application();

        return createHttpServer(app);
    }

    @SuppressWarnings("unchecked")
    public static HttpServer createHttpServer(Application app) {
        SSLEngineConfigurator sslEngineConfigurator = null;
        if (app.isSslConfigReady()) {
            SSLContextConfigurator sslContextConfiguration = new SSLContextConfigurator();
            sslContextConfiguration.setKeyPass(app.getSslKeyPassword());
            sslContextConfiguration.setSecurityProtocol(app.getSslProtocol());

            sslContextConfiguration.setKeyStoreBytes(app.getSslKeyStoreFile());
            sslContextConfiguration.setKeyStorePass(app.getSslKeyStorePassword());
            sslContextConfiguration.setKeyStoreProvider(app.getSslKeyStoreProvider());
            sslContextConfiguration.setKeyStoreType(app.getSslKeyStoreType());
            sslContextConfiguration.setKeyManagerFactoryAlgorithm(app.getSslKeyManagerFactoryAlgorithm());

            sslContextConfiguration.setTrustStoreBytes(app.getSslTrustStoreFile());
            if (StringUtils.isNotBlank(app.getSslTrustStorePassword()))
                sslContextConfiguration.setTrustStorePass(app.getSslTrustStorePassword());
            sslContextConfiguration.setTrustStoreType(app.getSslTrustStoreType());
            sslContextConfiguration.setTrustStoreProvider(app.getSslTrustStoreProvider());
            sslContextConfiguration.setTrustManagerFactoryAlgorithm(app.getSslTrustManagerFactoryAlgorithm());

            sslEngineConfigurator = new SSLEngineConfigurator(sslContextConfiguration,
                    app.isSslClientMode(), app.isSslNeedClientAuth(), app.isSslWantClientAuth());
        }

        CompressionConfig compressionConfig = new CompressionConfig();


        String modeStr = (String) app.getProperty("http.compression.mode");
        if (StringUtils.isNotBlank(modeStr) && ((modeStr = modeStr.toUpperCase()).equals("ON") || modeStr.equals("FORCE"))) {

            String minSizeStr = (String) app.getProperty("http.compression.minSize");
            String mimeTypesStr = (String) app.getProperty("http.compression.mimeTypes");
            String userAgentsStr = (String) app.getProperty("http.compression.ignore.userAgents");

            compressionConfig.setCompressionMode(CompressionConfig.CompressionMode.fromString(modeStr)); // the mode
            if (StringUtils.isNotBlank(minSizeStr))
                try {
                    compressionConfig.setCompressionMinSize(Integer.parseInt(minSizeStr)); // the min amount of bytes to compress
                } catch (Exception e) {
                    logger.error("parse http.compression.minSize error", e);
                }
            if (StringUtils.isNotBlank(mimeTypesStr))
                compressionConfig.setCompressableMimeTypes(mimeTypesStr.split(",")); // the mime types to compress
            if (StringUtils.isNotBlank(userAgentsStr))
                compressionConfig.setNoCompressionUserAgents(userAgentsStr.split(","));
        }


        HttpServer server = createHttpServer(
                app.getHttpServerBaseUri(),
                app,
                compressionConfig,
                app.isSecureEnabled(),
                app.isAjpEnabled(),
                app.isJmxEnabled(),
                sslEngineConfigurator,
                false);

        ServerConfiguration serverConfiguration = server.getServerConfiguration();
        serverConfiguration.setHttpServerName(app.getApplicationName());
        serverConfiguration.setHttpServerVersion(app.getApplicationVersion());
        serverConfiguration.setName("Ameba-HttpServer-" + app.getApplicationName());

        String charset = StringUtils.defaultIfBlank((String) app.getProperty("app.encoding"), "utf-8");
        serverConfiguration.setSendFileEnabled(true);
        if (!app.isRegistered(AssetsFeature.class)) {
            Map<String, String[]> assetMap = AssetsFeature.getAssetMap(app);
            Set<String> mapKey = assetMap.keySet();
            for (String key : mapKey) {
                HttpHandler httpHandler = new CLStaticHttpHandler(Application.class.getClassLoader(), key + "/");
                httpHandler.setRequestURIEncoding(charset);
                serverConfiguration.addHttpHandler(httpHandler,
                        assetMap.get(key));
            }
        }

        server.getServerConfiguration().setDefaultQueryEncoding(Charset.forName(charset));

        return server;
    }

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
                compressionCfg, secure, ajpEnabled, jmxEnabled, sslEngineConfigurator, start);
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
    public static HttpServer createHttpServer(final URI uri,
                                              final HttpHandler handler,
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
        final HttpServer server = new HttpServer();
        final NetworkListener listener = new NetworkListener(DEFAULT_NETWORK_LISTENER_NAME, host, port);
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


    static Application bootstrap(Application app) {
        final HttpServer server = createHttpServer(app);
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
