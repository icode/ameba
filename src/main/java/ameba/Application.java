package ameba;

import ameba.container.server.Connector;
import ameba.event.Event;
import ameba.event.SystemEventBus;
import ameba.exceptions.AmebaException;
import ameba.exceptions.ConfigErrorException;
import ameba.feature.AmebaFeature;
import ameba.util.IOUtils;
import ameba.util.LinkedProperties;
import ameba.util.Times;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

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
    private static final String REGISTER_CONF_PREFIX = "app.register.";
    private static final String JERSEY_CONF_NAME_PREFIX = "app.sys.core.";
    private static String INFO_SPLITOR = "---------------------------------------------------";
    protected boolean jmxEnabled;
    private String configFile;
    private Mode mode;
    private CharSequence applicationVersion;
    private File sourceRoot;
    private File packageRoot;
    private Container container;
    private long timestamp = System.currentTimeMillis();

    public Application() {
        this("conf/application.conf");
    }

    @SuppressWarnings("unchecked")
    public Application(String confFile) {

        if (Ameba.getApp() != null) {
            throw new AmebaException("已经存在一个应用实例");
        }

        configFile = confFile;
        logger.trace("初始化...");
        Map<String, Object> configMap = Maps.newLinkedHashMap();

        Properties properties = new LinkedProperties();

        logger.trace("读取系统默认配置...");
        //读取系统默认配置
        try {
            properties.load(getResourceAsStream("conf/default.conf"));
            //将默认配置放入临时配置对象,占坑(index),不清除内存,防止module替换默认配置,允许application.conf替换
            configMap.putAll((Map) properties);
        } catch (Exception e) {
            logger.warn("读取[conf/default.conf]出错", e);
        }
        logger.trace("读取应用自定义配置...");
        //读取应用程序配置
        URL appCfgUrl = readAppConfig(properties, confFile);

        //获取应用程序模式
        try {
            mode = Mode.valueOf(properties.getProperty("app.mode").toUpperCase());
        } catch (Exception e) {
            mode = Mode.PRODUCT;
        }

        //设置应用程序名称
        setApplicationName(StringUtils.defaultString(properties.getProperty("app.name"), "ameba"));
        applicationVersion = properties.getProperty("app.version");
        if (StringUtils.isBlank(applicationVersion)) {
            applicationVersion = new UnknownVersion();
        }

        //配置日志器
        configureLogger(properties);

        Ameba.printInfo();

        logger.info("初始化...");
        logger.info("应用配置文件 {}", toExternalForm(appCfgUrl));

        AmebaFeature.preConfigure(this);

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

        //配置资源
        configureResource(configMap);

        //配置特性
        configureFeature(configMap);

        //配置服务器相关
        configureServer(properties);

        //清空临时配置
        configMap.clear();
        configMap = null;

        //清空临时读取的配置
        properties.clear();
        properties = null;

        publishEvent(new ConfiguredEvent(this));
        logger.info("装载特性...");
    }

    private static void publishEvent(Event event) {
        SystemEventBus.publish(event);
        AmebaFeature.getEventBus().publish(event);
    }

    public long getTimestamp() {
        return timestamp;
    }

    @SuppressWarnings("unchecked")
    private void preConfigureFeature(Class clazz) {
        if (AmebaFeature.class.isAssignableFrom(clazz)) {
            try {
                Method m = clazz.getMethod("preConfigure", Application.class);
                if (Modifier.isStatic(m.getModifiers())) {
                    m.invoke(null, Application.this);
                }
            } catch (IllegalAccessException e) {
                logger.warn("前期初始化特性出错[" + clazz.getName() + "]", e);
            } catch (InvocationTargetException e) {
                logger.warn("前期初始化特性出错[" + clazz.getName() + "]", e);
            } catch (NoSuchMethodException e) {
                logger.trace(clazz.getName() + " 类未发现需要前期配置项");
            }
        }
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

                    preConfigureFeature(clazz);

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
            if (key.startsWith(REGISTER_CONF_PREFIX)) {
                String className = (String) getProperty(key);
                if (StringUtils.isNotBlank(className)) {
                    String name = key.substring(REGISTER_CONF_PREFIX.length());
                    try {
                        logger.debug("注册特性[{}({})]", name, className);
                        Class clazz = getClassLoader().loadClass(className);
                        if (isRegistered(clazz)) {
                            beak++;
                            logger.warn("并未注册装特性[{}({})]，因为该特性已存在", name, clazz);
                            continue;
                        }
                        preConfigureFeature(clazz);
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
                String pkgStr = (String) configMap.get(key);
                if (StringUtils.isNotBlank(pkgStr)) {
                    String[] pkgs = StringUtils.deleteWhitespace(pkgStr).split(",");
                    for (String pkg : pkgs) {
                        if (!ArrayUtils.contains(packages, pkg))
                            packages = ArrayUtils.add(packages, pkg);
                    }
                }
            }
        }
        packages = ArrayUtils.removeElement(packages, "");
        logger.info("设置资源扫描包:{}", StringUtils.join(packages, ","));
        registerFinder(new PackageNamesScanner(getClassLoader(), packages, true));
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
            if (key.startsWith(JERSEY_CONF_NAME_PREFIX)) {
                String name = key.substring(JERSEY_CONF_NAME_PREFIX.length());
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
        Properties modeProperties = new LinkedProperties();

        //读取相应模式的配置文件
        Enumeration<java.net.URL> confs = IOUtils.getResources("conf/" + mode.name().toLowerCase() + ".conf");
        while (confs.hasMoreElements()) {
            InputStream in = null;
            try {
                in = confs.nextElement().openStream();
                modeProperties.load(in);
            } catch (IOException e) {
                logger.warn("读取[conf/" + mode.name().toLowerCase() + ".conf]出错", e);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
        if (modeProperties.size() > 0)
            //将模式配置放入临时配置对象
            configMap.putAll((Map) modeProperties);

        //清空应用程序模式配置
        modeProperties.clear();
        modeProperties = null;
    }

    private void configureServer(Properties properties) {
        jmxEnabled = Boolean.parseBoolean(properties.getProperty("app.jmx.enabled"));
        if (jmxEnabled && properties.getProperty(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED) == null)
            property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, jmxEnabled);
        registerInstances(new ContainerLifecycleListener() {
            @Override
            public void onStartup(Container container) {
                publishEvent(new ContainerStartupEvent(container, Application.this));

                if (Application.this.container == null) {
                    Runtime r = Runtime.getRuntime();
                    r.gc();

                    String startUsedTime = Times.toDuration(System.currentTimeMillis() - timestamp);

                    StringBuilder builder = new StringBuilder();
                    builder
                            .append("Ameba版本  >   ")
                            .append(Ameba.getVersion())
                            .append("\n")
                            .append("HTTP容器   >   ")
                            .append(StringUtils.defaultString(Ameba.getContainer().getType(), "Unknown"))
                            .append("\n")
                            .append("启动用时    >   ")
                            .append(startUsedTime)
                            .append("\n")
                            .append("应用名称    >   ")
                            .append(getApplicationName())
                            .append("\n")
                            .append("应用版本    >   ")
                            .append(getApplicationVersion())
                            .append("\n")
                            .append("内存使用    >   ")
                            .append(FileUtils.byteCountToDisplaySize((r.totalMemory() - r.freeMemory())))
                            .append("/")
                            .append(FileUtils.byteCountToDisplaySize(r.maxMemory()))
                            .append("\n")
                            .append("启用JMX    >   ")
                            .append(isJmxEnabled())
                            .append("\n")
                            .append("应用模式    >   ")
                            .append(getMode());

                    List<Connector> connectors = getConnectors();
                    if (connectors != null && connectors.size() > 0) {
                        builder.append("\n")
                                .append("监听地址    >   ");
                        for (Connector connector : connectors) {
                            builder.append("\n             ")
                                    .append(connector.getHttpServerBaseUri());
                        }
                    }

                    logger.info("应用容器已启动\n{}\n{}\n{}",
                            INFO_SPLITOR,
                            builder,
                            INFO_SPLITOR);
                }


                Application.this.container = container;
            }

            @Override
            public void onReload(Container container) {
                publishEvent(new ContainerReloadEvent(container, Application.this));
                logger.trace("应用容器重新加载");
            }

            @Override
            public void onShutdown(Container container) {
                publishEvent(new ContainerShutdownEvent(container, Application.this));
                logger.trace("应用容器已关闭");
            }
        });
    }

    private String toExternalForm(URL url) {
        try {
            return URLDecoder.decode(url.toExternalForm(), Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException e) {
            return url.toExternalForm();
        }
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

                    int fileIndex = modelName.lastIndexOf("/");
                    modelName = modelName.substring(fileIndex + 1);

                    logger.info("加载模块 {}", modelName);
                    logger.debug("读取[{}]文件配置", toExternalForm(url));
                    in = url.openStream();
                } catch (IOException e) {
                    logger.error("读取[{}]出错", toExternalForm(url));
                }
                if (in != null) {
                    try {
                        moduleProperties.load(in);
                    } catch (Exception e) {
                        logger.error("读取[{}]出错", toExternalForm(url));
                    }
                } else {
                    logger.error("读取[{}]出错", toExternalForm(url));
                }
                closeQuietly(in);
            }
            configMap.putAll((Map) moduleProperties);
            moduleProperties.clear();
            moduleProperties = null;
        } else {
            logger.info("未找到附加模块");
        }
    }

    private URL readAppConfig(Properties properties, String confFile) {
        Enumeration<URL> urls = IOUtils.getResources(confFile);
        URL url = null;
        if (urls.hasMoreElements()) {
            InputStream in = null;
            url = urls.nextElement();
            try {
                logger.trace("读取[{}]文件配置", toExternalForm(url));
                in = url.openStream();
            } catch (IOException e) {
                logger.error("读取[{}]出错", toExternalForm(url));
            }
            if (in != null) {
                try {
                    properties.load(in);
                } catch (Exception e) {
                    logger.error("读取[{}]出错", toExternalForm(url));
                }
            } else {
                logger.error("读取[{}]出错", toExternalForm(url));
            }
            closeQuietly(in);

            if (urls.hasMoreElements()) {
                List<String> urlList = Lists.newArrayList(toExternalForm(url));
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
        return url;
    }

    public void reload() {
        publishEvent(new ContainerBeginReloadEvent(container, this));
        container.reload();
    }

    public void reload(ResourceConfig configuration) {
        container.reload(configuration);
    }

    public File getPackageRoot() {
        return packageRoot;
    }

    public void setPackageRoot(File packageRoot) {
        this.packageRoot = packageRoot;
    }

    public String getConfigFile() {
        return configFile;
    }

    public File getSourceRoot() {
        return sourceRoot;
    }

    public void setSourceRoot(File sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    public Mode getMode() {
        return mode;
    }

    public CharSequence getApplicationVersion() {
        return applicationVersion;
    }

    public List<Connector> getConnectors() {
        return Ameba.getContainer().getConnectors();
    }

    public boolean isJmxEnabled() {
        return jmxEnabled;
    }

    /**
     * 设置日志器
     */
    private void configureLogger(Properties properties) {
        //set logback config file
        URL loggerConfigFile = getResource(StringUtils.defaultIfBlank((String) properties.getProperty("logger.config.file"), "conf/logback.groovy"));

        if (loggerConfigFile == null) {
            loggerConfigFile = getResource("conf/logback-" + getMode().name().toLowerCase() + ".groovy");
        }

        if (loggerConfigFile != null) {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.reset();
            context.putProperty("appName", getApplicationName());
            String appPackage = (String) properties.getProperty("app.package");
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

    private static class ContainerEvent extends Event {
        private Container container;
        private Application app;

        public ContainerEvent(Container container, Application app) {
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

    public static class ContainerStartupEvent extends ContainerEvent {
        public ContainerStartupEvent(Container container, Application app) {
            super(container, app);
        }
    }

    public static class ContainerReloadEvent extends ContainerEvent {
        public ContainerReloadEvent(Container container, Application app) {
            super(container, app);
        }
    }

    public static class ContainerBeginReloadEvent extends ContainerEvent {
        public ContainerBeginReloadEvent(Container container, Application app) {
            super(container, app);
        }
    }

    public static class ContainerShutdownEvent extends ContainerEvent {
        public ContainerShutdownEvent(Container container, Application app) {
            super(container, app);
        }
    }

    public class UnknownVersion implements CharSequence {

        String version = "Unknown";

        @Override
        public int length() {
            return version.length();
        }

        @Override
        public char charAt(int index) {
            return version.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return version.subSequence(start, end);
        }

        @Override
        public String toString() {
            return version;
        }
    }
}
