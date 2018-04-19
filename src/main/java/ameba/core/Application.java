package ameba.core;

import ameba.Ameba;
import ameba.container.Container;
import ameba.container.event.StartupEvent;
import ameba.container.server.Connector;
import ameba.core.event.RequestEvent;
import ameba.event.Listener;
import ameba.event.SystemEventBus;
import ameba.exception.AmebaException;
import ameba.exception.ConfigErrorException;
import ameba.feature.AmebaFeature;
import ameba.i18n.Messages;
import ameba.inject.Value;
import ameba.scanner.Acceptable;
import ameba.scanner.ClassFoundEvent;
import ameba.scanner.ClassInfo;
import ameba.scanner.PackageScanner;
import ameba.util.*;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferUtil;
import com.google.common.base.Charsets;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import javassist.CtClass;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.model.ContractProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ResourceFinder;
import org.glassfish.jersey.server.ServerConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.stream.Stream;

import static ameba.util.AmebaInfo.INFO_SEPARATOR;
import static ameba.util.IOUtils.*;

/**
 * 应用程序启动配置
 *
 * @author icode
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
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static String SCAN_CLASSES_CACHE_FILE;
    protected boolean jmxEnabled;
    private String[] configFiles;
    private long timestamp = System.currentTimeMillis();
    private boolean initialized = false;
    private Mode mode;
    private CharSequence applicationVersion;
    private Container container;
    private Set<Addon> addons = Sets.newLinkedHashSet();
    private Set<String> excludes = Sets.newLinkedHashSet();
    private ResourceConfig config = new ExcludeResourceConfig(excludes);
    private Map<String, Object> srcProperties = Maps.newLinkedHashMap();
    private Set<String> scanPackages;
    private String[] ids;

    /**
     * <p>Constructor for Application.</p>
     */
    protected Application() {
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

        Set<String> configFiles = parseIds2ConfigFile(ids);
        this.configFiles = configFiles.toArray(new String[configFiles.size()]);

        configure();
    }

    /**
     * <p>parseIds2ConfigFile.</p>
     *
     * @param ids a {@link java.lang.String} object.
     * @return a {@link java.util.Set} object.
     */
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

    /**
     * <p>readModuleConfig.</p>
     *
     * @param properties a {@link java.util.Properties} object.
     * @param isDev      a boolean.
     */
    @SuppressWarnings("unchecked")
    public static void readModuleConfig(Properties properties, boolean isDev) {
        logger.info(Messages.get("info.module.load.conf"));
        //读取模块配置
        Enumeration<URL> moduleUrls = IOUtils.getResources("conf/module.conf");
        List<String> readedModule = Lists.newArrayList();
        if (moduleUrls.hasMoreElements()) {
            while (moduleUrls.hasMoreElements()) {
                InputStream in = null;
                URL url = moduleUrls.nextElement();
                try {
                    String fileName = url.getFile();
                    String modelName = fileName;
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

                    if (url.getProtocol().equals("file") && "module".equals(modelName)) {
                        //projectName/target/classes/conf/module
                        //projectName/src/main/resources/conf/module
                        if (fileName.endsWith("/target/classes/conf/module.conf")) {
                            try {
                                modelName = Paths.get(url.toURI())
                                        .resolveSibling("../../../").normalize().getFileName().toString();
                            } catch (URISyntaxException e) {
                                // no op
                            }
                        } else if (fileName.endsWith("/src/main/resources/conf/module.conf")) {
                            try {
                                modelName = Paths.get(url.toURI()).resolveSibling("../../../../")
                                        .normalize().getFileName().toString();
                            } catch (URISyntaxException e) {
                                // no op
                            }
                        }
                    }

                    if (readedModule.contains(modelName)) continue;
                    readedModule.add(modelName);

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
                loadProperties(in, properties, url);
            }
        } else {
            logger.info(Messages.get("info.module.none"));
        }
    }


    private static void loadProperties(InputStream in, Properties properties, URL url) {
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

    /**
     * <p>readModeConfig.</p>
     *
     * @param properties a {@link java.util.Properties} object.
     * @param mode       a {@link ameba.core.Application.Mode} object.
     */
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

    /**
     * <p>readDefaultConfig.</p>
     *
     * @return a {@link java.util.Properties} object.
     */
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

    /**
     * <p>readAppConfig.</p>
     *
     * @param properties a {@link java.util.Properties} object.
     * @param confFile   a {@link java.lang.String} object.
     * @return a {@link java.net.URL} object.
     */
    public static URL readAppConfig(Properties properties, String confFile) {
        Enumeration<URL> urls = IOUtils.getResources(confFile);
        URL url = null;
        if (urls.hasMoreElements()) {
            InputStream in = null;
            Set<URL> urlSet = Sets.newHashSet(Iterators.forEnumeration(urls));

            if (urlSet.size() > 1) {
                String errorMsg = Messages.get("info.load.config.multi.error",
                        StringUtils.join(urlSet.stream().map(Application::toExternalForm), LINE_SEPARATOR));
                logger.error(errorMsg);
                throw new ConfigErrorException(errorMsg);
            }

            url = urlSet.iterator().next();

            try {
                logger.trace(Messages.get("info.load", toExternalForm(url)));
                in = url.openStream();
            } catch (IOException e) {
                logger.error(Messages.get("info.load.error", toExternalForm(url)));
            }
            loadProperties(in, properties, url);
        } else {
            logger.warn(Messages.get("info.load.error.not.found", confFile));
        }
        return url;
    }

    /**
     * <p>reconfigure.</p>
     */
    public void reconfigure() {
        addons = Sets.newLinkedHashSet();
        excludes = Sets.newLinkedHashSet();
        config = new ExcludeResourceConfig(excludes);
        srcProperties = Maps.newLinkedHashMap();
        configure();
    }

    /**
     * <p>configure.</p>
     */
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

        setEnvironmentConfig(srcProperties);

        addOnSetup(srcProperties);

        //转换jersey配置项
        convertJerseyConfig(srcProperties);

        //将临时配置对象放入应用程序配置
        addProperties(srcProperties);

        srcProperties = Collections.unmodifiableMap(srcProperties);

        registerBinder();

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

        addons = Collections.unmodifiableSet(addons);
        excludes = Collections.unmodifiableSet(excludes);

        logger.info(Messages.get("info.feature.load"));
    }

    private void configureExclude(Map<String, Object> configMap) {

        String ex = (String) configMap.get(EXCLUDES_KEY);
        if (StringUtils.isNotBlank(ex)) {
            addExcludes(ex);
        }

        configMap.keySet().stream()
                .filter(key -> key.startsWith(EXCLUDES_KEY_PREFIX))
                .forEachOrdered(key -> addExcludes((String) configMap.get(key)));

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
            SCAN_CLASSES_CACHE_FILE = IOUtils.getResource("/").getPath()
                    + "conf/classes_" + getApplicationVersion() + ".list";
        }
        URL cacheList = IOUtils.getResource(SCAN_CLASSES_CACHE_FILE);
        if (cacheList == null || getMode().isDev()) {
            logger.debug(Messages.get("info.scan.classes"));
            PackageScanner scanner = new PackageScanner(scanPackages);
            scanner.scan();

            if (getMode().isDev()) return;

            OutputStream out = null;
            try {
                File cacheFile = new File(SCAN_CLASSES_CACHE_FILE);

                if (cacheFile.isDirectory()) {
                    FileUtils.deleteQuietly(cacheFile);
                }

                out = FileUtils.openOutputStream(cacheFile);

                IOUtils.writeLines(scanner.getAcceptClasses(), null, out, Charsets.UTF_8);
            } catch (IOException e) {
                logger.error(Messages.get("info.write.class.cache.error"), e);
            } finally {
                closeQuietly(out);
            }
            scanner.clear();
        } else {
            logger.debug(Messages.get("info.read.class.cache"));
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
                        ClassInfo info = new ClassInfo(className.substring(className.lastIndexOf(".") + 1).concat(".class")) {
                            @Override
                            public InputStream getFileStream() {
                                return fin;
                            }

                            @Override
                            public void closeFileStream() {
                                closeQuietly(fin);
                            }
                        };
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

    private void registerBinder() {
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(Application.this).to(Application.class).proxy(false);
                bind(mode).to(Application.Mode.class).proxy(false);
                bind(ConfigurationInjectionResolver.class)
                        .to(new GenericType<InjectionResolver<Named>>() {
                        }).in(Singleton.class);
                bind(ValueConfigurationInjectionResolver.class)
                        .to(new GenericType<InjectionResolver<Value>>() {
                        }).in(Singleton.class);
            }
        });
        register(Requests.BindRequest.class);
        register(SysEventListener.class);
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

    /**
     * <p>addOnSetup.</p>
     *
     * @param configMap a {@link java.util.Map} object.
     */
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
                if (Addon.class.isAssignableFrom(addOnClass)) {
                    Addon addon = (Addon) addOnClass.newInstance();
                    if (addon.isEnabled(this) && addons.add(addon)) {
                        addon.setup(this);
                    }
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

    /**
     * <p>addOnDone.</p>
     */
    protected void addOnDone() {
        for (Addon addon : addons) {
            try {
                addon.done(this);
            } catch (Exception e) {
                logger.error(Messages.get("info.addon.error", addon.getClass().getName()), e);
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

    /**
     * <p>configureFeature.</p>
     *
     * @param configMap a {@link java.util.Map} object.
     */
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

        final Set<ClassInfo> resources = Sets.newLinkedHashSet();

        SystemEventBus.subscribe(ClassFoundEvent.class, event -> event.accept(new Acceptable<ClassInfo>() {

            private boolean isResource(CtClass ctClass) {
                int modifiers = ctClass.getModifiers();
                return !javassist.Modifier.isAbstract(modifiers)
                        && !javassist.Modifier.isInterface(modifiers)
                        && !javassist.Modifier.isAnnotation(modifiers)
                        && !javassist.Modifier.isEnum(modifiers);
            }

            @Override
            public boolean accept(ClassInfo info) {
                if (info.isPublic()) {
                    CtClass thisClass = info.getCtClass();
                    if (isResource(thisClass)) {
                        if (info.accpet(ctClass -> ctClass.hasAnnotation(Path.class)
                                || ctClass.hasAnnotation(Provider.class))) {
                            resources.add(info);
                            return true;
                        }
                    }
                }
                return false;
            }
        }));

        addons.add(new Addon() {
            @Override
            public void done(Application application) {
                for (ClassInfo info : resources) {
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
            logger.info(Messages.get("info.configure.resource.package",
                    StringUtils.join(packages, "," + LINE_SEPARATOR)));
        } else {
            logger.warn(Messages.get("info.configure.resource.package.none"));
        }
        packages(packages);
        subscribeResourceEvent();
    }

    private void setEnvironmentConfig(Map<String, Object> configMap) {
        configMap.forEach((key, value) -> {
            if (key.startsWith("env.") && value instanceof String) {
                System.setProperty(key.substring(4), (String) value);
            }
        });
    }

    private void convertJerseyConfig(Map<String, Object> configMap) {
        Map<String, Field> staticFieldsMap = Maps.newLinkedHashMap();
        Stream.of(ServerProperties.class.getDeclaredFields())
                .filter((field) -> Modifier.isStatic(field.getModifiers()))
                .forEach((field) -> staticFieldsMap.put(field.getName(), field));

        List<String> removeKeys = Lists.newArrayList();
        Map<String, Object> map = Maps.newLinkedHashMap();

        //进行jersey配置项转化
        configMap.forEach((key, value) -> {
            if (key.startsWith(JERSEY_CONF_NAME_PREFIX)) {
                String name = key.substring(JERSEY_CONF_NAME_PREFIX.length());
                //转化键到jersey配置
                name = name.replace(".", "_").toUpperCase();
                Field filed = staticFieldsMap.get(name);
                if (null != filed) {
                    filed.setAccessible(true);
                    try {
                        map.put((String) filed.get(null), value);
                        removeKeys.add(key);
                    } catch (IllegalAccessException e) {
                        logger.error(Messages.get("info.config.error.key", key), e);
                    }
                }
            }
        });

        map.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, "true");
        map.put(ServerProperties.MOXY_JSON_FEATURE_DISABLE, "true");
        map.put(ServerProperties.JSON_PROCESSING_FEATURE_DISABLE, "true");

        //移除转化需要的临时属性
        removeKeys.forEach(configMap::remove);

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
        SystemEventBus.subscribe(StartupEvent.class, new Listener<StartupEvent>() {
            final String lineStart = "- ";
            final String lineChild = " >";
            final StringBuilder builder = new StringBuilder();

            void appendInfo(String key, Object... value) {
                builder.append(lineStart)
                        .append(Messages.get(key, value))
                        .append(LINE_SEPARATOR);
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
                                        builder.append(LINE_SEPARATOR)
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
                    builder.append(LINE_SEPARATOR)
                            .append(lineStart)
                            .append(lineChild)
                            .append(httpStart)
                            .append("localhost:")
                            .append(connector.getPort())
                            .append("/")
                            .append(LINE_SEPARATOR)
                            .append(lineStart)
                            .append(lineChild)
                            .append(httpStart)
                            .append("127.0.0.1:")
                            .append(connector.getPort())
                            .append("/");
                } else {
                    builder.append(LINE_SEPARATOR)
                            .append(lineStart)
                            .append(lineChild)
                            .append(connector.getHttpServerBaseUri());
                }
            }

            @Override
            public void onReceive(StartupEvent event) {

                Application.this.container = event.getContainer();

                if (!isInitialized()) {
                    Runtime r = Runtime.getRuntime();
                    r.gc();

                    final String startUsedTime = Times.toDuration(System.currentTimeMillis() - timestamp);
                    builder.append(LINE_SEPARATOR)
                            .append(INFO_SEPARATOR)
                            .append(LINE_SEPARATOR);
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
                        connectors.forEach(this::appendVisitUrl);
                    } else {
                        builder.append(LINE_SEPARATOR)
                                .append(lineStart)
                                .append(lineChild)
                                .append(Messages.get("info.locations.none"));
                        logger.warn(Messages.get("info.connector.none"));
                    }
                    builder.append(LINE_SEPARATOR)
                            .append(INFO_SEPARATOR);
                    logger.info(Messages.get("info.started"));
                    logger.info(builder.toString());
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
        if (scanPackages == null) {
            scanPackages = Sets.newHashSet();
        }
        Collections.addAll(scanPackages, packages);
        return this;
    }

    /**
     * <p>getPackages.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getPackages() {
        return scanPackages;
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

    /**
     * <p>isInitialized.</p>
     *
     * @return a boolean.
     */
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
     * <p>Getter for the field <code>configFiles</code>.</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getConfigFiles() {
        return configFiles;
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

        String appPackage = properties.getProperty("app.package");
        if (StringUtils.isBlank(appPackage)) {
            logger.warn(Messages.get("warn.app.package.not.config"));
        }
    }

    /**
     * <p>Getter for the field <code>srcProperties</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Object> getSrcProperties() {
        return srcProperties;
    }

    /**
     * <p>Getter for the field <code>excludes</code>.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getExcludes() {
        return excludes;
    }

    /**
     * <p>Getter for the field <code>addons</code>.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Addon> getAddons() {
        return addons;
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

    @Provider
    protected static class SysEventListener implements ApplicationEventListener {
        @Override
        public void onEvent(ApplicationEvent event) {
            SystemEventBus.publish(new ameba.core.event.ApplicationEvent(event));
        }

        @Override
        public RequestEventListener onRequest(org.glassfish.jersey.server.monitoring.RequestEvent requestEvent) {
            AmebaFeature.publishEvent(new RequestEvent(requestEvent));
            return event -> AmebaFeature.publishEvent(new RequestEvent(event));
        }
    }

    protected static abstract class BaseConfigurationInjectionResolver<T extends Annotation> implements InjectionResolver<T> {
        @Context
        Application application;
        Class<T> aType;

        protected BaseConfigurationInjectionResolver(Class<T> aType) {
            this.aType = aType;
        }

        protected abstract String getName(Annotation annotation);

        @Override
        public Object resolve(Injectee injectee, ServiceHandle<?> root) {
            Map<String, Object> props = application.getProperties();
            AnnotatedElement element = injectee.getParent();
            Annotation annotation;
            if (injectee.getPosition() == -1) {
                annotation = element.getAnnotation(aType);
            } else {
                annotation = ((Executable) element).getParameters()[injectee.getPosition()].getAnnotation(aType);
            }
            String name = getName(annotation);
            Object value = props.get(name);
            Type type = injectee.getRequiredType();
            if (value instanceof String) {
                if (ReflectionHelper.isSubClassOf(type, Integer.class)) {
                    return Ints.tryParse((String) value);
                } else if (ReflectionHelper.isSubClassOf(type, int.class)) {
                    Integer v = Ints.tryParse((String) value);
                    return v == null ? -1 : v;
                } else if (ReflectionHelper.isSubClassOf(type, Long.class)) {
                    return Longs.tryParse((String) value);
                } else if (ReflectionHelper.isSubClassOf(type, long.class)) {
                    Long v = Longs.tryParse((String) value);
                    return v == null ? -1 : v;
                } else if (ReflectionHelper.isSubClassOf(type, Double.class)) {
                    return Doubles.tryParse((String) value);
                } else if (ReflectionHelper.isSubClassOf(type, double.class)) {
                    Double v = Doubles.tryParse((String) value);
                    return v == null ? -1 : v;
                } else if (ReflectionHelper.isSubClassOf(type, Float.class)) {
                    return Floats.tryParse((String) value);
                } else if (ReflectionHelper.isSubClassOf(type, float.class)) {
                    Float v = Floats.tryParse((String) value);
                    return v == null ? -1 : v;
                } else if (ReflectionHelper.isSubClassOf(type, Boolean.class)
                        || ReflectionHelper.isSubClassOf(type, boolean.class)) {
                    return Boolean.parseBoolean((String) value);
                }
            }

            return value;
        }

        @Override
        public boolean isConstructorParameterIndicator() {
            return true;
        }

        @Override
        public boolean isMethodParameterIndicator() {
            return false;
        }
    }

    protected static class ConfigurationInjectionResolver extends BaseConfigurationInjectionResolver<Named> {
        protected ConfigurationInjectionResolver() {
            super(Named.class);
        }

        @Override
        protected String getName(Annotation annotation) {
            return ((Named) annotation).value();
        }
    }

    protected static class ValueConfigurationInjectionResolver extends BaseConfigurationInjectionResolver<Value> {
        protected ValueConfigurationInjectionResolver() {
            super(Value.class);
        }

        @Override
        protected String getName(Annotation annotation) {
            return ((Value) annotation).value();
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
