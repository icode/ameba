package ameba;

import ameba.db.model.EnhanceModelFeature;
import ameba.dev.ReloadingClassLoader;
import ameba.dev.JvmAgent;
import ameba.mvc.assets.AssetsFeature;
import ameba.util.IOUtils;
import ameba.util.LinkedProperties;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferUtil;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.ajp.AjpAddOn;
import org.glassfish.grizzly.http.server.*;
import org.glassfish.grizzly.spdy.SpdyAddOn;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.inject.Singleton;
import javax.ws.rs.ProcessingException;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ameba.util.IOUtils.*;

/**
 * 应用程序启动配置
 *
 * @author: ICode
 * @since: 13-8-6 下午8:42
 */
@Singleton
public class Application extends ResourceConfig {
    public static final String DEFAULT_NETWORK_LISTENER_NAME = "ameba";
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private URI httpServerBaseUri;
    private String configFile;
    private Mode mode;
    private String domain;
    private String host;
    private String applicationVersion;
    private boolean ajpEnabled;
    private boolean secureEnabled;
    private boolean jmxEnabled;
    private Integer port;
    private String sslProtocol;
    private boolean sslClientMode;
    private boolean sslNeedClientAuth;
    private boolean sslWantClientAuth;
    private String sslKeyPassword;
    private byte[] sslKeyStoreFile;
    private String sslKeyStoreType;
    private String sslKeyStorePassword;
    private String sslKeyStoreProvider;
    private String sslKeyManagerFactoryAlgorithm;

    private String sslTrustPassword;
    private byte[] sslTrustStoreFile;
    private String sslTrustStorePassword;
    private String sslTrustStoreType;
    private String sslTrustStoreProvider;
    private String sslTrustManagerFactoryAlgorithm;
    private boolean sslConfigReady;
    private File sourceRoot;
    private File packageRoot;

    public Application() {
        this("conf/application.conf");
    }

