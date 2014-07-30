package ameba;

import ameba.db.model.EnhanceModelFeature;
import ameba.dev.JvmAgent;
import ameba.dev.ReloadingClassLoader;
import ameba.dev.ReloadingFilter;
import ameba.event.Event;
import ameba.event.SystemEventBus;
import ameba.exceptions.AmebaException;
import ameba.exceptions.ConfigErrorException;
import ameba.feature.AmebaFeature;
import ameba.util.IOUtils;
import ameba.util.LinkedProperties;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferUtil;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ameba.util.IOUtils.*;

/**
 * 应用程序启动配置
 *
 * @author ICode
 * @since 13-8-6 下午8:42
 */
@Singleton
public class Application extends ResourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^(\\s*(/\\*|\\*|//))");//注释正则
    private static final Pattern PKG_PATTERN = Pattern.compile("^(\\s*package)\\s+([_a-zA-Z][_a-zA-Z0-9\\.]+)\\s*;$");//包名正则
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
    private Container container;

    public Application() {
        this("conf/application.conf");
    }

    @SuppressWarnings("unchecked")
    public Application(String confFile) {

        if (Ameba.getApp() != null) {
            throw new AmebaException("已经存在一个应用实例");
        }

        configFile = confFile;
        logger.info("初始化...");
        Map<String, Object> configMap = Maps.newLinkedHashMap();

        Properties properties = new LinkedProperties();

        logger.info("读取系统默认配置...");
        //读取系统默认配置
        try {
            properties.load(getResourceAsStream("conf/default.conf"));
            //将默认配置放入临时配置对象,占坑(index),不清除内存,防止module替换默认配置,允许application.conf替换
            configMap.putAll((Map) properties);
        } catch (Exception e) {
            logger.warn("读取[conf/default.conf]出错", e);
        }
        logger.info("读取应用自定义配置...");
        //读取应用程序配置
        readAppConfig(properties, confFile);

        //获取应用程序模式
        try {
            mode = Mode.valueOf(properties.getProperty("app.mode").toUpperCase());
        } catch (Exception e) {
            mode = Mode.PRODUCT;
        }

        if (mode.isDev()) {
            configureDev();
        }

        //设置ssl相关
        configureSsl(properties);

        //读取模式配置
        readModeConfig(configMap);

        //读取模块配置
        readModuleConfig(configMap);

        //将用户配置放入临时配置对象
        if (properties.size() > 0)
            configMap.putAll((Map) properties);

        //转换jersey配置项
        convertJerseyConfig(configMap);

        //将临时配置对象放入应用程序配置
        addProperties(configMap);

        //设置应用程序名称
        setApplicationName(StringUtils.defaultString((String) getProperty("app.name"), "ameba"));
        applicationVersion = (String) getProperty("app.version");

        //配置日志器
        configureLogger();

        //配置资源
        configureResource(configMap);

        //配置特性
        configureFeature(configMap);

        //设置服务器相关
        configureServer(properties);

        //清空临时配置
        configMap.clear();
        configMap = null;

        //清空临时读取的配置
        properties.clear();
        properties = null;
    }

    private static void publishEvent(Event event) {
        SystemEventBus.publish(event);
        AmebaFeature.getEventBus().publish(event);
    }

    private void configureFeature(Map<String, Object> configMap) {
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
    }

    private void configureResource(Map<String, Object> configMap) {
        String[] packages = StringUtils.deleteWhitespace(StringUtils.defaultIfBlank((String) getProperty("resource.packages"), "")).split(",");
        for (String key : configMap.keySet()) {
            if (key.startsWith("resource.packages.")) {
                String pkg = StringUtils.deleteWhitespace(StringUtils.defaultIfBlank((String) configMap.get(key), ""));
                if (!"".equals(pkg) && Arrays.binarySearch(packages, pkg) == -1)
                    packages = ArrayUtils.addAll(packages, pkg.split(","));
            }
        }
        logger.info("设置资源扫描包:{}", StringUtils.join(packages, ","));
        registerFinder(new PackageNamesScanner(getClassLoader(), packages, true));

        if (Mode.DEV.isDev() && !isRegistered(LoggingFilter.class)) {
            logger.debug("注册日志过滤器");
            register(LoggingFilter.class);
        }
    }

    private void convertJerseyConfig(Map<String, Object> configMap) {
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

        //清空转化的配置
        map.clear();
        map = null;
    }

    @SuppressWarnings("unchecked")
    private void readModeConfig(Map<String, Object> configMap) {
        Properties modeProperties = new Properties();

        //读取相应模式的配置文件
        try {
            modeProperties.load(getResourceAsStream("conf/" + mode.name().toLowerCase() + ".conf"));
            //将模式配置放入临时配置对象
            configMap.putAll((Map) modeProperties);
        } catch (IOException e) {
            logger.warn("读取[conf/" + mode.name().toLowerCase() + ".conf]出错", e);
        }


        //清空应用程序开发模式配置
        modeProperties.clear();
        modeProperties = null;
    }

    private void configureSsl(Properties properties) {
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
    }

    private void configureServer(Properties properties) {
        ajpEnabled = Boolean.parseBoolean(properties.getProperty("http.server.ajp.enabled", "false"));
        jmxEnabled = Boolean.parseBoolean(properties.getProperty("app.jmx.enabled", "false"));

        domain = (String) getProperty("app.domain");

        host = StringUtils.defaultIfBlank((String) getProperty("app.host"), "0.0.0.0");

        port = Integer.valueOf(StringUtils.defaultIfBlank((String) getProperty("app.port"), "80"));

        if (isRegistered(EnhanceModelFeature.class)) {
            register(EnhanceModelFeature.Do.class, Integer.MIN_VALUE);
        }

        //config server base uri
        httpServerBaseUri = URI.create("http" + (isSecureEnabled() ? "s" : "") + "://" + host
                + ":" + port + "/");
        logger.info("配置服务器监听地址绑定到[{}]", httpServerBaseUri);
        logger.info("装载特性...");
        registerInstances(new ContainerLifecycleListener() {
            @Override
            public void onStartup(Container container) {
                Application.this.container = container;
                publishEvent(new ContainerStartupEvent(container, Application.this));
                logger.info("容器已启动");
            }

            @Override
            public void onReload(Container container) {
                publishEvent(new ContainerShutdownEvent(container, Application.this));
                logger.info("容器重新加载");
            }

            @Override
            public void onShutdown(Container container) {
                publishEvent(new ContainerShutdownEvent(container, Application.this));
                logger.info("容器已关闭");
            }
        });

        SystemEventBus.publish(new ConfiguredEvent(this));

        registerInstances(new Feature() {
            @Override
            public boolean configure(FeatureContext context) {
                return true;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void readModuleConfig(Map<String, Object> configMap) {
        logger.info("读取模块配置...");
        //读取模块配置
        Enumeration<URL> moduleUrls = IOUtils.getResources("conf/module.conf");
        Properties moduleProperties = new LinkedProperties();
        if (moduleUrls.hasMoreElements()) {
            while (moduleUrls.hasMoreElements()) {
                InputStream in = null;
                URL url = moduleUrls.nextElement();
                try {
                    String modelName = url.getFile();
                    int jarFileIndex = modelName.lastIndexOf("!");
                    if (jarFileIndex != -1) {
                        modelName = modelName.substring(0, jarFileIndex);
                    }

                    jarFileIndex = modelName.lastIndexOf(".");
                    if (jarFileIndex != -1) {
                        modelName = modelName.substring(0, jarFileIndex);
                    }

                    int fileIndex = modelName.lastIndexOf(File.separator);
                    modelName = modelName.substring(fileIndex + 1);

                    logger.info("加载模块 {}", modelName);
                    logger.debug("读取[{}]文件配置", url.toExternalForm());
                    in = url.openStream();
                } catch (IOException e) {
                    logger.error("读取[{}]出错", url.toExternalForm());
                }
                if (in != null) {
                    try {
                        moduleProperties.load(in);
                    } catch (Exception e) {
                        logger.error("读取[{}]出错", url.toExternalForm());
                    }
                } else {
                    logger.error("读取[{}]出错", url.toExternalForm());
                }
            }
            configMap.putAll((Map) moduleProperties);
            moduleProperties.clear();
            moduleProperties = null;
        } else {
            logger.info("未找到附加模块");
        }
    }

    private void configureDev() {
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
            searchPackageRoot();
            if (packageRoot == null) {
                logger.info("未找到包根目录，很多功能将失效，请确认项目内是否有Java源文件，如果确实存在Java源文件，" +
                        "请设置项目根目录的JVM参数，添加 -Dapp.source.root=${yourAppRootDir}");
                logger.debug("打开文件监听，寻找包根目录...");
                long interval = TimeUnit.SECONDS.toMillis(4);
                final FileAlterationObserver observer = new FileAlterationObserver(
                        sourceRoot,
                        FileFilterUtils.and(
                                FileFilterUtils.fileFileFilter(),
                                FileFilterUtils.suffixFileFilter(".java")));
                final FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
                observer.addListener(new FileAlterationListenerAdaptor() {
                    @Override
                    public void onFileCreate(File pFile) {
                        if (pFile.getName().endsWith(".java") && pFile.canRead()) {
                            if (searchPackageRoot(pFile)) {
                                logger.debug("找到包根目录为：{}，退出监听。", pFile.getAbsolutePath());
                                try {
                                    monitor.stop();
                                } catch (Exception e) {
                                    logger.info("停止监控目录发生错误", e);
                                }
                            }
                        }
                    }
                });
                try {
                    monitor.start();
                } catch (Exception e) {
                    logger.info("监控目录发生错误", e);
                }
            } else {
                logger.info("包根目录为:{}", packageRoot.getAbsolutePath());
            }
        } else {
            logger.info("未找到项目根目录，很多功能将失效，请设置项JVM参数，添加 -Dapp.source.root=${yourAppRootDir}");
        }

        final ClassLoader classLoader = new ReloadingClassLoader(this);
        setClassLoader(classLoader);
        Thread.currentThread().setContextClassLoader(classLoader);

        JvmAgent.initialize();
        logger.info("注册热加载过滤器");
        register(ReloadingFilter.class);
    }

    private void readAppConfig(Properties properties, String confFile) {
        Enumeration<URL> urls = IOUtils.getResources(confFile);
        if (urls.hasMoreElements()) {
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

            if (urls.hasMoreElements()) {
                List<String> urlList = Lists.newArrayList(url.toExternalForm());
                while (urls.hasMoreElements()) {
                    urlList.add(urls.nextElement().toExternalForm());
                }
                String errorMsg = "存在多个程序配置,请使用唯一的程序配置文件:\n" + StringUtils.join(urlList, "\n");
                logger.error(errorMsg);
                throw new ConfigErrorException(errorMsg);
            }
        } else {
            logger.warn("未找到{}文件,请何实", confFile);
        }
    }

    public void reload() {
        container.reload();
    }

    public void reload(ResourceConfig configuration) {
        container.reload(configuration);
    }

    public boolean searchPackageRoot(File f) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(f));

            String line = null;
            while (StringUtils.isBlank(line)) {
                line = reader.readLine();
                //匹配注释
                Matcher m = COMMENT_PATTERN.matcher(line);
                if (m.find()) {
                    line = null;
                }
            }
            if (line == null) return false;
            Matcher m = PKG_PATTERN.matcher(line);

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
                    return true;
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
        return false;
    }

    public void searchPackageRoot() {
        FluentIterable<File> iterable = Files.fileTreeTraverser()
                .breadthFirstTraversal(sourceRoot);
        for (File f : iterable) {
            if (f.getName().endsWith(".java") && f.canRead()) {
                if (searchPackageRoot(f)) {
                    break;
                }
            }
        }
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
    private void configureLogger() {
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
        DEV, PRODUCT, TEST;

        public boolean isDev() {
            return this == DEV;
        }

        public boolean isProd() {
            return this == PRODUCT;
        }

        public boolean isTest() {
            return this == TEST;
        }
    }

    public static class ConfiguredEvent extends Event {
        private Application app;

        public ConfiguredEvent(Application app) {
            this.app = app;
        }

        public Application getApp() {
            return app;
        }
    }

    public static class ContainerStartupEvent extends Event {
        private Container container;
        private Application app;

        public ContainerStartupEvent(Container container, Application app) {
            this.container = container;
            this.app = app;
        }

        Container getContainer() {
            return container;
        }

        public Application getApp() {
            return app;
        }
    }

    public static class ContainerReloadEvent extends Event {
        private Container container;
        private Application app;

        public ContainerReloadEvent(Container container, Application app) {
            this.container = container;
            this.app = app;
        }

        Container getContainer() {
            return container;
        }

        public Application getApp() {
            return app;
        }
    }

    public static class ContainerShutdownEvent extends Event {
        Container container;
        Application app;

        public ContainerShutdownEvent(Container container, Application app) {
            this.container = container;
            this.app = app;
        }

        Container getContainer() {
            return container;
        }

        public Application getApp() {
            return app;
        }
    }
}
