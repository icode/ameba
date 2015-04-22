package ameba.core;

import ameba.Ameba;
import ameba.container.Container;
import ameba.container.server.Connector;
import ameba.event.Listener;
import ameba.event.SystemEventBus;
import ameba.exception.AmebaException;
import ameba.exception.ConfigErrorException;
import ameba.feature.AmebaFeature;
import ameba.lib.InitializationLogger;
import ameba.util.ClassUtils;
import ameba.util.IOUtils;
import ameba.util.LinkedProperties;
import ameba.util.Times;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.internal.OsgiRegistry;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.model.ContractProvider;
import org.glassfish.jersey.server.*;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Feature;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
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
public class Application {
    /**
     * Constant <code>DEFAULT_APP_NAME="Ameba"</code>
     */
    public static final String DEFAULT_APP_NAME = "Ameba";
    /**
     * Constant <code>DEFAULT_APP_CONF="conf/application.conf"</code>
     */
    public static final String DEFAULT_APP_CONF = "conf/application.conf";
    private static final String REGISTER_CONF_PREFIX = "app.register.";
    private static final String ADDON_CONF_PREFIX = "app.addon.";
    private static final String JERSEY_CONF_NAME_PREFIX = "app.sys.core.";
    private static final String SCAN_CLASSES_CACHE_FILE = IOUtils.getResource("/").getPath() + "conf/classes.list";
    private static InitializationLogger logger;
    private static String INFO_SPLITOR = "---------------------------------------------------";
    protected boolean jmxEnabled;
    private String[] configFiles;
    private long timestamp = System.currentTimeMillis();
    private boolean initialized = false;
    private Mode mode;
    private CharSequence applicationVersion;
    private File sourceRoot;
    private File packageRoot;
    private Container container;
    private Set<AddOn> addOns;
    private ResourceConfig config;
    private Set<String> scanPkgs;

    /**
     * <p>Constructor for Application.</p>
     */
    public Application() {
        this(DEFAULT_APP_CONF);
    }

    /**
     * <p>Constructor for Application.</p>
     *
     * @param confFile a {@link java.lang.String} object.
     */
    public Application(String... confFile) {

        if (Ameba.getApp() != null) {
            throw new AmebaException("已经存在一个应用实例");
        }

        logger = new InitializationLogger(Application.class, this);

        configFiles = confFile;

        configure();
    }

    public void reconfigure() {
        configure();
    }

    private void configure() {

        config = new ResourceConfig();

        Map<String, Object> configMap = Maps.newLinkedHashMap();
        Properties properties = new LinkedProperties();

        //读取系统默认配置
        try {
            properties.load(getResourceAsStream("conf/default.conf"));
            //将默认配置放入临时配置对象,占坑(index),不清除内存,防止module替换默认配置,允许application.conf替换
            configMap.putAll((Map) properties);
        } catch (Exception e) {
            logger.warn("读取[conf/default.conf]出错", e);
        }

        List<String> appConf = Lists.newArrayListWithExpectedSize(configFiles.length);
        for (String conf : configFiles) {
            //读取应用程序配置
            URL appCfgUrl = readAppConfig(properties, conf);
            appConf.add(toExternalForm(appCfgUrl));
        }
        //获取应用程序模式
        try {
            mode = Mode.valueOf(properties.getProperty("app.mode").toUpperCase());
        } catch (Exception e) {
            mode = Mode.PRODUCT;
        }

        //设置应用程序名称
        setApplicationName(StringUtils.defaultString(properties.getProperty("app.name"), DEFAULT_APP_NAME));
        applicationVersion = properties.getProperty("app.version");
        if (StringUtils.isBlank(applicationVersion)) {
            applicationVersion = new UnknownVersion();
        }

        //配置日志器
        configureLogger(properties);

        if (!isInitialized())
            Ameba.printInfo();

        logger.info("初始化...");
        logger.info("应用配置文件 {}", appConf);

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

        registerInstance();

        register(Requests.BindRequest.class);

        addOnSetup(configMap);

        //配置资源
        configureResource();

        //配置特性
        configureFeature(configMap);

        //配置服务器相关
        configureServer();

        //清空临时配置
        configMap.clear();

        //清空临时读取的配置
        properties.clear();

        scanClasses();

        addOnDone();

        logger.info("装载特性...");
    }

