package ameba.core;

import ameba.Ameba;
import ameba.container.Container;
import ameba.container.server.Connector;
import ameba.event.Listener;
import ameba.event.SystemEventBus;
import ameba.exception.AmebaException;
import ameba.exception.ConfigErrorException;
import ameba.feature.AmebaFeature;
import ameba.i18n.Messages;
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
import java.net.*;
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
    private static final String REGISTER_CONF_PREFIX = "register.";
    private static final String ADDON_CONF_PREFIX = "addon.";
    private static final String JERSEY_CONF_NAME_PREFIX = "sys.core.";
    private static final String DEFAULT_LOGBACK_CONF = "log.groovy";
    private static final String EXCLUDES_KEY = "exclude.classes";
    private static final String EXCLUDES_KEY_PREFIX = EXCLUDES_KEY + ".";
    private static String SCAN_CLASSES_CACHE_FILE;
    private static InitializationLogger logger = new InitializationLogger(Application.class, null);
    private static String INFO_SPLITER = "---------------------------------------------------";
    protected boolean jmxEnabled;
    private String[] configFiles;
    private long timestamp = System.currentTimeMillis();
    private boolean initialized = false;
    private Mode mode;
    private CharSequence applicationVersion;
    private File sourceRoot;
    private File packageRoot;
    private Container container;
    private Set<AddOn> addOns = Sets.newLinkedHashSet();
    private Set<String> excludes = Sets.newLinkedHashSet();
    private ResourceConfig config = new ExcludeResourceConfig(excludes);
    private Set<String> scanPkgs;
    private String[] ids;
    private Map<String, Object> srcProperties = Maps.newLinkedHashMap();

    protected Application() {
        logger = new InitializationLogger(Application.class, this);
    }

    /**
     * <p>Constructor for Application.</p>
     *
     * @param ids a {@link java.lang.String} object.
     */
    public Application(String... ids) {

        if (Ameba.getApp() != null) {
            throw new AmebaException(Messages.get("info.application.exists"));
        }

        this.ids = ids;
        logger = new InitializationLogger(Application.class, this);

        Set<String> configFiles = parseIds2ConfigFile(ids);
        this.configFiles = configFiles.toArray(new String[configFiles.size()]);

        configure();
    }

    public static Set<String> parseIds2ConfigFile(String... ids) {
        Set<String> configFiles = Sets.newLinkedHashSet();
        configFiles.add(DEFAULT_APP_CONF);

        if (ids != null && ids.length > 0) {
            String[] conf = DEFAULT_APP_CONF.split("\\.");
            String idPrefix = conf[0];
            String idSuffix = "." + conf[1];

            for (String id : ids) {
                if (StringUtils.isNotBlank(id)) {
                    String confFile = idPrefix + "_" + id + idSuffix;
                    configFiles.add(confFile);
                }
            }
        }
        return configFiles;
    }

    private static String toExternalForm(URL url) {
        if (url == null) return null;
        try {
            return URLDecoder.decode(url.toExternalForm(), Charset.defaultCharset().name());
        } catch (Exception e) {
            return url.toExternalForm();
        }
    }

    @SuppressWarnings("unchecked")
    public static void readModuleConfig(Properties properties, boolean isDev) {
        logger.info(Messages.get("info.module.load.conf"));
        //读取模块配置
        Enumeration<URL> moduleUrls = IOUtils.getResources("conf/module.conf");
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

                    logger.info(Messages.get("info.module.load", modelName));
                    logger.debug(Messages.get("info.module.load.item.conf", toExternalForm(url)));
                    URLConnection connection = url.openConnection();

                    if (isDev) {
                        connection.setUseCaches(false);
                    }
                    in = connection.getInputStream();
                } catch (IOException e) {
                    logger.error(Messages.get("info.load.error", toExternalForm(url)));
                }
                if (in != null) {
                    try {
                        properties.load(in);
                    } catch (Exception e) {
                        logger.error(Messages.get("info.load.error", toExternalForm(url)));
                    }
                } else {
                    logger.error(Messages.get("info.load.error", toExternalForm(url)));
                }
                closeQuietly(in);
            }
        } else {
            logger.info(Messages.get("info.module.none"));
        }
    }

    @SuppressWarnings("unchecked")
    public static void readModeConfig(Properties properties, Mode mode) {

        //读取相应模式的配置文件
        Enumeration<java.net.URL> confs = IOUtils.getResources("conf/" + mode.name().toLowerCase() + ".conf");
        while (confs.hasMoreElements()) {
            InputStream in = null;
            try {
                URLConnection connection = confs.nextElement().openConnection();
                if (mode.isDev()) {
                    connection.setUseCaches(false);
                }
                in = connection.getInputStream();
                properties.load(in);
            } catch (IOException e) {
                logger.warn(Messages.get("info.module.conf.error", "conf/" + mode.name().toLowerCase() + ".conf"), e);
            } finally {
                closeQuietly(in);
            }
        }
    }

    public static Properties readDefaultConfig() {
        Properties properties = new Props();

        //读取系统默认配置
        try {
            properties.load(getResourceAsStream("conf/default.conf"));
        } catch (Exception e) {
            logger.warn(Messages.get("info.module.conf.error", "conf/default.conf"), e);
        }

        return properties;
    }

    public static URL readAppConfig(Properties properties, String confFile) {
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
                String errorMsg = Messages.get("info.load.config.multi.error", StringUtils.join(urlList, "\n"));
                logger.error(errorMsg);
                throw new ConfigErrorException(errorMsg);
            }

            try {
                logger.trace(Messages.get("info.load", toExternalForm(url)));
                in = url.openStream();
            } catch (IOException e) {
                logger.error(Messages.get("info.load.error", toExternalForm(url)));
            }
            if (in != null) {
                try {
                    properties.load(in);
                } catch (Exception e) {
                    logger.error(Messages.get("info.load.error", toExternalForm(url)));
                }
            } else {
                logger.error(Messages.get("info.load.error", toExternalForm(url)));
            }
            closeQuietly(in);
        } else {
            logger.warn(Messages.get("info.load.error.not.found", confFile));
        }
        return url;
    }

    public void reconfigure() {
        configure();
    }

    @SuppressWarnings("unchecked")
    protected void configure() {

        Properties properties = readDefaultConfig();

        Properties appProperties = new Props();

        List<String> appConf = Lists.newArrayListWithExpectedSize(configFiles.length);
        for (String conf : configFiles) {
            //读取应用程序配置
            URL appCfgUrl = readAppConfig(appProperties, conf);
            appConf.add(toExternalForm(appCfgUrl));
        }

        //获取应用程序模式
        try {
            mode = Mode.valueOf(appProperties.getProperty("app.mode").toUpperCase());
        } catch (Exception e) {
            mode = Mode.PRODUCT;
        }

        //设置应用程序名称
        setApplicationName(StringUtils.defaultString(appProperties.getProperty("app.name"), DEFAULT_APP_NAME));
        applicationVersion = appProperties.getProperty("app.version");
        if (StringUtils.isBlank(applicationVersion)) {
            applicationVersion = new UnknownVersion();
        }

        //配置日志器
        configureLogger(appProperties);

        if (!isInitialized())
            Ameba.printInfo();

        logger.info(Messages.get("info.init"));
        logger.info(Messages.get("info.app.conf", appConf));

        //读取模式配置
        readModeConfig(properties, mode);

        //读取模块配置
        readModuleConfig(properties, getMode().isDev());

        properties.putAll(appProperties);

        srcProperties.putAll((Map) properties);

        addOnSetup(srcProperties);

        //转换jersey配置项
        convertJerseyConfig(srcProperties);

        //将临时配置对象放入应用程序配置
        addProperties(srcProperties);

        srcProperties = Collections.unmodifiableMap(srcProperties);

        registerInstance();

        register(Requests.BindRequest.class);

        configureExclude(srcProperties);

        //配置资源
        configureResource();

        //配置特性
        configureFeature(srcProperties);

        //配置服务器相关
        configureServer();

        //清空临时读取的配置
        properties.clear();

        scanClasses();

        addOnDone();

        logger.info(Messages.get("info.feature.load"));
    }

    private void configureExclude(Map<String, Object> configMap) {

        String ex = (String) configMap.get(EXCLUDES_KEY);
        if (StringUtils.isNotBlank(ex)) {
            addExcludes(ex);
        }

        for (String key : configMap.keySet()) {
            if (key.startsWith(EXCLUDES_KEY_PREFIX)) {
                addExcludes((String) configMap.get(key));
            }
        }

        logger.debug(Messages.get("info.exclude.classes", excludes));
    }

    private void addExcludes(String ex) {
        if (StringUtils.isNotBlank(ex)) {
            for (String e : ex.split(",")) {
                if (StringUtils.isNotBlank(e)) {
                    excludes.add(e);
                }
            }
        }
    }

    private void scanClasses() {
        if (SCAN_CLASSES_CACHE_FILE == null) {
            SCAN_CLASSES_CACHE_FILE = IOUtils.getResource("/").getPath() + "conf/classes.list";
        }
        URL cacheList = IOUtils.getResource(SCAN_CLASSES_CACHE_FILE);
        if (cacheList == null || getMode().isDev()) {
            logger.debug(Messages.get("info.scan.classes"));
            final PackageNamesScanner scanner = new PackageNamesScanner(
                    scanPkgs.toArray(new String[scanPkgs.size()]), true);
            Set<String> foundClasses = Sets.newHashSet();
            List<String> acceptClasses = Lists.newArrayList();
            while (scanner.hasNext()) {
                String fileName = scanner.next();
                if (!fileName.endsWith(".class")) continue;
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
                info.fileName = fileName;
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
            } catch (IOException e) {
                logger.error(Messages.get("info.write.class.cache.error"), e);
            } finally {
                acceptClasses.clear();
                closeQuietly(out);
            }
        } else {
            logger.debug(Messages.get("info.read.class.cache.error"));
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
                logger.error(Messages.get("info.read.class.cache.error"), e);
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
                logger.getSource().trace(Messages.get("info.on.request", requestEvent.getType().name()));
                AmebaFeature.publishEvent(new RequestEvent(requestEvent));
                return new RequestEventListener() {
                    @Override
                    public void onEvent(org.glassfish.jersey.server.monitoring.RequestEvent event) {
                        logger.getSource().trace(Messages.get("info.on.request", event.getType().name()));
                        AmebaFeature.publishEvent(new RequestEvent(event));
                    }
                };
            }
        });
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(Application.this).to(Application.class).proxy(false);
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

    protected void addOnSetup(Map<String, Object> configMap) {
        Set<SortEntry> addOnSorts = Sets.newTreeSet();
        for (String key : configMap.keySet()) {
            if (key.startsWith(ADDON_CONF_PREFIX)) {
                String className = (String) configMap.get(key);
                if (StringUtils.isNotBlank(className)) {
                    String name = key.substring(ADDON_CONF_PREFIX.length());

                    int sortSp = name.lastIndexOf(">");
                    Integer sortPriority = 1000;
                    if (sortSp != -1) {
                        String sortStr = name.substring(sortSp + 1);
                        if (sortStr.equalsIgnoreCase("last")) {
                            sortPriority = Integer.MAX_VALUE;
                        } else {
                            sortPriority = Ints.tryParse(sortStr);
                            if (sortPriority == null || sortPriority < 0 || sortPriority > Integer.MAX_VALUE) {
                                throw new ConfigErrorException(
                                        Messages.get("info.addon.key.priority.error", Integer.MAX_VALUE, key)
                                );
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
            logger.debug(Messages.get("info.addon.register.item", entry.key, entry.className));
            try {
                Class addOnClass = ClassUtils.getClass(entry.className);
                if (AddOn.class.isAssignableFrom(addOnClass)) {
                    AddOn addOn = (AddOn) addOnClass.newInstance();
                    boolean f = addOns.add(addOn);
                    if (f)
                        addOn.setup(this);
                } else {
                    throw new ConfigErrorException(Messages.get("info.addon.register.error.interface", entry.name, entry.key));
                }
            } catch (ClassNotFoundException e) {
                throw new ConfigErrorException(Messages.get("info.addon.register.error.not.found", entry.name, entry.key));
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ConfigErrorException(Messages.get("info.addon.register.error.init", entry.name, entry.key));
            } catch (Exception e) {
                logger.error(Messages.get("info.addon.register.error", entry.name, entry.key), e);
            }
        }
    }

    protected void addOnDone() {
        for (AddOn addOn : addOns) {
            try {
                addOn.done(this);
            } catch (Exception e) {
                logger.error(Messages.get("info.addon.error", addOn.getClass().getName()), e);
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

    protected void configureFeature(Map<String, Object> configMap) {

        logger.debug(Messages.get("info.feature.register"));

        int suc = 0, fail = 0, beak = 0;

        Set<FeatureEntry> featureEntries = Sets.newTreeSet();

        for (String key : configMap.keySet()) {
            if (key.startsWith(REGISTER_CONF_PREFIX)) {
                String className = (String) configMap.get(key);
                if (StringUtils.isNotBlank(className)) {
                    String name = key.substring(REGISTER_CONF_PREFIX.length());

                    int sortSp = name.lastIndexOf(">");
                    Integer sortPriority = 1000;
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
                                        Messages.get("info.feature.key.priority.error", Integer.MAX_VALUE, key)
                                );
                            }
                        }
                    }

                    int prioritySp = name.lastIndexOf("!");
                    Integer diPriority = ContractProvider.NO_PRIORITY;
                    if (prioritySp != -1) {
                        diPriority = Ints.tryParse(name.substring(prioritySp + 1));
                        if (diPriority == null || diPriority < 0 || diPriority > Integer.MAX_VALUE) {
                            throw new ConfigErrorException(
                                    Messages.get("info.feature.key.priority.error", Integer.MAX_VALUE, key)
                            );
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
                logger.debug(Messages.get("info.feature.register.item", entry.name, entry.className));
                Class clazz = ClassUtils.getClass(entry.className);
                if (isRegistered(clazz)) {
                    beak++;
                    logger.warn(Messages.get("info.feature.exists", entry.name, clazz));
                    continue;
                }

                register(clazz, entry.diPriority);
                suc++;
            } catch (ClassNotFoundException e) {
                fail++;
                if (!entry.name.startsWith("default."))
                    logger.error(Messages.get("info.feature.sys.get.error"), e);
                else
                    logger.warn(Messages.get("info.feature.sys.not.found", entry.className), e);
            }
        }

        String registerStr = StringUtils.deleteWhitespace(
                StringUtils.defaultIfBlank((String) getProperty("registers"), ""));
        String[] registers;
        if (StringUtils.isNotBlank(registerStr)) {
            registers = registerStr.split(",");
            for (String register : registers) {
                try {
                    logger.debug(Messages.get("info.feature.register.item", "registers", register));
                    Class clazz = ClassUtils.getClass(register);
                    if (isRegistered(clazz)) {
                        beak++;
                        logger.warn(Messages.get("info.feature.exists", register));
                        continue;
                    }

                    register(clazz);
                    suc++;
                } catch (ClassNotFoundException e) {
                    fail++;
                    if (!register.startsWith("default."))
                        logger.error(Messages.get("info.feature.sys.get.error"), e);
                    else
                        logger.warn(Messages.get("info.feature.not.found.error", register), e);
                }
            }
        }

        logger.info(Messages.get("info.feature.collect", suc, fail, beak));
    }

    private void subscribeResourceEvent() {

        final Set<ClassFoundEvent.ClassInfo> resources = Sets.newLinkedHashSet();

        SystemEventBus.subscribe(ClassFoundEvent.class, new Listener<ClassFoundEvent>() {
            @Override
            public void onReceive(ClassFoundEvent event) {
                event.accept(new ClassFoundEvent.ClassAccept() {
                    @Override
                    public boolean accept(ClassFoundEvent.ClassInfo info) {
                        if (info.isPublic()) {
                            boolean add = false;
                            if (info.containsAnnotations(Path.class, Provider.class)) {
                                add = true;
                            } else {
                                try {
                                    CtClass ctClass = info.getCtClass().getSuperclass();
                                    while (ctClass != null) {
                                        Object[] anns = ctClass.getAvailableAnnotations();
                                        for (Object anno : anns) {
                                            Class clazz = ((Annotation) anno).annotationType();
                                            if (clazz.equals(Path.class) || clazz.equals(Provider.class)) {
                                                add = true;
                                                break;
                                            }
                                        }
                                        if (add) {
                                            break;
                                        }
                                        ctClass = ctClass.getSuperclass();
                                    }
                                } catch (Exception e) {
                                    //no op
                                }
                            }
                            if (add) {
                                resources.add(info);
                                return true;
                            }
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
        if (packages.length > 0) {
            logger.info(Messages.get("info.configure.resource.package", StringUtils.join(packages, ",")));
        } else {
            logger.warn(Messages.get("info.configure.resource.package.none"));
        }
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
                        logger.error(Messages.get("info.config.error.key", key), e);
                    }
                }
            }
        }

        map.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, "true");

        //移除转化需要的临时属性
        for (String key : removeKeys) {
            configMap.remove(key);
        }

        //将转化的jersey配置放入临时配置对象
        configMap.putAll(map);

        //清空转化的配置
        map.clear();
    }

    private void configureServer() {
        jmxEnabled = Boolean.parseBoolean((String) getProperty("jmx.enabled"));
        if (jmxEnabled && getProperty(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED) == null)
            property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, jmxEnabled);

        subscribeServerEvent();
    }

    private void subscribeServerEvent() {
        SystemEventBus.subscribe(Container.StartupEvent.class, new Listener<Container.StartupEvent>() {

            final String line = System.getProperty("line.separator", "/n");
            final String lineStart = "- ";
            final String lineChild = " >";
            final StringBuilder builder = new StringBuilder();

            void appendInfo(String key, Object... value) {
                builder.append(lineStart)
                        .append(Messages.get(key, value))
                        .append(line);
            }

            void appendVisitUrl(Connector connector) {
                String httpStart = "http" + (connector.isSecureEnabled() ? "s" : "") + "://";
                if (connector.getHost().equals("0.0.0.0")) {
                    try {
                        Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();
                        while (netInterfaces.hasMoreElements()) {
                            NetworkInterface ni = (NetworkInterface) netInterfaces
                                    .nextElement();
                            Enumeration nii = ni.getInetAddresses();
                            while (nii.hasMoreElements()) {
                                InetAddress ip = (InetAddress) nii.nextElement();
                                if (ip instanceof Inet4Address) {
                                    String ipAddr = ip.getHostAddress();
                                    if (ipAddr != null && !ipAddr.equals("127.0.0.1")) {
                                        builder.append(line)
                                                .append(lineStart)
                                                .append(lineChild)
                                                .append(httpStart)
                                                .append(ip.getHostAddress())
                                                .append(":")
                                                .append(connector.getPort())
                                                .append("/");
                                    }
                                }
                            }
                        }
                    } catch (SocketException e) {
                        // noop
                    }
                    builder.append(line)
                            .append(lineStart)
                            .append(lineChild)
                            .append(httpStart)
                            .append("localhost:")
                            .append(connector.getPort())
                            .append("/")
                            .append(line)
                            .append(lineStart)
                            .append(lineChild)
                            .append(httpStart)
                            .append("127.0.0.1:")
                            .append(connector.getPort())
                            .append("/");
                } else {
                    builder.append(line)
                            .append(lineStart)
                            .append(lineChild)
                            .append(connector.getHttpServerBaseUri());
                }
            }

            @Override
            public void onReceive(Container.StartupEvent event) {

                Application.this.container = event.getContainer();

                if (!isInitialized()) {
                    Runtime r = Runtime.getRuntime();
                    r.gc();

                    final String startUsedTime = Times.toDuration(System.currentTimeMillis() - timestamp);
                    builder.append(line)
                            .append(INFO_SPLITER)
                            .append(line);
                    appendInfo("info.ameba.version", Ameba.getVersion());
                    appendInfo("info.http.container", StringUtils.defaultString(container.getType(), "Unknown"));
                    appendInfo("info.start.time", startUsedTime);
                    appendInfo("info.app.name", getApplicationName());
                    appendInfo("info.app.version", getApplicationVersion());
                    appendInfo(
                            "info.memory.usage",
                            FileUtils.byteCountToDisplaySize((r.totalMemory() - r.freeMemory())),
                            FileUtils.byteCountToDisplaySize(r.maxMemory())
                    );
                    appendInfo("info.jmx.enabled", Messages.get("info.enabled." + isJmxEnabled()));
                    appendInfo("info.app.mode", Messages.get("info.app.mode." + getMode().name().toLowerCase()));
                    builder.append(lineStart)
                            .append(Messages.get("info.locations"));

                    List<Connector> connectors = getConnectors();
                    if (connectors != null && connectors.size() > 0) {
                        for (Connector connector : connectors) {
                            appendVisitUrl(connector);
                        }
                    } else {
                        builder.append(line)
                                .append(lineStart)
                                .append(lineChild)
                                .append(Messages.get("info.locations.none"));
                        logger.warn(Messages.get("info.connector.none"));
                    }
                    builder.append(line)
                            .append(INFO_SPLITER);
                    logger.info(Messages.get("info.started"));
                    logger.getSource().info(builder.toString());
                }

                initialized = true;
            }
        });
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
        URL loggerConfigFile = getResource("conf/logback_" + getMode().name().toLowerCase() + ".groovy");

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        context.putObject("properties", properties);

        if (loggerConfigFile != null) {
            GafferUtil.runGafferConfiguratorOn(context, this, loggerConfigFile);
        }

        URL userLoggerConfigFile = getResource(StringUtils.defaultIfBlank(
                properties.getProperty("logger.config.file"), "conf/" + DEFAULT_LOGBACK_CONF));

        if (userLoggerConfigFile != null) {
            GafferUtil.runGafferConfiguratorOn(context, this, userLoggerConfigFile);
        }

        if (ids != null && ids.length > 0) {

            String confDir = "conf/log/";
            String[] logConf = DEFAULT_LOGBACK_CONF.split("\\.");
            String logConfPrefix = logConf[0];
            String logConfSuffix = "." + logConf[1];

            for (String id : ids) {
                URL configFile = getResource(confDir + logConfPrefix + "_" + id + logConfSuffix);
                if (configFile != null)
                    GafferUtil.runGafferConfiguratorOn(context, this, configFile);
            }
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

    public Map<String, Object> getSrcProperties() {
        return srcProperties;
    }

    public Set<String> getExcludes() {
        return excludes;
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

    public static final class Props extends LinkedProperties {

        @Override
        public synchronized Object put(Object key, Object value) {

            if (value instanceof String) {
                String str = (String) value;
                str = str.trim();
                if (str.startsWith("$") && !str.startsWith("$$")) {
                    str = str.substring(1);
                    value = System.getProperty(str);
                    if (StringUtils.isBlank((CharSequence) value)) {
                        value = System.getenv(str);
                    }
                }
            }
            return super.put(key, value);
        }
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
                if (ctClass == null && fileName.endsWith(".class")) {
                    try {
                        ctClass = ClassPool.getDefault().makeClass(getFileStream());
                    } catch (IOException e) {
                        logger.error("make class error", e);
                    }
                }
                return ctClass;
            }

            public String getClassName() {
                return getCtClass().getName();
            }

            public Object[] getAnnotations() {
                if (annotations == null) {
                    try {
                        annotations = getCtClass().getAvailableAnnotations();
                    } catch (Exception | Error e) {
                        return new Object[0];
                    }
                }
                return annotations;
            }

            @SuppressWarnings("unchecked")
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
