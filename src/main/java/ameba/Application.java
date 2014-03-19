package ameba;

import ameba.util.LinkedProperties;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.gaffer.GafferUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.spdy.SpdyAddOn;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
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
 * @author: ICode
 * @since: 13-8-6 下午8:42
 */
@Singleton
public class Application extends ResourceConfig {
    public static final Logger logger = LoggerFactory.getLogger(Application.class);
    private URI httpServerBaseUri;
    private String mode;
    private String domain;
    private String host;
    private boolean secure;
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

    public Application() {
        this("conf/application.conf");
    }

    @SuppressWarnings("unchecked")
    public Application(String confFile) {

        //property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        //property(ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, true);

        Map<String, Object> configMap = Maps.newLinkedHashMap();

        Properties properties = new LinkedProperties();

        //读取系统默认配置
        try {
            properties.load(getResourceAsStream("conf/default.conf"));
            //将默认配置放入临时配置对象
            configMap.putAll((Map) properties);
        } catch (Exception e) {
            logger.warn("读取[conf/default.conf]出错", e);
        }

        //读取应用程序配置
        InputStream in = getResourceAsStream(confFile);
        if (in != null) {
            try {
                properties.clear();
                properties.load(in);
            } catch (Exception e) {
                logger.error("读取[conf/application.conf]出错", e);
            }
        } else {
            logger.warn("用户配置文件[conf/application.conf]不存在");
        }

        //获取应用程序模式
        mode = properties.getProperty("app.mode");
        //设置ssl相关
        secure = Boolean.parseBoolean(properties.getProperty("ssl.enabled", "false"));
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

        if (sslKeyStoreFile != null &&
                StringUtils.isNotBlank(sslKeyPassword) &&
                StringUtils.isNotBlank(sslKeyStorePassword)) {
            sslConfigReady = true;
        }

        Properties modeProperties = new Properties();

        //读取相应模式的配置文件
        if (StringUtils.isNotBlank(mode)) {
            try {
                modeProperties.load(getResourceAsStream("conf/" + mode + ".conf"));
                //将模式配置放入临时配置对象
                configMap.putAll((Map) modeProperties);
            } catch (IOException e) {
                logger.warn("读取[conf/" + mode + ".conf]出错", e);
            }
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
        setApplicationName((String) getProperty("app.name"));

        //配置日志器
        loggerConfigure();

        String[] packages = StringUtils.deleteWhitespace(StringUtils.defaultIfBlank((String) getProperty("resource.packages"), "")).split(",");
        logger.info("设置RESEful [resource扫描包({})]", getProperty("resource.packages"));
        packages(packages);

        if ("dev".equals(mode)) {
            logger.info("注册日志过滤器");
            register(LoggingFilter.class);
        }

        logger.info("装载注册器");
        java.lang.reflect.Method method = null;
        try {
            method = this.getClass().getMethod("register", Class.class);
        } catch (NoSuchMethodException e) {
            logger.error("获取注册器失败", e);
        }
        if (method != null) {
            String registerStr = StringUtils.deleteWhitespace(StringUtils.defaultIfBlank((String) getProperty("app.registers"), ""));
            String[] registers = null;
            if (StringUtils.isNotBlank(registerStr)) {
                registers = registerStr.split(",");
                for (String register : registers) {
                    try {
                        logger.debug("安装注册器[{}]", register);
                        Class clazz = Class.forName(register);
                        if (isRegistered(clazz)) {
                            logger.debug("并安装注册器[{}]，因为该注册器已存在", register);
                            continue;
                        }
                        method.invoke(this, clazz);
                    } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
                        logger.error("获取注册器失败", e);
                    }
                }
            }

            for (String key : configMap.keySet()) {
                if (key.startsWith("app.register.")) {
                    String className = (String) getProperty(key);
                    if (StringUtils.isNotBlank(className)) {
                        String name = key.replaceFirst("^app\\.register\\.", "");
                        try {
                            logger.debug("安装注册器[{}({})]", name, className);
                            Class clazz = Class.forName(className);
                            if (isRegistered(clazz)) {
                                logger.debug("并安装注册器[{}({})]，因为该注册器已存在", name, clazz);
                                continue;
                            }
                            method.invoke(this, clazz);
                        } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
                            if (!name.startsWith("default."))
                                logger.error("获取注册器失败", e);
                            else
                                logger.warn("未找到系统默认特性[" + className + "]", e);
                        }
                    }
                }
            }
        }

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

        //config server base uri
        httpServerBaseUri = URI.create("http://" + this.host
                + ":" + this.port + "/");
        logger.info("配置服务器监听地址绑定[{}]", httpServerBaseUri);
    }

    public static HttpServer createHttpServer() {

        final Application app = new Application();
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
            sslContextConfiguration.setTrustStorePass(app.getSslTrustStorePassword());
            sslContextConfiguration.setTrustStoreType(app.getSslTrustStoreType());
            sslContextConfiguration.setTrustStoreProvider(app.getSslTrustStoreProvider());
            sslContextConfiguration.setTrustManagerFactoryAlgorithm(app.getSslTrustManagerFactoryAlgorithm());

            sslEngineConfigurator = new SSLEngineConfigurator(sslContextConfiguration,
                    app.isSslClientMode(), app.isSslNeedClientAuth(), app.isSslWantClientAuth());
        }

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(app.httpServerBaseUri, app, app.isSecure(), sslEngineConfigurator, false);

        if (app.isSecure()) {
            NetworkListener listener = server.getListener("grizzly");
            if (listener != null) {
                SpdyAddOn spdyAddon = new SpdyAddOn();
                listener.registerAddOn(spdyAddon);
            }
        }

        server.getServerConfiguration().setSendFileEnabled(true);
        return server;
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

    public String getMode() {
        return mode;
    }

    public URI getHttpServerBaseUri() {
        return httpServerBaseUri;
    }

    public boolean isSecure() {
        return secure;
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

    /**
     * 设置日志器
     */
    private void loggerConfigure() {
        //set logback config file
        URL loggerConfigFile = getResource(StringUtils.defaultIfBlank((String) getProperty("logger.config.file"), "conf/logback.groovy"));

        if (loggerConfigFile == null) {
            loggerConfigFile = getResource("conf/logback-" + getMode() + ".groovy");
        }


        if (loggerConfigFile != null) {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.reset();
            context.putProperty("appName", getApplicationName());
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