    private void scanClasses() {
        URL cacheList = IOUtils.getResource(SCAN_CLASSES_CACHE_FILE);
        if (cacheList == null || getMode().isDev()) {
            logger.debug("scan files ...");
            final PackageNamesScanner scanner = new PackageNamesScanner(
                    scanPkgs.toArray(new String[scanPkgs.size()]), true);
            Set<String> foundClasses = Sets.newHashSet();
            List<String> acceptClasses = Lists.newArrayList();
            while (scanner.hasNext()) {
                ClassFoundEvent.ClassInfo info = new ClassFoundEvent.ClassInfo() {

                    InputStream in;

                    @Override
                    public InputStream getFileStream() {
                        if (in == null) {
                            in = scanner.open();
                        }
                        return in;
                    }

                    @Override
                    public void closeFileStream() {
                        closeQuietly(in);
                    }
                };
                info.fileName = scanner.next();
                String className = info.getCtClass().getName();
                if (!foundClasses.contains(className)) {
                    ClassFoundEvent event = new ClassFoundEvent(info);
                    SystemEventBus.publish(event);
                    info.closeFileStream();

                    if (event.accept) {
                        acceptClasses.add(className);
                    }
                }
                foundClasses.add(className);
            }
            foundClasses.clear();

            if (getMode().isDev()) return;

            OutputStream out = null;
            try {
                File cacheFile = new File(SCAN_CLASSES_CACHE_FILE);

                if (cacheFile.isDirectory()) {
                    FileUtils.deleteQuietly(cacheFile);
                }

                out = FileUtils.openOutputStream(cacheFile);

                IOUtils.writeLines(acceptClasses, null, out);
            } catch (FileNotFoundException e) {
                logger.error("write class cache file error", e);
            } catch (IOException e) {
                logger.error("write class cache file error", e);
            } finally {
                acceptClasses.clear();
                closeQuietly(out);
            }
        } else {
            logger.debug("read files from scan cache ...");
            InputStream in = null;
            try {
                in = cacheList.openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                if (reader.ready()) {
                    String className = reader.readLine();
                    while (className != null) {
                        if (StringUtils.isBlank(className)) continue;
                        String fileName = className.replace(".", "/").concat(".class");
                        final InputStream fin = IOUtils.getResourceAsStream(fileName);
                        ClassFoundEvent.ClassInfo info = new ClassFoundEvent.ClassInfo() {
                            @Override
                            public InputStream getFileStream() {
                                return fin;
                            }

                            @Override
                            public void closeFileStream() {
                                closeQuietly(fin);
                            }
                        };
                        info.fileName = className.substring(className.lastIndexOf(".") + 1).concat(".class");
                        SystemEventBus.publish(new ClassFoundEvent(info, true));
                        info.closeFileStream();
                        className = reader.readLine();
                    }
                }
                closeQuietly(reader);
            } catch (IOException e) {
                logger.error("read classes cache list error", e);
            } finally {
                closeQuietly(in);
            }
        }
    }