    @SuppressWarnings("unchecked")
    public Application(String confFile) {

        configFile = confFile;
        logger.info("初始化...");
        //property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        //property(ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, true);
        Map<String, Object> configMap = Maps.newLinkedHashMap();

        Properties properties = new LinkedProperties();

        logger.info("读取系统默认配置...");
        //读取系统默认配置
        try {
            properties.load(getResourceAsStream("conf/default.conf"));
            //将默认配置放入临时配置对象
            configMap.putAll((Map) properties);
        } catch (Exception e) {
            logger.warn("读取[conf/default.conf]出错", e);
        }
        logger.info("读取应用自定义配置...");
        //读取应用程序配置
        Enumeration<URL> urls = IOUtils.getResources(confFile);
        while (urls.hasMoreElements()) {
            InputStream in = null;
            URL url = urls.nextElement();
            try {
                logger.info("读取[{}]文件配置", url.toExternalForm());
                in = url.openStream();
            } catch (IOException e) {
                logger.error("读取[{}]出错", url.toExternalForm());
            }
            if (in != null) {
                try {
                    properties.load(in);
                } catch (Exception e) {
                    logger.error("读取[{}]出错", url.toExternalForm());
                }
            } else {
                logger.error("读取[{}]出错", url.toExternalForm());
            }
        }

        //获取应用程序模式
        try {
            mode = Mode.valueOf(properties.getProperty("app.mode").toUpperCase());
        } catch (Exception e) {
            mode = Mode.PRODUCT;
        }

        if (mode.isDev()) {
            logger.warn("当前应用程序为开发模式");
            String sourceRootStr = System.getProperty("app.source.root");

            if (StringUtils.isNotBlank(sourceRootStr)) {
                sourceRoot = new File(sourceRootStr);
            } else {
                sourceRoot = new File("").getAbsoluteFile();
            }

            logger.info("应用源码根路径为：{}", sourceRoot.getAbsolutePath());

            logger.info("查找包根目录...");

            if (sourceRoot.exists() && sourceRoot.isDirectory()) {
                FluentIterable<File> iterable = Files.fileTreeTraverser()
                        .breadthFirstTraversal(sourceRoot);

                Pattern cp = Pattern.compile("^(\\s*(/\\*|\\*|//))");//注释正则
                Pattern p = Pattern.compile("^(\\s*package)\\s+([\\w\\.]+)\\s*;$");//包名正则
                for (File f : iterable) {
                    if (f.getName().endsWith(".java") && f.canRead()) {
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new FileReader(f));

                            String line = null;
                            while (StringUtils.isBlank(line)) {
                                line = reader.readLine();
                                //匹配注释
                                Matcher m = cp.matcher(line);
                                if (m.find()) {
                                    line = null;
                                }
                            }
                            Matcher m = p.matcher(line);

                            if (m.find()) {
                                String pkg = m.group(2);
                                String[] dirs = pkg.split("\\.");
                                ArrayUtils.reverse(dirs);
                                File pf = f.getParentFile();
                                boolean isPkg = true;
                                for (String dir : dirs) {
                                    if (!pf.getName().equals(dir)) {
                                        isPkg = false;
                                        break;
                                    }
                                    pf = pf.getParentFile();
                                }
                                if (isPkg && pf != null) {
                                    this.packageRoot = pf;
                                    break;
                                }
                            }

                        } catch (FileNotFoundException e) {
                            logger.error("find package root dir has error", e);
                        } catch (IOException e) {
                            logger.error("find package root dir has error", e);
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    logger.warn("close file input stream error", e);
                                }
                            }
                        }
                    }
                }
                if (packageRoot == null)
                    logger.warn("未找到包根目录，很多功能将失效，请确认项目内是否有Java源文件，或设置JVM参数，添加 -Dapp.source.root=${yourAppRootDir}");
                else {
                    logger.info("包根目录为:{}", packageRoot.getAbsolutePath());
                }

                ClassLoader classLoader = new ReloadingClassLoader(this);
                setClassLoader(classLoader);
                Thread.currentThread().setContextClassLoader(classLoader);
            }

            JvmAgent.initialize();
        }

        //设置ssl相关
        secureEnabled = Boolean.parseBoolean(properties.getProperty("ssl.enabled", "false"));
        sslProtocol = properties.getProperty("ssl.protocol");
        sslClientMode = Boolean.parseBoolean(properties.getProperty("ssl.clientMode", "false"));
        sslNeedClientAuth = Boolean.parseBoolean(properties.getProperty("ssl.needClientAuth", "false"));
        sslWantClientAuth = Boolean.parseBoolean(properties.getProperty("ssl.wantClientAuth", "false"));

        sslKeyManagerFactoryAlgorithm = properties.getProperty("ssl.key.manager.factory.algorithm");
        sslKeyPassword = properties.getProperty("ssl.key.password");
        sslKeyStoreProvider = properties.getProperty("ssl.key.store.provider");
        String keyStoreFile = properties.getProperty("ssl.key.store.file");
        if (StringUtils.isNotBlank(keyStoreFile))
            try {
                sslKeyStoreFile = readByteArrayFromResource(keyStoreFile);
            } catch (IOException e) {
                logger.error("读取sslKeyStoreFile出错", e);
            }
        sslKeyStoreType = properties.getProperty("ssl.key.store.type");
        sslKeyStorePassword = properties.getProperty("ssl.key.store.password");

        sslTrustManagerFactoryAlgorithm = properties.getProperty("ssl.Trust.manager.factory.algorithm");
        sslTrustPassword = properties.getProperty("ssl.trust.password");
        sslTrustStoreProvider = properties.getProperty("ssl.trust.store.provider");
        String trustStoreFile = properties.getProperty("ssl.trust.store.file");
        if (StringUtils.isNotBlank(trustStoreFile))
            try {
                sslTrustStoreFile = readByteArrayFromResource(trustStoreFile);
            } catch (IOException e) {
                logger.error("读取sslTrustStoreFile出错", e);
            }
        sslTrustStoreType = properties.getProperty("ssl.trust.store.type");
        sslTrustStorePassword = properties.getProperty("ssl.trust.store.password");

        if (secureEnabled && sslKeyStoreFile != null &&
                StringUtils.isNotBlank(sslKeyPassword) &&
                StringUtils.isNotBlank(sslKeyStorePassword)) {
            sslConfigReady = true;
        }

        ajpEnabled = Boolean.parseBoolean(properties.getProperty("http.server.ajp.enabled", "false"));
        jmxEnabled = Boolean.parseBoolean(properties.getProperty("app.jmx.enabled", "false"));

        Properties modeProperties = new Properties();

        //读取相应模式的配置文件
        try {
            modeProperties.load(getResourceAsStream("conf/" + mode.name().toLowerCase() + ".conf"));
            //将模式配置放入临时配置对象
            configMap.putAll((Map) modeProperties);
        } catch (IOException e) {
            logger.warn("读取[conf/" + mode.name().toLowerCase() + ".conf]出错", e);
        }
        //将用户配置放入临时配置对象
        if (properties.size() > 0)
            configMap.putAll((Map) properties);

        //读取jersey配置项
        Field[] declaredFields = ServerProperties.class.getDeclaredFields();
        Map<String, Field> staticFieldsMap = Maps.newLinkedHashMap();
        for (Field field : declaredFields) {
            if (Modifier.isStatic(field.getModifiers())) {
                staticFieldsMap.put(field.getName(), field);
            }
        }

        List<String> removeKeys = Lists.newArrayList();

        Map<String, Object> map = Maps.newLinkedHashMap();

        //进行jersey配置项转化
        for (String key : configMap.keySet()) {
            if (key.startsWith("app.")) {
                String name = key.substring(key.indexOf(".") + 1);
                //转化键到jersey配置
                name = name.replaceAll("\\.", "_").toUpperCase();
                Field filed = staticFieldsMap.get(name);
                if (null != filed) {
                    filed.setAccessible(true);
                    try {
                        map.put((String) filed.get(null), configMap.get(key));
                        removeKeys.add(key);
                    } catch (IllegalAccessException e) {
                        logger.error("无法获取设置的键值", e);
                    }
                }
            }
        }

        //移除转化需要的临时属性
        for (String key : removeKeys) {
            configMap.remove(key);
        }

        //将转化的jersey配置放入临时配置对象
        configMap.putAll(map);

        //将临时配置对象放入应用程序配置
        addProperties(configMap);

        //设置应用程序名称
        setApplicationName(StringUtils.defaultString((String) getProperty("app.name"), "ameba"));
        applicationVersion = (String) getProperty("app.version");

        //配置日志器
        loggerConfigure();

        String[] packages = StringUtils.deleteWhitespace(StringUtils.defaultIfBlank((String) getProperty("resource.packages"), "")).split(",");
        logger.info("设置资源扫描包:[{}]", getProperty("resource.packages"));
        packages(packages);

        if (Mode.DEV.isDev() && !isRegistered(LoggingFilter.class)) {
            logger.debug("注册日志过滤器");
            register(LoggingFilter.class);
        }

        logger.info("注册特性");

        String registerStr = StringUtils.deleteWhitespace(StringUtils.defaultIfBlank((String) getProperty("app.registers"), ""));
        String[] registers;
        int suc = 0, fail = 0, beak = 0;
        if (StringUtils.isNotBlank(registerStr)) {
            registers = registerStr.split(",");
            for (String register : registers) {
                try {
                    logger.debug("注册特性[{}]", register);
                    Class clazz = getClassLoader().loadClass(register);
                    if (isRegistered(clazz)) {
                        beak++;
                        logger.warn("并未注册特性[{}]，因为该特性已存在", register);
                        continue;
                    }
                    register(clazz);
                    suc++;
                } catch (ClassNotFoundException e) {
                    fail++;
                    if (!register.startsWith("default."))
                        logger.error("获取特性失败", e);
                    else
                        logger.warn("未找到系统默认特性[" + register + "]", e);
                }
            }
        }


        for (String key : configMap.keySet()) {
            if (key.startsWith("app.register.")) {
                String className = (String) getProperty(key);
                if (StringUtils.isNotBlank(className)) {
                    String name = key.replaceFirst("^app\\.register\\.", "");
                    try {
                        logger.debug("注册特性[{}({})]", name, className);
                        Class clazz = getClassLoader().loadClass(className);
                        if (isRegistered(clazz)) {
                            beak++;
                            logger.warn("并未注册装特性[{}({})]，因为该特性已存在", name, clazz);
                            continue;
                        }
                        register(clazz);
                        suc++;
                    } catch (ClassNotFoundException e) {
                        fail++;
                        if (!name.startsWith("default."))
                            logger.error("获取特性失败", e);
                        else
                            logger.warn("未找到系统默认特性[" + className + "]", e);
                    }
                }
            }
        }
        logger.info("成功注册{}个特性，失败{}个，跳过{}个", suc, fail, beak);

        //清空转化的配置
        map.clear();
        map = null;
        //清空临时配置
        configMap.clear();
        configMap = null;

        //清空临时读取的配置
        properties.clear();
        properties = null;
        //清空应用程序开发模式配置
        modeProperties.clear();
        modeProperties = null;

        this.domain = (String) getProperty("app.domain");

        this.host = StringUtils.defaultIfBlank((String) getProperty("app.host"), "0.0.0.0");

        this.port = Integer.valueOf(StringUtils.defaultIfBlank((String) getProperty("app.port"), "80"));

        register(new ApplicationProvider(this));


        if (isRegistered(EnhanceModelFeature.class)) {
            register(EnhanceModelFeature.Do.class, Integer.MIN_VALUE);
        }

        //config server base uri
        httpServerBaseUri = URI.create("http" + (isSecureEnabled() ? "s" : "") + "://" + this.host
                + ":" + this.port + "/");
        logger.info("配置服务器监听地址绑定到[{}]", httpServerBaseUri);
        logger.info("装载特性...");
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
                app.httpServerBaseUri,
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
        serverConfiguration.setName("HttpServer-" + app.getApplicationName());

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
     * @param secure                used for call {@link NetworkListener#setSecure(boolean)}.
     * @param ajpEnabled            used for call {@link NetworkListener#registerAddOn(org.glassfish.grizzly.http.server.AddOn)}
     *                              {@link org.glassfish.grizzly.spdy.SpdyAddOn}.
     * @param jmxEnabled            {@link org.glassfish.grizzly.http.server.ServerConfiguration#setJmxEnabled(boolean)}.
     * @param sslEngineConfigurator Ssl settings to be passed to {@link NetworkListener#setSSLEngineConfig(org.glassfish.grizzly.ssl.SSLEngineConfigurator)}.
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
     * @param secure                used for call {@link NetworkListener#setSecure(boolean)}.
     * @param ajpEnabled            used for call {@link NetworkListener#registerAddOn(org.glassfish.grizzly.http.server.AddOn)}
     *                              {@link org.glassfish.grizzly.spdy.SpdyAddOn}.
     * @param jmxEnabled            {@link org.glassfish.grizzly.http.server.ServerConfiguration#setJmxEnabled(boolean)}.
     * @param sslEngineConfigurator Ssl settings to be passed to {@link NetworkListener#setSSLEngineConfig(org.glassfish.grizzly.ssl.SSLEngineConfigurator)}.
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
                throw new ProcessingException("IOException thrown when trying to start grizzly server", ex);
            }
        }

        return server;
    }

    public File getPackageRoot() {
        return packageRoot;
    }

    public String getConfigFile() {
        return configFile;
    }

    public File getSourceRoot() {
        return sourceRoot;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getDomain() {
        return domain;
    }

    public Mode getMode() {
        return mode;
    }

    public URI getHttpServerBaseUri() {
        return httpServerBaseUri;
    }

    public boolean isSecureEnabled() {
        return secureEnabled;
    }

    public boolean isSslClientMode() {
        return sslClientMode;
    }

    public boolean isSslNeedClientAuth() {
        return sslNeedClientAuth;
    }

    public boolean isSslWantClientAuth() {
        return sslWantClientAuth;
    }

    public String getSslKeyPassword() {
        return sslKeyPassword;
    }

    public byte[] getSslKeyStoreFile() {
        return sslKeyStoreFile;
    }

    public String getSslKeyStoreType() {
        return sslKeyStoreType;
    }

    public String getSslKeyStorePassword() {
        return sslKeyStorePassword;
    }

    public String getSslTrustPassword() {
        return sslTrustPassword;
    }

    public byte[] getSslTrustStoreFile() {
        return sslTrustStoreFile;
    }

    public String getSslTrustStorePassword() {
        return sslTrustStorePassword;
    }

    public String getSslTrustStoreType() {
        return sslTrustStoreType;
    }

    public boolean isSslConfigReady() {
        return sslConfigReady;
    }

    public String getSslProtocol() {
        return sslProtocol;
    }

    public String getSslKeyStoreProvider() {
        return sslKeyStoreProvider;
    }

    public String getSslTrustStoreProvider() {
        return sslTrustStoreProvider;
    }

    public String getSslKeyManagerFactoryAlgorithm() {
        return sslKeyManagerFactoryAlgorithm;
    }

    public String getSslTrustManagerFactoryAlgorithm() {
        return sslTrustManagerFactoryAlgorithm;
    }

    public boolean isAjpEnabled() {
        return ajpEnabled;
    }

    public boolean isJmxEnabled() {
        return jmxEnabled;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    /**
     * 设置日志器
     */
    private void loggerConfigure() {
        //set logback config file
        URL loggerConfigFile = getResource(StringUtils.defaultIfBlank((String) getProperty("logger.config.file"), "conf/logback.groovy"));

        if (loggerConfigFile == null) {
            loggerConfigFile = getResource("conf/logback-" + getMode().name().toLowerCase() + ".groovy");
        }

        if (loggerConfigFile != null) {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.reset();
            context.putProperty("appName", getApplicationName());
            String appPackage = (String) getProperty("app.package");
            context.putProperty("appPackage", appPackage);
            GafferUtil.runGafferConfiguratorOn(context, this, loggerConfigFile);
        }

        //java.util.logging.Logger proxy
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
        SLF4JBridgeHandler.install();
        rootLogger.setLevel(Level.ALL);
    }

    public enum Mode {
        DEV, PRODUCT;

        public boolean isDev() {
            return this == DEV;
        }

        public boolean isProd() {
            return this == PRODUCT;
        }
    }

    private class ApplicationProvider extends AbstractBinder implements Factory<Application> {

        private Application application;

        public ApplicationProvider(Application application) {
            this.application = application;
        }

        @Override
        protected void configure() {
            bindFactory(this).to(Application.class);
        }

        @Override
        public Application provide() {
            return application;
        }

        @Override
        public void dispose(Application instance) {
        }
    }
}
