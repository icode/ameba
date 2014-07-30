package ameba.container.grizzly.server;

import ameba.Application;
import ameba.container.Container;
import ameba.exceptions.AmebaException;
import ameba.mvc.assets.AssetsFeature;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.server.ContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author icode
 */
public class GrizzlyContainer extends Container {
    public static final Logger logger = LoggerFactory.getLogger(GrizzlyContainer.class);

    private HttpServer httpServer;

    private ServiceLocator serviceLocator;

    public GrizzlyContainer(Application app) {
        super(app);

        GrizzlyHttpContainer containerProvider = ContainerFactory.createContainer(GrizzlyHttpContainer.class, app);

        serviceLocator = containerProvider.getApplicationHandler().getServiceLocator();

        httpServer = GrizzlyServerFactory.createHttpServer(
                app.getHttpServerBaseUri(),
                containerProvider,
                app.getProperties(),
                getCompressionConfig(app),
                app.isSecureEnabled(),
                app.isAjpEnabled(),
                app.isJmxEnabled(),
                getSslEngineConfigurator(app),
                false);

        ServerConfiguration serverConfiguration = httpServer.getServerConfiguration();
        serverConfiguration.setHttpServerName(app.getApplicationName());
        serverConfiguration.setHttpServerVersion(app.getApplicationVersion());
        serverConfiguration.setName("Ameba-HttpServer-" + app.getApplicationName());

        String charset = StringUtils.defaultIfBlank((String) app.getProperty("app.encoding"), "utf-8");
        serverConfiguration.setSendFileEnabled(true);
        if (!app.isRegistered(AssetsFeature.class)) {
            Map<String, String[]> assetMap = AssetsFeature.getAssetMap(app);
            Set<String> mapKey = assetMap.keySet();
            for (String key : mapKey) {
                HttpHandler httpHandler = new CLStaticHttpHandler(ameba.Application.class.getClassLoader(), key + "/");
                httpHandler.setRequestURIEncoding(charset);
                serverConfiguration.addHttpHandler(httpHandler,
                        assetMap.get(key));
            }
        }

        httpServer.getServerConfiguration().setDefaultQueryEncoding(Charset.forName(charset));
    }

    private static CompressionConfig getCompressionConfig(ameba.Application app) {
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
        return compressionConfig;
    }

    private static SSLEngineConfigurator getSslEngineConfigurator(ameba.Application app) {
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
        return sslEngineConfigurator;
    }

    @Override
    public ServiceLocator getServiceLocator() {
        return serviceLocator;
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