    private void registerInstance() {
        register(new ApplicationEventListener() {
            @Override
            public void onEvent(ApplicationEvent event) {
                SystemEventBus.publish(new Event(event));
            }

            @Override
            public RequestEventListener onRequest(org.glassfish.jersey.server.monitoring.RequestEvent requestEvent) {
                AmebaFeature.publishEvent(new RequestEvent(requestEvent));
                return new RequestEventListener() {
                    @Override
                    public void onEvent(org.glassfish.jersey.server.monitoring.RequestEvent event) {
                        AmebaFeature.publishEvent(new RequestEvent(event));
                    }
                };
            }
        });
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(Application.this).to(Application.class);
            }
        });
    }

    /**
     * <p>getApplicationName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getApplicationName() {
        return config.getApplicationName();
    }

    /**
     * <p>setApplicationName.</p>
     *
     * @param applicationName a {@link java.lang.String} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application setApplicationName(String applicationName) {
        config.setApplicationName(applicationName);
        return this;
    }

    /**
     * <p>Getter for the field <code>config</code>.</p>
     *
     * @return a {@link org.glassfish.jersey.server.ResourceConfig} object.
     */
    public ResourceConfig getConfig() {
        return config;
    }

    private void addOnSetup(Map<String, Object> configMap) {
        addOns = Sets.newHashSet();
        Set<SortEntry> addOnSorts = Sets.newTreeSet();
        for (String key : configMap.keySet()) {
            if (key.startsWith(ADDON_CONF_PREFIX)) {
                String className = (String) configMap.get(key);
                if (StringUtils.isNotBlank(className)) {
                    String name = key.substring(ADDON_CONF_PREFIX.length());

                    int sortSp = name.lastIndexOf(">");
                    Integer sortPriority = Integer.MAX_VALUE / 2;
                    if (sortSp != -1) {
                        String sortStr = name.substring(sortSp + 1);
                        if (sortStr.equalsIgnoreCase("last")) {
                            sortPriority = Integer.MAX_VALUE;
                        } else {
                            sortPriority = Ints.tryParse(sortStr);
                            if (sortPriority == null || sortPriority < 0 || sortPriority > Integer.MAX_VALUE) {
                                throw new ConfigErrorException(
                                        "插件配置出错，执行优先级设置错误，必须为last或数字（且大于-1小于"
                                                + Integer.MAX_VALUE + "）。配置 " + key);
                            }
                        }
                    }

                    if (sortSp != -1) {
                        name = name.substring(0, sortSp);
                    }

                    addOnSorts.add(new SortEntry(sortPriority, className, name, key));
                }
            }
        }

        for (SortEntry entry : addOnSorts) {
            logger.debug("注册插件 [{}({})}", entry.key, entry.className);
            try {
                Class addOnClass = ClassUtils.getClass(entry.className);
                if (AddOn.class.isAssignableFrom(addOnClass)) {
                    AddOn addOn = (AddOn) addOnClass.newInstance();
                    boolean f = addOns.add(addOn);
                    if (f)
                        addOn.setup(this);
                } else {
                    throw new ConfigErrorException("插件 " + entry.name + " 类配置必须实现ameba.core.AddOn,在鍵 " + entry.key + " 配置项。");
                }
            } catch (ClassNotFoundException e) {
                throw new ConfigErrorException("插件 " + entry.name + " 未找到,在鍵 " + entry.key + " 配置项。");
            } catch (InstantiationException e) {
                throw new ConfigErrorException("插件 " + entry.name + " 无法初始化,在鍵 " + entry.key + " 配置项。");
            } catch (IllegalAccessException e) {
                throw new ConfigErrorException("插件 " + entry.name + " 无法初始化,在鍵 " + entry.key + " 配置项。");
            } catch (Exception e) {
                logger.error("插件 " + entry.name + " 出错,在鍵 " + entry.key + " 配置项。", e);
            }
        }
    }

    private void addOnDone() {
        for (AddOn addOn : addOns) {
            try {
                addOn.done(this);
            } catch (Exception e) {
                logger.error("插件出错,在 " + addOn.getClass(), e);
            }
        }
    }

    /**
     * <p>Getter for the field <code>timestamp</code>.</p>
     *
     * @return a long.
     */
    public long getTimestamp() {
        return timestamp;
    }

    private void configureFeature(Map<String, Object> configMap) {
        logger.info("注册特性");

        int suc = 0, fail = 0, beak = 0;

        Set<FeatureEntry> featureEntries = Sets.newTreeSet();

        for (String key : configMap.keySet()) {
            if (key.startsWith(REGISTER_CONF_PREFIX)) {
                String className = (String) configMap.get(key);
                if (StringUtils.isNotBlank(className)) {
                    String name = key.substring(REGISTER_CONF_PREFIX.length());

                    int sortSp = name.lastIndexOf(">");
                    Integer sortPriority = Integer.MAX_VALUE / 2;
                    if (sortSp != -1) {
                        String sortStr = name.substring(sortSp + 1);
                        sortSp = name.lastIndexOf("!");
                        if (sortSp != -1)
                            sortStr = sortStr.substring(0, sortSp);
                        if (sortStr.equalsIgnoreCase("last")) {
                            sortPriority = Integer.MAX_VALUE;
                        } else {
                            sortPriority = Ints.tryParse(sortStr);
                            if (sortPriority == null || sortPriority < 0 || sortPriority > Integer.MAX_VALUE) {
                                throw new ConfigErrorException(
                                        "特性配置出错，执行优先级设置错误，必须为last或数字（且大于-1小于"
                                                + Integer.MAX_VALUE + "）。配置 " + key);
                            }
                        }
                    }

                    int prioritySp = name.lastIndexOf("!");
                    Integer diPriority = ContractProvider.NO_PRIORITY;
                    if (prioritySp != -1) {
                        diPriority = Ints.tryParse(name.substring(prioritySp + 1));
                        if (diPriority == null || diPriority < 0 || diPriority > Integer.MAX_VALUE) {
                            throw new ConfigErrorException(
                                    "特性配置出错，DI优先级设置错误，必须为数字，且大于-1小于"
                                            + Integer.MAX_VALUE + "。配置 " + key);
                        }
                    }

                    if (prioritySp != -1 || sortSp != -1) {
                        if (prioritySp > sortSp) {
                            name = name.substring(0, sortSp);
                        } else if (prioritySp < sortSp) {
                            if (prioritySp != -1) {
                                name = name.substring(0, prioritySp);
                            } else {
                                name = name.substring(0, sortSp);
                            }
                        }
                    }

                    featureEntries.add(new FeatureEntry(diPriority, sortPriority, className, name));
                }
            }
        }

        for (FeatureEntry entry : featureEntries) {
            try {
                logger.debug("注册特性[{}({})]", entry.name, entry.className);
                Class clazz = ClassUtils.getClass(entry.className);
                if (isRegistered(clazz)) {
                    beak++;
                    logger.warn("并未注册装特性[{}({})]，因为该特性已存在", entry.name, clazz);
                    continue;
                }

                register(clazz, entry.diPriority);
                suc++;
            } catch (ClassNotFoundException e) {
                fail++;
                if (!entry.name.startsWith("default."))
                    logger.error("获取特性失败", e);
                else
                    logger.warn("未找到系统默认特性[" + entry.className + "]", e);
            }
        }

        String registerStr = StringUtils.deleteWhitespace(
                StringUtils.defaultIfBlank((String) getProperty("app.registers"), ""));
        String[] registers;
        if (StringUtils.isNotBlank(registerStr)) {
            registers = registerStr.split(",");
            for (String register : registers) {
                try {
                    logger.debug("注册特性[{}]", register);
                    Class clazz = ClassUtils.getClass(register);
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

        logger.info("成功注册{}个特性，失败{}个，跳过{}个", suc, fail, beak);
    }

    private void subscribeResourceEvent() {

        final Set<ClassFoundEvent.ClassInfo> resources = Sets.newHashSet();

        SystemEventBus.subscribe(ClassFoundEvent.class, new Listener<ClassFoundEvent>() {
            @Override
            public void onReceive(ClassFoundEvent event) {
                event.accept(new ClassFoundEvent.ClassAccept() {
                    @Override
                    public boolean accept(ClassFoundEvent.ClassInfo info) {
                        if (info.isPublic() && info.containsAnnotations(Path.class, Provider.class)) {
                            resources.add(info);
                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        addOns.add(new AddOn() {
            @Override
            public void done(Application application) {
                for (ClassFoundEvent.ClassInfo info : resources) {
                    Class clazz = info.toClass();
                    if (!isRegistered(clazz))
                        register(clazz);
                }
                resources.clear();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void configureResource() {
        String[] packages = StringUtils.deleteWhitespace(
                StringUtils.defaultIfBlank((String) getProperty("resource.packages"), "")).split(",");
        for (String key : getPropertyNames()) {
            if (key.startsWith("resource.packages.")) {
                Object pkgObj = getProperty(key);
                if (pkgObj instanceof String) {
                    String pkgStr = (String) pkgObj;
                    if (StringUtils.isNotBlank(pkgStr)) {
                        String[] pkgs = StringUtils.deleteWhitespace(pkgStr).split(",");
                        for (String pkg : pkgs) {
                            if (!ArrayUtils.contains(packages, pkg))
                                packages = ArrayUtils.add(packages, pkg);
                        }
                    }
                }
            }
        }
        packages = ArrayUtils.removeElement(packages, "");
        logger.info("设置资源扫描包:{}", StringUtils.join(packages, ","));
        packages(packages);
        subscribeResourceEvent();
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
                name = name.replace(".", "_").toUpperCase();
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
                closeQuietly(in);
            }
        }
        if (modeProperties.size() > 0)
            //将模式配置放入临时配置对象
            configMap.putAll((Map) modeProperties);

        //清空应用程序模式配置
        modeProperties.clear();
    }

    private void configureServer() {
        jmxEnabled = Boolean.parseBoolean((String) getProperty("app.jmx.enabled"));
        if (jmxEnabled && getProperty(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED) == null)
            property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, jmxEnabled);

        subscribeServerEvent();
    }

    private void subscribeServerEvent() {
        SystemEventBus.subscribe(Container.StartupEvent.class, new Listener<Container.StartupEvent>() {
            @Override
            public void onReceive(Container.StartupEvent event) {

                Application.this.container = event.getContainer();

                if (!isInitialized()) {
                    Runtime r = Runtime.getRuntime();
                    r.gc();

                    String startUsedTime = Times.toDuration(System.currentTimeMillis() - timestamp);

                    StringBuilder builder = new StringBuilder();
                    builder
                            .append("Ameba版本  >   ")
                            .append(Ameba.getVersion())
                            .append("\n")
                            .append("HTTP容器   >   ")
                            .append(StringUtils.defaultString(container.getType(), "Unknown"))
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
                            .append(getMode())
                            .append("\n")
                            .append("监听地址    >   ");

                    List<Connector> connectors = getConnectors();
                    if (connectors != null && connectors.size() > 0) {
                        for (Connector connector : connectors) {
                            builder.append("\n             ")
                                    .append(connector.getHttpServerBaseUri());
                        }
                    } else {
                        builder.append("\n             无");
                        logger.warn("请通过connector.[Name].port配置监听端口");
                    }

                    logger.getSource().info("应用已启动\n{}\n{}\n{}",
                            INFO_SPLITOR,
                            builder,
                            INFO_SPLITOR);
                }

                initialized = true;
            }
        });
    }

    private String toExternalForm(URL url) {
        if (url == null) return null;
        try {
            return URLDecoder.decode(url.toExternalForm(), Charset.defaultCharset().name());
        } catch (Exception e) {
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

            if (urls.hasMoreElements()) {
                List<String> urlList = Lists.newArrayList(toExternalForm(url));
                while (urls.hasMoreElements()) {
                    urlList.add(urls.nextElement().toExternalForm());
                }
                String errorMsg = "存在多个程序配置,请使用唯一的程序配置文件:\n" + StringUtils.join(urlList, "\n");
                logger.error(errorMsg);
                throw new ConfigErrorException(errorMsg);
            }

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
        } else {
            logger.warn("未找到{}文件,请何实", confFile);
        }
        return url;
    }

    /**
     * <p>register.</p>
     *
     * @param componentClass a {@link java.lang.Class} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application register(Class<?> componentClass) {
        config.register(componentClass);
        return this;
    }

    /**
     * <p>register.</p>
     *
     * @param component a {@link java.lang.Object} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application register(Object component) {
        config.register(component);
        return this;
    }

    /**
     * <p>registerClasses.</p>
     *
     * @param classes a {@link java.lang.Class} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application registerClasses(Class<?>... classes) {
        config.registerClasses(classes);
        return this;
    }

    /**
     * <p>register.</p>
     *
     * @param component       a {@link java.lang.Object} object.
     * @param bindingPriority a int.
     * @return a {@link ameba.core.Application} object.
     */
    public Application register(Object component, int bindingPriority) {
        config.register(component, bindingPriority);
        return this;
    }

    /**
     * <p>getConfiguration.</p>
     *
     * @return a {@link org.glassfish.jersey.server.ServerConfig} object.
     */
    public ServerConfig getConfiguration() {
        return config.getConfiguration();
    }

    /**
     * <p>getClassLoader.</p>
     *
     * @return a {@link java.lang.ClassLoader} object.
     */
    public ClassLoader getClassLoader() {
        return config.getClassLoader();
    }

    /**
     * <p>setClassLoader.</p>
     *
     * @param classLoader a {@link java.lang.ClassLoader} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application setClassLoader(ClassLoader classLoader) {
        config.setClassLoader(classLoader);
        return this;
    }

    /**
     * <p>registerInstances.</p>
     *
     * @param instances a {@link java.lang.Object} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application registerInstances(Object... instances) {
        config.registerInstances(instances);
        return this;
    }

    /**
     * <p>packages.</p>
     *
     * @param packages a {@link java.lang.String} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application packages(String... packages) {
        if (scanPkgs == null) {
            scanPkgs = Sets.newHashSet();
        }
        Collections.addAll(scanPkgs, packages);
        return this;
    }

    /**
     * <p>getPackages.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getPackages() {
        return scanPkgs;
    }

    /**
     * <p>register.</p>
     *
     * @param component a {@link java.lang.Object} object.
     * @param contracts a {@link java.util.Map} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application register(Object component, Map<Class<?>, Integer> contracts) {
        config.register(component, contracts);
        return this;
    }

    /**
     * <p>getResources.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Resource> getResources() {
        return config.getResources();
    }

    /**
     * <p>getSingletons.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Object> getSingletons() {
        return config.getSingletons();
    }

    /**
     * <p>getPropertyNames.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<String> getPropertyNames() {
        return config.getPropertyNames();
    }

    /**
     * <p>register.</p>
     *
     * @param componentClass a {@link java.lang.Class} object.
     * @param contracts      a {@link java.lang.Class} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application register(Class<?> componentClass, Class<?>... contracts) {
        config.register(componentClass, contracts);
        return this;
    }

    /**
     * <p>getClasses.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Class<?>> getClasses() {
        return config.getClasses();
    }

    /**
     * <p>register.</p>
     *
     * @param component a {@link java.lang.Object} object.
     * @param contracts a {@link java.lang.Class} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application register(Object component, Class<?>... contracts) {
        config.register(component, contracts);
        return this;
    }

    /**
     * <p>isRegistered.</p>
     *
     * @param componentClass a {@link java.lang.Class} object.
     * @return a boolean.
     */
    public boolean isRegistered(Class<?> componentClass) {
        return config.isRegistered(componentClass);
    }

    /**
     * <p>registerResources.</p>
     *
     * @param resources a {@link java.util.Set} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application registerResources(Set<Resource> resources) {
        config.registerResources(resources);
        return this;
    }

    /**
     * <p>isEnabled.</p>
     *
     * @param feature a {@link javax.ws.rs.core.Feature} object.
     * @return a boolean.
     */
    public boolean isEnabled(Feature feature) {
        return config.isEnabled(feature);
    }

    /**
     * <p>getContracts.</p>
     *
     * @param componentClass a {@link java.lang.Class} object.
     * @return a {@link java.util.Map} object.
     */
    public Map<Class<?>, Integer> getContracts(Class<?> componentClass) {
        return config.getContracts(componentClass);
    }

    /**
     * <p>getProperty.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    public Object getProperty(String name) {
        return config.getProperty(name);
    }

    /**
     * <p>addProperties.</p>
     *
     * @param properties a {@link java.util.Map} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application addProperties(Map<String, Object> properties) {
        config.addProperties(properties);
        return this;
    }

    /**
     * <p>registerFinder.</p>
     *
     * @param resourceFinder a {@link org.glassfish.jersey.server.ResourceFinder} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application registerFinder(ResourceFinder resourceFinder) {
        config.registerFinder(resourceFinder);
        return this;
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * <p>register.</p>
     *
     * @param componentClass  a {@link java.lang.Class} object.
     * @param bindingPriority a int.
     * @return a {@link ameba.core.Application} object.
     */
    public Application register(Class<?> componentClass, int bindingPriority) {
        config.register(componentClass, bindingPriority);
        return this;
    }

    /**
     * <p>registerResources.</p>
     *
     * @param resources a {@link org.glassfish.jersey.server.model.Resource} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application registerResources(Resource... resources) {
        config.registerResources(resources);
        return this;
    }

    /**
     * <p>getRuntimeType.</p>
     *
     * @return a {@link javax.ws.rs.RuntimeType} object.
     */
    public RuntimeType getRuntimeType() {
        return config.getRuntimeType();
    }

    /**
     * <p>isRegistered.</p>
     *
     * @param component a {@link java.lang.Object} object.
     * @return a boolean.
     */
    public boolean isRegistered(Object component) {
        return config.isRegistered(component);
    }

    /**
     * <p>getProperties.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Object> getProperties() {
        return config.getProperties();
    }

    /**
     * <p>setProperties.</p>
     *
     * @param properties a {@link java.util.Map} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application setProperties(Map<String, ?> properties) {
        config.setProperties(properties);
        return this;
    }

    /**
     * <p>property.</p>
     *
     * @param name  a {@link java.lang.String} object.
     * @param value a {@link java.lang.Object} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application property(String name, Object value) {
        config.property(name, value);
        return this;
    }

    /**
     * <p>registerInstances.</p>
     *
     * @param instances a {@link java.util.Set} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application registerInstances(Set<Object> instances) {
        config.registerInstances(instances);
        return this;
    }

    /**
     * <p>getInstances.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Object> getInstances() {
        return config.getInstances();
    }

    /**
     * <p>register.</p>
     *
     * @param componentClass a {@link java.lang.Class} object.
     * @param contracts      a {@link java.util.Map} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        config.register(componentClass, contracts);
        return this;
    }

    /**
     * <p>registerClasses.</p>
     *
     * @param classes a {@link java.util.Set} object.
     * @return a {@link ameba.core.Application} object.
     */
    public Application registerClasses(Set<Class<?>> classes) {
        config.registerClasses(classes);
        return this;
    }

    /**
     * <p>isEnabled.</p>
     *
     * @param featureClass a {@link java.lang.Class} object.
     * @return a boolean.
     */
    public boolean isEnabled(Class<? extends Feature> featureClass) {
        return config.isEnabled(featureClass);
    }

    /**
     * <p>isProperty.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean isProperty(String name) {
        return config.isProperty(name);
    }

    /**
     * <p>Getter for the field <code>packageRoot</code>.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getPackageRoot() {
        return packageRoot;
    }

    /**
     * <p>Setter for the field <code>packageRoot</code>.</p>
     *
     * @param packageRoot a {@link java.io.File} object.
     */
    public void setPackageRoot(File packageRoot) {
        this.packageRoot = packageRoot;
    }

    /**
     * <p>Getter for the field <code>configFiles</code>.</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getConfigFiles() {
        return configFiles;
    }

    /**
     * <p>Getter for the field <code>sourceRoot</code>.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getSourceRoot() {
        return sourceRoot;
    }

    /**
     * <p>Setter for the field <code>sourceRoot</code>.</p>
     *
     * @param sourceRoot a {@link java.io.File} object.
     */
    public void setSourceRoot(File sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    /**
     * <p>Getter for the field <code>mode</code>.</p>
     *
     * @return a {@link ameba.core.Application.Mode} object.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * <p>Getter for the field <code>applicationVersion</code>.</p>
     *
     * @return a {@link java.lang.CharSequence} object.
     */
    public CharSequence getApplicationVersion() {
        return applicationVersion;
    }

    /**
     * <p>getConnectors.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Connector> getConnectors() {
        return container.getConnectors();
    }

    /**
     * <p>Getter for the field <code>container</code>.</p>
     *
     * @return a {@link ameba.container.Container} object.
     */
    public Container getContainer() {
        return container;
    }

    /**
     * <p>isJmxEnabled.</p>
     *
     * @return a boolean.
     */
    public boolean isJmxEnabled() {
        return jmxEnabled;
    }

    /**
     * 设置日志器
     */
    private void configureLogger(Properties properties) {
        //set logback config file
        URL loggerConfigFile = getResource(StringUtils.defaultIfBlank(properties.getProperty("logger.config.file"), "conf/logback.groovy"));

        if (loggerConfigFile == null) {
            loggerConfigFile = getResource("conf/logback-" + getMode().name().toLowerCase() + ".groovy");
        }

        if (loggerConfigFile != null) {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.reset();
            context.putProperty("appName", getApplicationName());
            String appPackage = properties.getProperty("app.package");
            context.putProperty("appPackage", appPackage);
            String traceEnabled = properties.getProperty("ameba.trace.enabled");
            context.putProperty("ameba.trace.enabled", traceEnabled);
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

    /**
     * <p>Getter for the field <code>addOns</code>.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<AddOn> getAddOns() {
        return addOns;
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

    private static class State {
        protected boolean jmxEnabled;
        private Mode mode;
        private CharSequence applicationVersion;
        private File sourceRoot;
        private File packageRoot;
        private Container container;
        private Set<AddOn> addOns;
        private ResourceConfig config;
        private Set<String> scanPkgs;
    }

    public static class Event implements ameba.event.Event {

        ApplicationEvent event;

        public Event(ApplicationEvent event) {
            this.event = event;
        }

        public ApplicationEvent.Type getType() {
            return event.getType();
        }

        public ResourceConfig getResourceConfig() {
            return event.getResourceConfig();
        }

        public ResourceModel getResourceModel() {
            return event.getResourceModel();
        }

        public Set<Class<?>> getProviders() {
            return event.getProviders();
        }

        public Set<Object> getRegisteredInstances() {
            return event.getRegisteredInstances();
        }

        public Set<Class<?>> getRegisteredClasses() {
            return event.getRegisteredClasses();
        }
    }

    public static class RequestEvent implements ameba.event.Event {
        org.glassfish.jersey.server.monitoring.RequestEvent event;

        public RequestEvent(org.glassfish.jersey.server.monitoring.RequestEvent event) {
            this.event = event;
        }

        public org.glassfish.jersey.server.monitoring.RequestEvent.Type getType() {
            return event.getType();
        }

        public org.glassfish.jersey.server.monitoring.RequestEvent.ExceptionCause getExceptionCause() {
            return event.getExceptionCause();
        }

        public Iterable<ContainerResponseFilter> getContainerResponseFilters() {
            return event.getContainerResponseFilters();
        }

        public ContainerRequest getContainerRequest() {
            return event.getContainerRequest();
        }

        public boolean isResponseSuccessfullyMapped() {
            return event.isResponseSuccessfullyMapped();
        }

        public Iterable<ContainerRequestFilter> getContainerRequestFilters() {
            return event.getContainerRequestFilters();
        }

        public boolean isResponseWritten() {
            return event.isResponseWritten();
        }

        public boolean isSuccess() {
            return event.isSuccess();
        }

        public Throwable getException() {
            return event.getException();
        }

        public ExceptionMapper<?> getExceptionMapper() {
            return event.getExceptionMapper();
        }

        public ExtendedUriInfo getUriInfo() {
            return event.getUriInfo();
        }

        public ContainerResponse getContainerResponse() {
            return event.getContainerResponse();
        }
    }

    public static class ClassFoundEvent implements ameba.event.Event {
        private boolean accept;
        private boolean cacheMode = false;
        private ClassInfo classInfo;

        public ClassFoundEvent(ClassInfo classInfo, boolean cacheMode) {
            this.cacheMode = cacheMode;
            this.classInfo = classInfo;
        }

        public ClassFoundEvent(ClassInfo classInfo) {
            this.classInfo = classInfo;
        }

        public InputStream getFileStream() {
            return classInfo.getFileStream();
        }

        public void accept(ClassAccept accept) {
            try {
                boolean re = accept.accept(classInfo);
                if (!this.accept && re)
                    this.accept = true;
            } catch (Exception e) {
                logger.error("class accept error", e);
            }
        }

        public boolean isCacheMode() {
            return cacheMode;
        }

        public interface ClassAccept {
            /**
             * 如果是需要的class则返回true
             *
             * @param info class info
             * @return 是否需要该class
             */
            boolean accept(ClassInfo info);
        }

        public static abstract class ClassInfo {
            private CtClass ctClass;
            private String fileName;
            private Object[] annotations;

            public String getFileName() {
                return fileName;
            }

            public CtClass getCtClass() {
                if (ctClass == null) {
                    try {
                        ctClass = ClassPool.getDefault().makeClass(getFileStream());
                    } catch (IOException e) {
                        throw new AmebaException("make class error", e);
                    }
                }
                return ctClass;
            }

            public String getClassName() {
                return getCtClass().getName();
            }

            public Object[] getAnnotations() {
                if (annotations == null) {
                    annotations = getCtClass().getAvailableAnnotations();
                }
                return annotations;
            }

            public boolean containsAnnotations(Class<? extends Annotation>... annotationClass) {
                if (ArrayUtils.isEmpty(annotationClass)) {
                    return false;
                }

                for (Object anno : getAnnotations()) {
                    for (Class cls : annotationClass) {
                        if (((Annotation) anno).annotationType().equals(cls)) {
                            return true;
                        }
                    }
                }
                return false;
            }

            public boolean isPublic() {
                return javassist.Modifier.isPublic(getCtClass().getModifiers());
            }

            public Class toClass() {
                return getClassForName(getCtClass().getName());
            }

            public Class getClassForName(final String className) {
                try {
                    final OsgiRegistry osgiRegistry = ReflectionHelper.getOsgiRegistryInstance();

                    if (osgiRegistry != null) {
                        return osgiRegistry.classForNameWithException(className);
                    } else {
                        return AccessController.doPrivileged(ReflectionHelper.classForNameWithExceptionPEA(className));
                    }
                } catch (final ClassNotFoundException ex) {
                    throw new RuntimeException(LocalizationMessages.ERROR_SCANNING_CLASS_NOT_FOUND(className), ex);
                } catch (final PrivilegedActionException pae) {
                    final Throwable cause = pae.getCause();
                    if (cause instanceof ClassNotFoundException) {
                        throw new RuntimeException(LocalizationMessages.ERROR_SCANNING_CLASS_NOT_FOUND(className), cause);
                    } else if (cause instanceof RuntimeException) {
                        throw (RuntimeException) cause;
                    } else {
                        throw new RuntimeException(cause);
                    }
                }
            }

            public boolean startsWithPackage(String... pkgs) {
                for (String st : pkgs) {
                    if (!st.endsWith(".")) st += ".";
                    String className = getClassName();
                    if (className.startsWith(st)) {
                        return true;
                    }
                }
                return false;
            }

            public abstract InputStream getFileStream();

            public abstract void closeFileStream();
        }
    }

    private class SortEntry implements Comparable<SortEntry> {
        Integer sortPriority;
        String className;
        String name;
        String key;

        private SortEntry(Integer sortPriority, String className, String name) {
            this.sortPriority = sortPriority;
            this.className = className;
            this.name = name;
        }

        private SortEntry(Integer sortPriority, String className, String name, String key) {
            this.sortPriority = sortPriority;
            this.className = className;
            this.name = name;
            this.key = key;
        }

        @Override
        public int compareTo(SortEntry entry) {
            if (this.className.equals(entry.className)) {
                return 0;
            }
            int index = Integer.compare(this.sortPriority, entry.sortPriority);
            if (index == 0)
                return 1;
            return index;
        }
    }

    private class FeatureEntry extends SortEntry {
        int diPriority;

        private FeatureEntry(int diPriority, Integer sortPriority, String className, String name) {
            super(sortPriority, className, name);
            this.diPriority = diPriority;
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
